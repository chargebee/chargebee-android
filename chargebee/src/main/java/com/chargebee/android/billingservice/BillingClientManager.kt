package com.chargebee.android.billingservice

import android.app.Activity
import android.content.Context
import android.os.Looper
import android.util.Log
import com.android.billingclient.api.*
import com.chargebee.android.ErrorDetail
import com.chargebee.android.exceptions.CBException
import com.chargebee.android.models.Products

class BillingClientManager constructor(context: Context, skuType: String,
                                       skuList: ArrayList<String>, callBack : CBCallback.ListProductsCallback<ArrayList<Products>>) : BillingClientStateListener, PurchasesUpdatedListener {

    private val CONNECT_TIMER_START_MILLISECONDS = 1L * 1000L
    private lateinit var billingClient: BillingClient
    var mContext : Context? = null
    private val handler = android.os.Handler(Looper.getMainLooper())
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
                Log.i(TAG,"onBillingSetupFinished() -> successfully for ${billingClient?.toString()}.")
                callBack?.let { loadProductDetails(BillingClient.SkuType.SUBS, listIds, it) }
            }
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED,
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
                Log.i(TAG,"onBillingSetupFinished() -> with error: ${billingResult.debugMessage}")
            }
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> {
                // Client is already in the process of connecting to billing service
                Log.i(TAG,"onBillingSetupFinished() -> Client is already in the process of connecting to billing service")

            }
            else -> {
                Log.i(TAG,"onBillingSetupFinished -> with error: ${billingResult.debugMessage}.")
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
        skuList: ArrayList<String>, callBack : CBCallback.ListProductsCallback<ArrayList<Products>>
    ) {
       try {
           val params = SkuDetailsParams
               .newBuilder()
               .setSkusList(skuList)
               .setType(skuType)
               .build()

           billingClient.querySkuDetailsAsync(
               params
           ) { billingResult, skuDetailsList ->
               if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                   try {
                       skusWithSkuDetails.clear()
                       for (details in skuDetailsList) {
                           val products = Products(details.sku,details.title, details.price, details, false)
                           skusWithSkuDetails.add(products)
                       }
                       callBack.onSuccess(productIDs = skusWithSkuDetails)
                   }catch (ex: CBException){
                       callBack.onError(CBException(ErrorDetail("Unknown error")))
                       Log.e(TAG,"exception :"+ex.message)
                   }
               }
           }
       }catch (exp : CBException){
           Log.e(TAG,"exception :$exp.message")
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
            .takeIf {
                    billingResult -> billingResult.responseCode != BillingClient.BillingResponseCode.OK
            }?.let { billingResult ->
                Log.e(TAG, "Failed to launch billing flow $billingResult")
            }

    }
    fun processPurchases(purchases: Set<Purchase>) {
        purchases.forEach { purchase ->
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                // Implement server verification
                // If purchase token is OK, then unlock user access to the content
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        acknowledgePurchase(purchase)
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
                    //val debugMessage = billingResult.debugMessage
                    try {
                        val purchaseModel = PurchaseModel(
                            purchase.purchaseToken,
                            purchase.isAcknowledged,
                            purchase.purchaseTime
                        )
                        purchaseCallBack?.onSuccess(purchaseModel)
                    } catch (ex: CBException) {
                        Log.e("error", ex.toString())
                        purchaseCallBack?.onError(ex)
                    }
                }
            }
        }

    }
}