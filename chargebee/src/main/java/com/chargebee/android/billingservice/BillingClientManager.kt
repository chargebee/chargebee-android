package com.chargebee.android.billingservice

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import com.android.billingclient.api.*
import com.chargebee.android.ErrorDetail
import com.chargebee.android.exceptions.CBException
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.models.CBProduct
import com.chargebee.android.network.CBReceiptResponse
import java.util.*

class BillingClientManager constructor(
    context: Context, skuType: String,
    skuList: ArrayList<String>, callBack: CBCallback.ListProductsCallback<ArrayList<CBProduct>>
) : BillingClientStateListener, PurchasesUpdatedListener {

    private val CONNECT_TIMER_START_MILLISECONDS = 1L * 1000L
    lateinit var billingClient: BillingClient
    var mContext : Context? = null
    private val handler = Handler(Looper.getMainLooper())
    private var skuType : String? = null
    private var skuList = arrayListOf<String>()
    private var callBack : CBCallback.ListProductsCallback<ArrayList<CBProduct>>
    private var purchaseCallBack: CBCallback.PurchaseCallback<String>? = null
    private val skusWithSkuDetails = arrayListOf<CBProduct>()
    private val TAG = "BillingClientManager"
    var customerID : String = ""
    lateinit var product: CBProduct
    var oldPurchaseToken: String? = null
    lateinit var newSkuDetails: SkuDetails

    init {
        mContext = context
        this.skuList = skuList
        this.skuType =skuType
        this.callBack = callBack
        startBillingServiceConnection()

    }

    /* Called to notify that the connection to the billing service was lost*/
    override fun onBillingServiceDisconnected() {
        connectToBillingService()
    }

    /* The listener method will be called when the billing client setup process complete */
    override fun onBillingSetupFinished(billingResult: BillingResult) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                Log.i(
                    TAG,
                    "onBillingSetupFinished() -> successfully for ${billingClient.toString()}."
                )
                loadProductDetails(BillingClient.SkuType.SUBS, skuList, callBack)
            }
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED,
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
                callBack.onError(CBException(ErrorDetail(GPErrorCode.BillingUnavailable.errorMsg)))
                Log.i(TAG, "onBillingSetupFinished() -> with error: ${billingResult.debugMessage}")
            }
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
            BillingClient.BillingResponseCode.USER_CANCELED,
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE,
            BillingClient.BillingResponseCode.ERROR,
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED,
            BillingClient.BillingResponseCode.SERVICE_TIMEOUT,
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> {
                Log.i(
                    TAG,
                    "onBillingSetupFinished() -> google billing client error: ${billingResult.debugMessage}"
                )
            }
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> {
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
        billingClient = mContext?.let {
            BillingClient.newBuilder(it)
                .enablePendingPurchases()
                .setListener(this).build()
        }!!

        connectToBillingService()
    }
    /* Connect the billing client service */
    private fun connectToBillingService() {
        if (!billingClient.isReady) {
            handler.postDelayed(
                { billingClient.startConnection(this@BillingClientManager) },
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

           // queryAllPurchases()

           billingClient.querySkuDetailsAsync(
               params
           ) { billingResult, skuDetailsList ->
               if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
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
                   }catch (ex: CBException){
                       callBack.onError(CBException(ErrorDetail(GPErrorCode.UnknownError.errorMsg)))
                       Log.e(TAG, "exception :" + ex.message)
                   }
               }else{
                   Log.e(TAG, "Response Code :" + billingResult.responseCode)
                   callBack.onError(CBException(ErrorDetail("Service Unavailable")))
               }
           }
       }catch (exp: CBException){
           Log.e(TAG, "exception :$exp.message")
           callBack.onError(CBException(ErrorDetail("${exp.message}")))
       }
    }

    /* Purchase the product: Initiates the billing flow for an In-app-purchase  */
    fun purchase(
        product: CBProduct,
        customerID: String = "",
        purchaseCallBack: CBCallback.PurchaseCallback<String>
    ) {
        this.purchaseCallBack = purchaseCallBack
        this.product = product
        val skuDetails = product.skuDetails

        if (!(TextUtils.isEmpty(customerID))) {
            this.customerID = customerID
        }
        val params = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails)
            .build()

        billingClient.launchBillingFlow(mContext as Activity, params)
            .takeIf { billingResult -> billingResult.responseCode != BillingClient.BillingResponseCode.OK
            }?.let { billingResult ->
                Log.e(TAG, "Failed to launch billing flow $billingResult")
            }
    }

    /* Checks if the specified feature is supported by the Play Store */
    fun isFeatureSupported(): Boolean {
        try {
            val featureSupportedResult = billingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS)
            when(featureSupportedResult.responseCode){
                BillingClient.BillingResponseCode.OK -> {
                    return true
                }
                BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> {
                    return false
                }
            }
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Play Services not available ")
        }
        return false
    }

    /* Checks if the billing client connected to the service */
    fun isBillingClientReady(): Boolean{
        return billingClient.isReady
    }

    /* Google Play calls this method to deliver the result of the Purchase Process/Operation */
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    when (purchase.purchaseState) {
                        Purchase.PurchaseState.PURCHASED -> {
                            acknowledgePurchase(purchase)
                        }
                        Purchase.PurchaseState.PENDING -> {
                            purchaseCallBack?.onError(CBException(ErrorDetail(GPErrorCode.PurchasePending.errorMsg)))
                        }
                        Purchase.PurchaseState.UNSPECIFIED_STATE -> {
                            purchaseCallBack?.onError(CBException(ErrorDetail(GPErrorCode.PurchaseUnspecified.errorMsg)))
                        }
                    }
                }
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Log.e(TAG, "onPurchasesUpdated: ITEM_ALREADY_OWNED")
                purchaseCallBack?.onError(CBException(ErrorDetail(GPErrorCode.ProductAlreadyOwned.errorMsg)))
            }
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
                connectToBillingService()
            }
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> {
                Log.e(TAG, "onPurchasesUpdated: ITEM_UNAVAILABLE")
                purchaseCallBack?.onError(CBException(ErrorDetail(GPErrorCode.ProductUnavailable.errorMsg)))
            }
            BillingClient.BillingResponseCode.USER_CANCELED ->{
                Log.e(TAG, "onPurchasesUpdated : USER_CANCELED ")
                purchaseCallBack?.onError(CBException(ErrorDetail(GPErrorCode.CanceledPurchase.errorMsg)))
            }
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED ->{
                Log.e(TAG, "onPurchasesUpdated : ITEM_NOT_OWNED ")
                purchaseCallBack?.onError(CBException(ErrorDetail(GPErrorCode.ProductNotOwned.errorMsg)))
            }
            else -> {
                Log.e(TAG, "Failed to PurchasesUpdated"+billingResult.responseCode)
                purchaseCallBack?.onError(CBException(ErrorDetail(GPErrorCode.UnknownError.errorMsg)))
            }
        }
    }

    /* Acknowledge the Purchases */
    private fun acknowledgePurchase(purchase: Purchase) {
        if (!purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(params) { billingResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    try {
                        if (purchase.purchaseToken.isEmpty()){
                            Log.e(TAG, "Receipt Not Found")
                            purchaseCallBack?.onError(CBException(ErrorDetail(message = GPErrorCode.PurchaseReceiptNotFound.errorMsg)))
                        }else {
                            Log.i(TAG, "Google Purchase - success")
                            Log.i(TAG, "Purchase Token -${purchase.purchaseToken}")
                            billingClient.endConnection()

                            if(TextUtils.isEmpty(oldPurchaseToken) && oldPurchaseToken ==null){
                                validateReceipt(purchase.purchaseToken, product)
                            }else{
                                purchaseCallBack?.onSuccess(purchase.purchaseToken, true)
                                oldPurchaseToken = null
                            }

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
        try{
        CBPurchase.validateReceipt(purchaseToken, customerID, product) {
            when(it) {
                is ChargebeeResult.Success -> {
                    billingClient.endConnection()
                    Log.i(
                        TAG,
                        "Validate Receipt Response:  ${(it.data as CBReceiptResponse).in_app_subscription}"
                    )
                    if (it.data.in_app_subscription != null){
                        val subscriptionId = (it.data).in_app_subscription.subscription_id
                        Log.i(TAG, "Subscription ID:  $subscriptionId")
                        if (subscriptionId.isEmpty()) {
                            purchaseCallBack?.onSuccess(subscriptionId, false)
                        } else {
                            purchaseCallBack?.onSuccess(subscriptionId, true)
                        }
                    }else{
                        billingClient.endConnection()
                        purchaseCallBack?.onError(CBException(ErrorDetail(GPErrorCode.PurchaseInvalid.errorMsg)))
                    }
                }
                is ChargebeeResult.Error -> {
                    Log.e(TAG, "Exception from Server - validateReceipt() :  ${it.exp.message}")
                    billingClient.endConnection()
                    purchaseCallBack?.onError(CBException(ErrorDetail(it.exp.message)))
                }
            }
        }
        }catch (exp: Exception){
            billingClient.endConnection()
            Log.e(TAG, "Exception from Server- validateReceipt() :  ${exp.message}")
            purchaseCallBack?.onError(CBException(ErrorDetail(exp.message)))
        }
    }

    fun queryAllPurchases(){
        billingClient.queryPurchasesAsync(
            BillingClient.SkuType.SUBS
        ) { billingResult, activeSubsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.i(TAG, "queryAllPurchases  :$activeSubsList")
            } else {
                Log.i(
                    TAG,
                    "queryAllPurchases  :${billingResult.debugMessage}"
                )
            }
        }
    }

    fun queryPurchaseHistory(){
        billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.SUBS){ billingResult, subsHistoryList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.i(TAG, "queryPurchaseHistory  :$subsHistoryList")
            } else {
                Log.i(
                    TAG,
                    "queryPurchaseHistory  :${billingResult.debugMessage}"
                )
            }
        }
    }
    /* Update Purchases on existing plan */
    fun updatePurchaseFlow(context: Context, skuDetails: SkuDetails, oldPurchaseToken: String, updateCallBack : CBCallback.PurchaseCallback<String>) {
        Log.i(TAG, "oldPurchaseToken : $oldPurchaseToken")
        this.purchaseCallBack = updateCallBack
        this.oldPurchaseToken = oldPurchaseToken
        val updateParams = oldPurchaseToken.let {
            BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                .setOldSkuPurchaseToken(it.trim())
                .setReplaceSkusProrationMode(BillingFlowParams.ProrationMode.IMMEDIATE_WITH_TIME_PRORATION)
                .build()
        }

        val billingFlowParams = updateParams.let {
            BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .setSubscriptionUpdateParams(it)
                .build()
        }
        billingClient.launchBillingFlow(mContext as Activity, billingFlowParams)
            .takeIf { billingResult -> billingResult.responseCode != BillingClient.BillingResponseCode.OK
            }?.let { billingResult ->
                Log.e(TAG, "Failed to launch billing flow $billingResult")
            }

    }
    /* This method will handle the Price Change confirmation with Google play */
    fun priceChangeConfirmationFlow(cbProduct: CBProduct, priceChangeCallBack: CBCallback.PriceChangeCallback<String>){
        val priceChangeParams = PriceChangeFlowParams.newBuilder()
            .setSkuDetails(cbProduct.skuDetails)
            .build()
        billingClient.launchPriceChangeConfirmationFlow(mContext as Activity, priceChangeParams) {
                billingResult ->
            when(billingResult.responseCode){
                BillingClient.BillingResponseCode.OK ->{
                    Log.i(TAG, "User has accepted/Confirmed Price change")
                    Log.i(TAG, billingResult.debugMessage)
                    if (billingResult.debugMessage.isEmpty())
                        priceChangeCallBack.onError(CBException(ErrorDetail("Success")))
                    else
                        priceChangeCallBack.onError(CBException(ErrorDetail(billingResult.debugMessage)))
                }
                BillingClient.BillingResponseCode.USER_CANCELED,
                BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
                BillingClient.BillingResponseCode.SERVICE_TIMEOUT,
                BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
                BillingClient.BillingResponseCode.DEVELOPER_ERROR,
                BillingClient.BillingResponseCode.ERROR ->{
                    if (billingResult.debugMessage.isEmpty())
                        priceChangeCallBack.onError(CBException(ErrorDetail("Error from Google Play Library")))
                    else
                        priceChangeCallBack.onError(CBException(ErrorDetail(billingResult.debugMessage)))
                }
            }

        }
    }
}
