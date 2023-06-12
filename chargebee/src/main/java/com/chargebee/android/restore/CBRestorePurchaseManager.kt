package com.chargebee.android.restore

import android.content.Context
import android.util.Log
import com.chargebee.android.ErrorDetail
import com.chargebee.android.billingservice.CBCallback
import com.chargebee.android.billingservice.CBPurchase
import com.chargebee.android.billingservice.GPErrorCode
import com.chargebee.android.exceptions.CBException
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.loggers.CBLogger
import com.chargebee.android.models.*
import com.chargebee.android.models.ResultHandler
import com.chargebee.android.resources.RestorePurchaseResource

class CBRestorePurchaseManager {

    companion object {
        private var allTransactions = ArrayList<PurchaseTransaction>()
        private var restorePurchases = ArrayList<CBRestoreSubscription>()
        private var activeTransactions = ArrayList<PurchaseTransaction>()
        private lateinit var completionCallback: CBCallback.RestorePurchaseCallback

        private fun retrieveStoreSubscription(
            purchaseToken: String,
            completion: (ChargebeeResult<Any>) -> Unit
        ) {
            val logger = CBLogger(name = "restore", action = "retrieve_restore_subscriptions")
            ResultHandler.safeExecuter(
                { RestorePurchaseResource().retrieveStoreSubscription(purchaseToken) },
                completion,
                logger
            )
        }

        internal fun retrieveRestoreSubscription(
            purchaseToken: String,
            result: (CBRestoreSubscription) -> Unit,
            error: (CBException) -> Unit
        ) {
            retrieveStoreSubscription(purchaseToken) {
                when (it) {
                    is ChargebeeResult.Success -> {
                        val restoreSubscription =
                            ((it.data) as CBRestorePurchases).inAppSubscriptions.firstOrNull()
                        restoreSubscription?.let {
                            result(restoreSubscription)
                        }
                    }
                    is ChargebeeResult.Error -> {
                        error(it.exp)
                    }
                }
            }
        }

        internal fun fetchStoreSubscriptionStatus(
            context: Context,
            storeTransactions: ArrayList<PurchaseTransaction>,
            completionCallback: CBCallback.RestorePurchaseCallback
        ) {
            this.completionCallback = completionCallback
            if (storeTransactions.isNotEmpty()) {
                val storeTransaction =
                    storeTransactions.firstOrNull()?.also { storeTransactions.remove(it) }
                storeTransaction?.purchaseToken?.let { purchaseToken ->
                    retrieveRestoreSubscription(purchaseToken, {
                        restorePurchases.add(it)
                        when (it.storeStatus) {
                            StoreStatus.Active.value -> activeTransactions.add(storeTransaction)
                            else -> allTransactions.add(storeTransaction)
                        }
                        getRestorePurchases(context, storeTransactions)
                    }, { _ ->
                        getRestorePurchases(context, storeTransactions)
                    })
                }
            } else {
                completionCallback.onSuccess(emptyList())
            }
        }

        internal fun getRestorePurchases(context: Context, storeTransactions: ArrayList<PurchaseTransaction>) {
            if (storeTransactions.isEmpty()) {
                if (restorePurchases.isEmpty()) {
                    completionCallback.onError(
                        CBException(
                            ErrorDetail(
                                message = GPErrorCode.InvalidPurchaseToken.errorMsg,
                                httpStatusCode = 400
                            )
                        )
                    )
                } else {
                    val activePurchases = restorePurchases.filter { subscription ->
                        subscription.storeStatus == StoreStatus.Active.value
                    }
                    val allPurchases = restorePurchases.filter { subscription ->
                        subscription.storeStatus == StoreStatus.Active.value || subscription.storeStatus == StoreStatus.InTrial.value
                                || subscription.storeStatus == StoreStatus.Cancelled.value || subscription.storeStatus == StoreStatus.Paused.value
                    }
                    if (CBPurchase.includeInActivePurchases) {
                        completionCallback.onSuccess(allPurchases)
                        syncPurchaseWithChargebee(allTransactions, context)
                    } else {
                        completionCallback.onSuccess(activePurchases)
                        syncPurchaseWithChargebee(activeTransactions, context)
                    }
                }
                restorePurchases.clear()
                allTransactions.clear()
                activeTransactions.clear()
            } else {
                fetchStoreSubscriptionStatus(context, storeTransactions, completionCallback)
            }
        }

        internal fun syncPurchaseWithChargebee(storeTransactions: ArrayList<PurchaseTransaction>, context: Context) {
            storeTransactions.forEach { productIdList ->
                validateReceipt(productIdList.purchaseToken, productIdList.productId, context)
            }
        }

        internal fun validateReceipt(
            purchaseToken: String,
            productId: List<String>,
            context: Context
        ) {
            CBPurchase.retrieveProducts(
                context,
                productId as ArrayList<String>,
                object : CBCallback.ListProductsCallback<ArrayList<CBProduct>> {
                    override fun onSuccess(productIDs: ArrayList<CBProduct>) {
                        if (productIDs.size == 0) {
                            Log.i(javaClass.simpleName, "Product not available")
                            return
                        }
                        CBPurchase.validateReceipt(purchaseToken, productIDs.first()) {
                            when (it) {
                                is ChargebeeResult.Success -> {
                                    Log.i(javaClass.simpleName, "result :  ${it.data}")
                                }
                                is ChargebeeResult.Error -> {
                                    Log.e(
                                        javaClass.simpleName,
                                        "Exception from Server - validateReceipt() :  ${it.exp.message}"
                                    )
                                }
                            }
                        }
                    }

                    override fun onError(error: CBException) {
                        Log.e(javaClass.simpleName, "Error:  ${error.message}")
                    }
                })
        }
    }
}