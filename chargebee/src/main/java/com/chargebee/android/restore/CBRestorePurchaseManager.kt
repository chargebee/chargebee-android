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
        internal var allTransactions = ArrayList<PurchaseTransaction>()
        private var restorePurchases = ArrayList<CBRestoreSubscription>()
        internal var activeTransactions = ArrayList<PurchaseTransaction>()
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
            result: (List<CBRestoreSubscription>) -> Unit,
            error: (CBException) -> Unit
        ) {
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
                        getRestorePurchases(storeTransactions, result, error)
                    }, { _ ->
                        getRestorePurchases(storeTransactions, result, error)
                    })
                }
            } else {
                result(emptyList())
            }
        }

        internal fun getRestorePurchases(
            storeTransactions: ArrayList<PurchaseTransaction>,
            result: (List<CBRestoreSubscription>) -> Unit,
            error: (CBException) -> Unit
        ) {
            if (storeTransactions.isEmpty()) {
                if (restorePurchases.isEmpty()) {
                    error(
                        CBException(
                            ErrorDetail(
                                message = GPErrorCode.InvalidPurchaseToken.errorMsg,
                                httpStatusCode = 400
                            )
                        )
                    )
                } else {
                    result(restorePurchases)
                }
                restorePurchases.clear()
                allTransactions.clear()
                activeTransactions.clear()
            } else {
                fetchStoreSubscriptionStatus(storeTransactions, result, error)
            }
        }
    }
}