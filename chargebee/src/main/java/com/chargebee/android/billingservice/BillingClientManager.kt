package com.chargebee.android.billingservice

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode.*
import com.chargebee.android.ErrorDetail
import com.chargebee.android.billingservice.RestoreErrorCode.Companion.throwCBException
import com.chargebee.android.models.PurchaseTransaction
import com.chargebee.android.exceptions.CBException
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.models.CBProduct
import com.chargebee.android.network.CBReceiptResponse
import com.chargebee.android.restore.CBRestorePurchaseManager
import kotlin.collections.ArrayList

class BillingClientManager : BillingClientStateListener, PurchasesUpdatedListener {

    private val CONNECT_TIMER_START_MILLISECONDS = 1L * 1000L
    internal var billingClient: BillingClient? = null
    var mContext: Context? = null
    private val handler = Handler(Looper.getMainLooper())
    private var skuType: String? = null
    private var skuList = arrayListOf<String>()
    private lateinit var callBack: CBCallback.ListProductsCallback<ArrayList<CBProduct>>
    private var purchaseCallBack: CBCallback.PurchaseCallback<String>? = null
    private val skusWithSkuDetails = arrayListOf<CBProduct>()
    private val TAG = javaClass.simpleName
    lateinit var product: CBProduct
    private lateinit var completionCallback: CBCallback.RestorePurchaseCallback

    constructor(
        context: Context, skuType: String,
        skuList: ArrayList<String>, callBack: CBCallback.ListProductsCallback<ArrayList<CBProduct>>
    ) {
        mContext = context
        this.skuList = skuList
        this.skuType = skuType
        this.callBack = callBack
        startBillingServiceConnection()

    }

    constructor(context: Context) {
        this.mContext = context
    }

    /* Called to notify that the connection to the billing service was lost*/
    override fun onBillingServiceDisconnected() {
        connectToBillingService()
    }

    /* The listener method will be called when the billing client setup process complete */
    override fun onBillingSetupFinished(billingResult: BillingResult) {
        when (billingResult.responseCode) {
            OK -> {
                Log.i(
                    TAG,
                    "Google Billing Setup Done!"
                )
                loadProductDetails(BillingClient.SkuType.SUBS, skuList, callBack)
            }
            FEATURE_NOT_SUPPORTED,
            BILLING_UNAVAILABLE -> {
                callBack.onError(
                    CBException(
                        ErrorDetail(
                            message = GPErrorCode.BillingUnavailable.errorMsg,
                            httpStatusCode = billingResult.responseCode
                        )
                    )
                )
                Log.i(TAG, "onBillingSetupFinished() -> with error: ${billingResult.debugMessage}")
            }
            SERVICE_DISCONNECTED,
            USER_CANCELED,
            SERVICE_UNAVAILABLE,
            ITEM_UNAVAILABLE,
            ERROR,
            ITEM_ALREADY_OWNED,
            SERVICE_TIMEOUT,
            ITEM_NOT_OWNED -> {
                Log.i(
                    TAG,
                    "onBillingSetupFinished() -> google billing client error: ${billingResult.debugMessage}"
                )
            }
            DEVELOPER_ERROR -> {
                Log.i(
                    TAG,
                    "onBillingSetupFinished() -> Client is already in the process of connecting to billing service"
                )

            }
            else -> {
                Log.i(TAG, "onBillingSetupFinished -> with error: ${billingResult.debugMessage}.")
            }
        }
    }

    /* Method used to configure and create a instance of billing client */
    private fun startBillingServiceConnection() {
        buildBillingClient(this)
        connectToBillingService()
    }

    /* Connect the billing client service */
    private fun connectToBillingService() {
        if (billingClient?.isReady == false) {
            handler.postDelayed(
                { billingClient?.startConnection(this@BillingClientManager) },
                CONNECT_TIMER_START_MILLISECONDS
            )
        }
    }

