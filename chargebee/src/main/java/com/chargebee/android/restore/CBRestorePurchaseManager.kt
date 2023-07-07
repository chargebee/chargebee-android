package com.chargebee.android.restore

import android.util.Log
import com.chargebee.android.ErrorDetail
import com.chargebee.android.billingservice.CBCallback
import com.chargebee.android.billingservice.CBPurchase
import com.chargebee.android.billingservice.GPErrorCode
import com.chargebee.android.billingservice.ProductType
import com.chargebee.android.exceptions.CBException
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.loggers.CBLogger
import com.chargebee.android.models.*
import com.chargebee.android.models.ResultHandler
import com.chargebee.android.resources.RestorePurchaseResource

class CBRestorePurchaseManager {

    companion object {
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
            storeTransactions: ArrayList<PurchaseTransaction>,
            allTransactions: ArrayList<PurchaseTransaction>,
            activeTransactions: ArrayList<PurchaseTransaction>,
            restorePurchases: ArrayList<CBRestoreSubscription>,
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
                            StoreStatus.Active.value -> {
                                activeTransactions.add(storeTransaction)
                                allTransactions.add(storeTransaction)
                            }
                            else -> allTransactions.add(storeTransaction)
                        }
                        getRestorePurchases(storeTransactions, allTransactions, activeTransactions, restorePurchases)
                    }, { _ ->
                        getRestorePurchases(storeTransactions, allTransactions, activeTransactions, restorePurchases)
                    })
                }
            } else {
                completionCallback.onSuccess(emptyList())
            }
        }

        internal fun getRestorePurchases(
            storeTransactions: ArrayList<PurchaseTransaction>,
            allTransactions: ArrayList<PurchaseTransaction>,
            activeTransactions: ArrayList<PurchaseTransaction>,
            restorePurchases: ArrayList<CBRestoreSubscription>
        ) {
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
                    if (CBPurchase.includeInActivePurchases) {
                        completionCallback.onSuccess(restorePurchases)
                        syncPurchaseWithChargebee(allTransactions)
                    } else {
                        completionCallback.onSuccess(activePurchases)
                        syncPurchaseWithChargebee(activeTransactions)
                    }
                }
            } else {
                fetchStoreSubscriptionStatus(storeTransactions,allTransactions, activeTransactions,restorePurchases, completionCallback)
            }
        }

        internal fun syncPurchaseWithChargebee(storeTransactions: ArrayList<PurchaseTransaction>) {
            storeTransactions.forEach { purchaseTransaction ->
                if (purchaseTransaction.productType == ProductType.SUBS.value) {
                    validateReceipt(purchaseTransaction.purchaseToken, purchaseTransaction.productId.first())
                }
            }
        }

        internal fun validateReceipt(purchaseToken: String, productId: String) {
            CBPurchase.validateReceipt(purchaseToken, productId) {
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
    }
}