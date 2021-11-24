package com.chargebee.android.billingservice

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.android.billingclient.api.*
import com.chargebee.android.ErrorDetail
import com.chargebee.android.exceptions.CBException
import com.chargebee.android.models.Products
import java.util.*

class BillingClientManager constructor(
    context: Context, skuType: String,
    skuList: ArrayList<String>, callBack: CBCallback.ListProductsCallback<ArrayList<Products>>
) : BillingClientStateListener, PurchasesUpdatedListener {

    private val CONNECT_TIMER_START_MILLISECONDS = 1L * 1000L
    lateinit var billingClient: BillingClient
    var mContext : Context? = null
    private val handler = Handler(Looper.getMainLooper())
    private var skuType : String? = null
    private var listIds = arrayListOf<String>()
    private var callBack : CBCallback.ListProductsCallback<ArrayList<Products>>? = null
    private var purchaseCallBack: CBCallback.PurchaseCallback<PurchaseModel>? = null
    private val skusWithSkuDetails = arrayListOf<Products>()
    private val TAG = "BillingClientManager"

    init {
        mContext = context
        listIds = skuList
        this.skuType =skuType
        this.callBack = callBack
        startBillingServiceConnection()

    }

    override fun onBillingServiceDisconnected() {
        connectToBillingService()
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                Log.i(
                    TAG,
                    "onBillingSetupFinished() -> successfully for ${billingClient?.toString()}."
                )
                callBack?.let { loadProductDetails(BillingClient.SkuType.SUBS, listIds, it) }
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

    fun startBillingServiceConnection() {
        billingClient = mContext?.let {
            BillingClient.newBuilder(it)
                .enablePendingPurchases()
                .setListener(this).build()
        }!!

        connectToBillingService()
    }
    private fun connectToBillingService() {
        if (!billingClient.isReady) {
            handler.postDelayed(
                { billingClient.startConnection(this@BillingClientManager) },
                CONNECT_TIMER_START_MILLISECONDS
            )
        }
    }

    private fun loadProductDetails(
        @BillingClient.SkuType skuType: String,
        skuList: ArrayList<String>, callBack: CBCallback.ListProductsCallback<ArrayList<Products>>
    ) {
       try {
           val params = SkuDetailsParams
               .newBuilder()
               .setSkusList(skuList)
               .setType(skuType)
               .build()

           queryAllPurchases()

           billingClient.querySkuDetailsAsync(
               params
           ) { billingResult, skuDetailsList ->
               if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                   try {
                       skusWithSkuDetails.clear()
                       for (details in skuDetailsList) {
                           Log.i(TAG, "Product details :$details")
                           val products = Products(
                               details.sku,
                               details.title,
                               details.price,
                               details,
                               false
                           )
                           skusWithSkuDetails.add(products)
                       }
                       callBack.onSuccess(productIDs = skusWithSkuDetails)
                   }catch (ex: CBException){
                       callBack.onError(CBException(ErrorDetail("Unknown error")))
                       Log.e(TAG, "exception :" + ex.message)
                   }
               }
           }
       }catch (exp: CBException){
           Log.e(TAG, "exception :$exp.message")
           callBack.onError(CBException(ErrorDetail("failed")))
       }

    }

    fun purchase(param: Products, purchaseCallBack: CBCallback.PurchaseCallback<PurchaseModel>) {
        this.purchaseCallBack = purchaseCallBack
        val skuDetails = param.skuDetails
        val params = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails)
            .build()

        billingClient.launchBillingFlow(mContext as Activity, params)
            .takeIf { billingResult -> billingResult.responseCode != BillingClient.BillingResponseCode.OK
            }?.let { billingResult ->
                Log.e(TAG, "Failed to launch billing flow $billingResult")
            }

    }

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

    fun isBillingClientReady(): Boolean{
        return billingClient.isReady
    }

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
                // call queryPurchases to verify and process all owned items
                Log.e(TAG, "onPurchasesUpdated ITEM_ALREADY_OWNED")
                purchaseCallBack?.onError(CBException(ErrorDetail("Item already owned")))
            }
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
                connectToBillingService()
            }
            else -> {
                Log.e(TAG, "Failed to onPurchasesUpdated")
                purchaseCallBack?.onError(CBException(ErrorDetail("Unknown error")))
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        if (!purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(params) { billingResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    try {
                        val purchaseModel = purchase.accountIdentifiers?.let {
                            PurchaseModel(
                                purchase.purchaseToken,
                                purchase.isAcknowledged,
                                purchase.purchaseTime,
                                it,
                                purchase.orderId,
                                purchase.isAutoRenewing,
                                purchase.packageName
                            )
                        }
                        if (purchaseModel != null) {
                            purchaseCallBack?.onSuccess(purchaseModel)
                        }
                    } catch (ex: CBException) {
                        Log.e("error", ex.toString())
                        purchaseCallBack?.onError(ex)
                    }
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