    /* Get the SKU/Products from Play Console */
    private fun loadProductDetails(
        @BillingClient.SkuType skuType: String,
        skuList: ArrayList<String>, callBack: CBCallback.ListProductsCallback<ArrayList<CBProduct>>
    ) {
        try {
            val params = SkuDetailsParams
                .newBuilder()
                .setSkusList(skuList)
                .setType(skuType)
                .build()

            billingClient?.querySkuDetailsAsync(
                params
            ) { billingResult, skuDetailsList ->
                if (billingResult.responseCode == OK && skuDetailsList != null) {
                    try {
                        skusWithSkuDetails.clear()
                        for (skuProduct in skuDetailsList) {
                            val product = CBProduct(
                                skuProduct.sku,
                                skuProduct.title,
                                skuProduct.price,
                                skuProduct,
                                false
                            )
                            skusWithSkuDetails.add(product)
                        }
                        Log.i(TAG, "Product details :$skusWithSkuDetails")
                        callBack.onSuccess(productIDs = skusWithSkuDetails)
                    } catch (ex: CBException) {
                        callBack.onError(
                            CBException(
                                ErrorDetail(
                                    message = "Error while parsing data",
                                    httpStatusCode = billingResult.responseCode
                                )
                            )
                        )
                        Log.e(TAG, "exception :" + ex.message)
                    }
                } else {
                    Log.e(TAG, "Response Code :" + billingResult.responseCode)
                    callBack.onError(
                        CBException(
                            ErrorDetail(
                                message = GPErrorCode.PlayServiceUnavailable.errorMsg,
                                httpStatusCode = billingResult.responseCode
                            )
                        )
                    )
                }
            }
        } catch (exp: CBException) {
            Log.e(TAG, "exception :$exp.message")
            callBack.onError(CBException(ErrorDetail(message = "${exp.message}")))
        }

    }

    /* Purchase the product: Initiates the billing flow for an In-app-purchase  */
    fun purchase(
        product: CBProduct,
        purchaseCallBack: CBCallback.PurchaseCallback<String>
    ) {
        this.purchaseCallBack = purchaseCallBack
        this.product = product
        val skuDetails = product.skuDetails

        val params = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails)
            .build()

