package com.chargebee.android.billingservice

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.android.billingclient.api.*
import com.chargebee.android.ErrorDetail
import com.chargebee.android.ProgressBarListener
import com.chargebee.android.exceptions.CBException
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.models.CBProduct
import com.chargebee.android.network.CBReceiptResponse
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
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
    private var callBack : CBCallback.ListProductsCallback<ArrayList<CBProduct>>? = null
    private var purchaseCallBack: CBCallback.PurchaseCallback<String>? = null
    private val skusWithSkuDetails = arrayListOf<CBProduct>()
    private val TAG = "BillingClientManager"
    var customerID : String = ""
    var product: CBProduct? = null
    lateinit var offerToken: String
   companion object {
       lateinit var mProgressBarListener: Any
   }

    var mProgressBarListener: ProgressBarListener? = null

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
                callBack?.let { loadProductDetails(BillingClient.SkuType.SUBS, skuList, it) }
            }
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED,
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
                callBack?.onError(CBException(ErrorDetail(GPErrorCode.BillingUnavailable.errorMsg)))
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
                // Client is already in the process of connecting to billing service
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
    fun startBillingServiceConnection() {
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

    fun queryProductDetails(skuList: ArrayList<String>) {
        val params = QueryProductDetailsParams.newBuilder()
        val productList = mutableListOf<QueryProductDetailsParams.Product>()

        for (product in skuList) {

            productList.add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(product)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )

            params.setProductList(productList).let { productDetailsParams ->
                Log.i(TAG, "queryProductDetailsAsync")

                billingClient.queryProductDetailsAsync(productDetailsParams.build()){
                     billingResult, productDetailsList ->
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            try {
                                skusWithSkuDetails.clear()
                                for (skuProduct in productDetailsList) {
                                    val cbProductJson =  Gson().toJson(skuProduct)
                                    Log.i(TAG, "skuProduct :$skuProduct")
                                    var price: String =""
                                    if(skuProduct !=null && skuProduct.subscriptionOfferDetails!!.size>0 && skuProduct.subscriptionOfferDetails!!.get(0).pricingPhases.pricingPhaseList.size>0){
                                        price = skuProduct.subscriptionOfferDetails!!.get(0).pricingPhases.pricingPhaseList.get(0).formattedPrice
                                    }

                                    val product = CBProduct(
                                            skuProduct.productId,
                                            skuProduct.title,
                                            price,
                                            skuProduct
                                        )

                                    skusWithSkuDetails.add(product)

                                }

                                Log.i(TAG, "Product details :$skusWithSkuDetails")
                                callBack?.onSuccess(productIDs = skusWithSkuDetails)
                            }catch (ex: CBException){
                                callBack?.onError(CBException(ErrorDetail("Unknown error")))
                                Log.e(TAG, "exception :" + ex.message)
                            }
                        }else{
                            Log.e(TAG, "Response Code :" + billingResult.responseCode)
                            callBack?.onError(CBException(ErrorDetail("Service Unavailable")))
                        }

                }
            }
        }
    }

    /* Get the SKU/Products from Play Console */
    private fun loadProductDetails(
        @BillingClient.SkuType skuType: String,
        skuList: ArrayList<String>, callBack: CBCallback.ListProductsCallback<ArrayList<CBProduct>>
    ) {
       try {
           queryProductDetails(skuList)

//           val params = SkuDetailsParams
//               .newBuilder()
//               .setSkusList(skuList)
//               .setType(skuType)
//               .build()
//
//           queryAllPurchases()
//
//           billingClient.querySkuDetailsAsync(
//               params
//           ) { billingResult, skuDetailsList ->
//               if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
//                   try {
//                       skusWithSkuDetails.clear()
//                       for (skuProduct in skuDetailsList) {
//                           val product = CBProduct(
//                               skuProduct.sku,
//                               skuProduct.title,
//                               skuProduct.price,
//                               skuProduct,
//                               false
//                           )
//                           skusWithSkuDetails.add(product)
//                       }
//                       Log.i(TAG, "Product details :$skusWithSkuDetails")
//                       callBack.onSuccess(productIDs = skusWithSkuDetails)
//                   }catch (ex: CBException){
//                       callBack.onError(CBException(ErrorDetail("Unknown error")))
//                       Log.e(TAG, "exception :" + ex.message)
//                   }
//               }else{
//                   Log.e(TAG, "Response Code :" + billingResult.responseCode)
//                   callBack.onError(CBException(ErrorDetail("Service Unavailable")))
//               }
//           }
       }catch (exp: CBException){
           Log.e(TAG, "exception :$exp.message")
           callBack.onError(CBException(ErrorDetail("failed")))
       }

    }

    /* Purchase the product: Initiates the billing flow for an In-app-purchase  */
    fun purchase(
        product: CBProduct,
        customerID: String? = "",
        purchaseCallBack: CBCallback.PurchaseCallback<String>
    ) {
        this.purchaseCallBack = purchaseCallBack
        this.product = product
        try {
            if (product.productDetails.subscriptionOfferDetails != null && product.productDetails.subscriptionOfferDetails!!.size > 0) {
                val offerDetails: LinkedTreeMap<String, String> = product.productDetails.subscriptionOfferDetails!![0] as LinkedTreeMap<String, String>
                Log.i(
                    TAG,
                    "offerDetails: $offerDetails "
                )
                offerToken = offerDetails.get("offerToken") as String

                Log.i(
                    TAG,
                    "offerToken: $offerToken "
                )

            }
        }catch (e: Exception){
            Log.i(
                TAG,
                "Exception : ${e.stackTrace} "
            )
        }
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(product.productDetails)
                .setOfferToken(offerToken)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList).build()

        billingClient.launchBillingFlow(mContext as Activity, billingFlowParams)
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
                            purchaseCallBack?.onError(CBException(ErrorDetail("Your purchase is pending state, you need to complete it from store")))
                        }
                    }
                }
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                mProgressBarListener?.onHideProgressBar()
                // call queryPurchases to verify and process all owned items
                Log.e(TAG, "onPurchasesUpdated ITEM_ALREADY_OWNED")
                purchaseCallBack?.onError(CBException(ErrorDetail("Item already owned")))
            }
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
                mProgressBarListener?.onHideProgressBar()
                connectToBillingService()
            }
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> {
                Log.e(TAG, "onPurchasesUpdated ITEM_UNAVAILABLE")
                purchaseCallBack?.onError(CBException(ErrorDetail("Item Unavailable")))
            }
            else -> {
                Log.e(TAG, "Failed to onPurchasesUpdated"+billingResult.responseCode)
                mProgressBarListener?.onHideProgressBar()
                purchaseCallBack?.onError(CBException(ErrorDetail("Unknown error")))
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
                    mProgressBarListener?.onShowProgressBar()
                    try {
                        if (purchase.purchaseToken.isEmpty()){
                            Log.i(TAG, "Receipt Not Found")
                            mProgressBarListener?.onHideProgressBar()
                        }else {
                            Log.i(TAG, "Google Purchase - success")
                            product?.let { validateReceipt(purchase.purchaseToken, it) }
                        }

                    } catch (ex: CBException) {
                        mProgressBarListener?.onHideProgressBar()
                        Log.e("Error", ex.toString())
                        purchaseCallBack?.onError(ex)
                    }
                }
            }
        }

    }

    /* Chargebee method called here to validate receipt */
    private fun validateReceipt(purchaseToken: String, product: CBProduct) {
        CBPurchase.validateReceipt(purchaseToken, product){
            when(it){
                is ChargebeeResult.Success -> {
                    Log.i(
                        TAG,
                        "Validate Receipt Response:  ${(it.data as CBReceiptResponse).in_app_subscription}"
                    )
                    val subscriptionId = (it.data).in_app_subscription.subscription_id
                    Log.i(TAG, "Subscription ID:  $subscriptionId")
                    if (subscriptionId.isEmpty()){
                        purchaseCallBack?.onError(CBException(ErrorDetail(message = "Invalid Purchase")))
                        purchaseCallBack?.onSuccess(subscriptionId,false)
                    }else {
                        purchaseCallBack?.onSuccess(subscriptionId,true)
                    }
                }
                is ChargebeeResult.Error -> {
                    mProgressBarListener?.onHideProgressBar()
                    Log.e(TAG, "Exception from server - validateReceipt() :  ${it.exp.message}")
                    purchaseCallBack?.onError(CBException(ErrorDetail(it.exp.message)))
                }
            }
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
}