        billingClient?.launchBillingFlow(mContext as Activity, params)
            .takeIf { billingResult ->
                billingResult?.responseCode != OK
            }?.let { billingResult ->
                Log.e(TAG, "Failed to launch billing flow $billingResult")
                purchaseCallBack.onError(
                    CBException(
                        ErrorDetail(
                            message = GPErrorCode.LaunchBillingFlowError.errorMsg,
                            httpStatusCode = billingResult.responseCode
                        )
                    )
                )
            }
    }

    /**
     * This method will provide all the purchases associated with the current account based on the [includeInActivePurchases] flag set.
     * And the associated purchases will be synced with Chargebee.
     *
     * @param [completionCallback] The listener will be called when restore purchase completes.
     */
    internal fun restorePurchases(completionCallback: CBCallback.RestorePurchaseCallback) {
        this.completionCallback = completionCallback
        onConnected({ status ->
            queryPurchaseHistoryFromStore(status)
        }, { error ->
            completionCallback.onError(error)
        })
    }

    /* Checks if the specified feature is supported by the Play Store */
    fun isFeatureSupported(): Boolean {
        try {
            val featureSupportedResult =
                billingClient?.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS)
            when (featureSupportedResult?.responseCode) {
                OK -> {
                    return true
                }
                FEATURE_NOT_SUPPORTED -> {
                    return false
                }
            }
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Play Services not available ")
        }
        return false
    }

    /* Checks if the billing client connected to the service */
    fun isBillingClientReady(): Boolean? {
        return billingClient?.isReady
    }

    /* Google Play calls this method to deliver the result of the Purchase Process/Operation */
    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        when (billingResult.responseCode) {
            OK -> {
                purchases?.forEach { purchase ->
                    when (purchase.purchaseState) {
                        Purchase.PurchaseState.PURCHASED -> {
                            acknowledgePurchase(purchase)
                        }
                        Purchase.PurchaseState.PENDING -> {
                            purchaseCallBack?.onError(
                                CBException(
                                    ErrorDetail(
                                        message = GPErrorCode.PurchasePending.errorMsg,
                                        httpStatusCode = billingResult.responseCode
                                    )
                                )
                            )
                        }
                        Purchase.PurchaseState.UNSPECIFIED_STATE -> {
                            purchaseCallBack?.onError(
                                CBException(
                                    ErrorDetail(
                                        message = GPErrorCode.PurchaseUnspecified.errorMsg,
                                        httpStatusCode = billingResult.responseCode
                                    )
                                )
                            )
                        }
                    }
                }
            }
            ITEM_ALREADY_OWNED -> {
                Log.e(TAG, "Billing response code : ITEM_ALREADY_OWNED")
                purchaseCallBack?.onError(
                    CBException(
                        ErrorDetail(
                            message = GPErrorCode.ProductAlreadyOwned.errorMsg,
                            httpStatusCode = billingResult.responseCode
                        )
                    )
                )
            }
            SERVICE_DISCONNECTED -> {
                connectToBillingService()
            }
            ITEM_UNAVAILABLE -> {
                Log.e(TAG, "Billing response code : ITEM_UNAVAILABLE")
                purchaseCallBack?.onError(
                    CBException(
                        ErrorDetail(
                            message = GPErrorCode.ProductUnavailable.errorMsg,
                            httpStatusCode = billingResult.responseCode
                        )
                    )
                )
            }
            USER_CANCELED -> {
                Log.e(TAG, "Billing response code  : USER_CANCELED ")
                purchaseCallBack?.onError(
                    CBException(
                        ErrorDetail(
                            message = GPErrorCode.CanceledPurchase.errorMsg,
                            httpStatusCode = billingResult.responseCode
                        )
                    )
                )
            }
            ITEM_NOT_OWNED -> {
                Log.e(TAG, "Billing response code  : ITEM_NOT_OWNED ")
                purchaseCallBack?.onError(
                    CBException(
                        ErrorDetail(
                            message = GPErrorCode.ProductNotOwned.errorMsg,
                            httpStatusCode = billingResult.responseCode
                        )
                    )
                )
            }
            SERVICE_TIMEOUT -> {
                Log.e(TAG, "Billing response code :SERVICE_TIMEOUT ")
                purchaseCallBack?.onError(
                    CBException(
                        ErrorDetail(
                            message = GPErrorCode.PlayServiceTimeOut.errorMsg,
                            httpStatusCode = billingResult.responseCode
                        )
                    )
                )
            }
            SERVICE_UNAVAILABLE -> {
                Log.e(TAG, "Billing response code: SERVICE_UNAVAILABLE")
                purchaseCallBack?.onError(
                    CBException(
                        ErrorDetail(
                            message = GPErrorCode.PlayServiceUnavailable.errorMsg,
                            httpStatusCode = billingResult.responseCode
                        )
                    )
                )
            }
            ERROR -> {
                Log.e(TAG, "Billing response code: ERROR")
                purchaseCallBack?.onError(
                    CBException(
                        ErrorDetail(
                            message = GPErrorCode.UnknownError.errorMsg,
                            httpStatusCode = billingResult.responseCode
                        )
                    )
                )
            }
            DEVELOPER_ERROR -> {
                Log.e(TAG, "Billing response code: DEVELOPER_ERROR")
                purchaseCallBack?.onError(
                    CBException(
                        ErrorDetail(
                            message = GPErrorCode.DeveloperError.errorMsg,
                            httpStatusCode = billingResult.responseCode
                        )
                    )
                )
            }
            BILLING_UNAVAILABLE -> {
                Log.e(TAG, "Billing response code: BILLING_UNAVAILABLE")
                purchaseCallBack?.onError(
                    CBException(
                        ErrorDetail(
                            message = GPErrorCode.BillingUnavailable.errorMsg,
                            httpStatusCode = billingResult.responseCode
                        )
                    )
                )
            }
            FEATURE_NOT_SUPPORTED -> {
                Log.e(TAG, "Billing response code: FEATURE_NOT_SUPPORTED")
                purchaseCallBack?.onError(
                    CBException(
                        ErrorDetail(
                            message = GPErrorCode.FeatureNotSupported.errorMsg,
                            httpStatusCode = billingResult.responseCode
                        )
                    )
                )
            }
        }
    }

    /* Acknowledge the Purchases */
    private fun acknowledgePurchase(purchase: Purchase) {
        if (!purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient?.acknowledgePurchase(params) { billingResult ->
                if (billingResult.responseCode == OK) {
                    try {
                        if (purchase.purchaseToken.isEmpty()) {
                            Log.e(TAG, "Receipt Not Found")
                            purchaseCallBack?.onError(
                                CBException(
                                    ErrorDetail(
                                        message = GPErrorCode.PurchaseReceiptNotFound.errorMsg,
                                        httpStatusCode = billingResult.responseCode
                                    )
                                )
                            )
                        } else {
                            Log.i(TAG, "Google Purchase - success")
                            Log.i(TAG, "Purchase Token -${purchase.purchaseToken}")
                            validateReceipt(purchase.purchaseToken, product)
                        }

                    } catch (ex: CBException) {
                        Log.e("Error", ex.toString())
                        purchaseCallBack?.onError(CBException(ErrorDetail(message = ex.message)))
                    }
                }
            }
        }
    }

    /* Chargebee method called here to validate receipt */
    private fun validateReceipt(purchaseToken: String, product: CBProduct) {
        try {
            CBPurchase.validateReceipt(purchaseToken, product) {
                when (it) {
                    is ChargebeeResult.Success -> {
                        Log.i(
                            TAG,
                            "Validate Receipt Response:  ${(it.data as CBReceiptResponse).in_app_subscription}"
                        )
                        if (it.data.in_app_subscription != null) {
                            val subscriptionId = (it.data).in_app_subscription.subscription_id
                            Log.i(TAG, "Subscription ID:  $subscriptionId")
                            val subscriptionResult = (it.data).in_app_subscription
                            if (subscriptionId.isEmpty()) {
                                purchaseCallBack?.onSuccess(subscriptionResult, false)
                            } else {
                                purchaseCallBack?.onSuccess(subscriptionResult, true)
                            }
                        } else {
                            purchaseCallBack?.onError(CBException(ErrorDetail(message = GPErrorCode.PurchaseInvalid.errorMsg)))
                        }
                    }
                    is ChargebeeResult.Error -> {
                        Log.e(TAG, "Exception from Server - validateReceipt() :  ${it.exp.message}")
                        purchaseCallBack?.onError(it.exp)
                    }
                }
            }
        } catch (exp: Exception) {
            Log.e(TAG, "Exception from Server- validateReceipt() :  ${exp.message}")
            purchaseCallBack?.onError(CBException(ErrorDetail(message = exp.message)))
        }
    }

    private val connectionError = CBException(
        ErrorDetail(
            message = RestoreErrorCode.SERVICE_UNAVAILABLE.name,
            httpStatusCode = RestoreErrorCode.SERVICE_UNAVAILABLE.code
        )
    )

    private fun queryPurchaseHistoryFromStore(
        connectionStatus: Boolean
    ) {
        if (connectionStatus) {
            queryPurchaseHistory { purchaseHistoryList ->
                val storeTransactions = arrayListOf<PurchaseTransaction>()
                storeTransactions.addAll(purchaseHistoryList)
                CBRestorePurchaseManager.fetchStoreSubscriptionStatus(
                    storeTransactions,
                    completionCallback
                )
            }
        } else {
            completionCallback.onError(
                connectionError
            )
        }
    }

    private fun queryPurchaseHistory(
        storeTransactions: (List<PurchaseTransaction>) -> Unit
    ) {
        queryAllSubsPurchaseHistory(CBPurchase.ProductType.SUBS.value) { subscriptionHistory ->
            queryAllInAppPurchaseHistory(CBPurchase.ProductType.INAPP.value) { inAppHistory ->
                val purchaseTransactionHistory = inAppHistory?.let {
                    subscriptionHistory?.plus(it)
                }
                storeTransactions(purchaseTransactionHistory ?: emptyList())
            }
        }
    }

    private fun queryAllSubsPurchaseHistory(
        productType: String, purchaseTransactionList: (List<PurchaseTransaction>?) -> Unit
    ) {
        queryPurchaseHistoryAsync(productType) {
            purchaseTransactionList(it)
        }
    }

    private fun queryAllInAppPurchaseHistory(
        productType: String, purchaseTransactionList: (List<PurchaseTransaction>?) -> Unit
    ) {
        queryPurchaseHistoryAsync(productType) {
            purchaseTransactionList(it)
        }
    }

    private fun queryPurchaseHistoryAsync(
        productType: String, purchaseTransactionList: (List<PurchaseTransaction>?) -> Unit
    ) {
        billingClient?.queryPurchaseHistoryAsync(productType) { billingResult, subsHistoryList ->
            if (billingResult.responseCode == OK) {
                val purchaseHistoryList = subsHistoryList?.map {
                    it.toPurchaseTransaction(productType)
                }
                purchaseTransactionList(purchaseHistoryList)
            } else {
                completionCallback.onError(throwCBException(billingResult))
            }
        }
    }

    private fun PurchaseHistoryRecord.toPurchaseTransaction(productType: String): PurchaseTransaction {
        return PurchaseTransaction(
            productId = this.skus,
            productType = productType,
            purchaseTime = this.purchaseTime,
            purchaseToken = this.purchaseToken
        )
    }

    private fun buildBillingClient(listener: PurchasesUpdatedListener): BillingClient? {
        if (billingClient == null) {
            billingClient = mContext?.let {
                BillingClient.newBuilder(it).enablePendingPurchases().setListener(listener)
                    .build()
            }
        }
        return billingClient
    }

    private fun onConnected(status: (Boolean) -> Unit, connectionError: (CBException) -> Unit) {
        val billingClient = buildBillingClient(this)
        if (billingClient?.isReady == false) {
            handler.postDelayed({
                billingClient.startConnection(
                    createBillingClientStateListener(status, connectionError)
                )
            }, CONNECT_TIMER_START_MILLISECONDS)
        } else status(true)
    }

    private fun createBillingClientStateListener(
        status: (Boolean) -> Unit,
        connectionError: (CBException) -> Unit
    ) = object :
        BillingClientStateListener {
        override fun onBillingServiceDisconnected() {
            Log.i(javaClass.simpleName, "onBillingServiceDisconnected")
            status(false)
        }

        override fun onBillingSetupFinished(billingResult: BillingResult) {
            when (billingResult.responseCode) {
                OK -> {
                    Log.i(TAG, "Google Billing Setup Done!")
                    status(true)
                }
                else -> {
                    connectionError(throwCBException(billingResult))
                }
            }
        }
    }
}
