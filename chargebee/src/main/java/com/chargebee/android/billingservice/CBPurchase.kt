package com.chargebee.android.billingservice

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.chargebee.android.Chargebee
import com.chargebee.android.ErrorDetail
import com.chargebee.android.exceptions.CBException
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.exceptions.InvalidRequestException
import com.chargebee.android.exceptions.OperationFailedException
import com.chargebee.android.loggers.CBLogger
import com.chargebee.android.models.Products
import com.chargebee.android.models.ResultHandler
import com.chargebee.android.network.CBAuthentication
import com.chargebee.android.network.Params
import com.chargebee.android.resources.ReceiptResource
import okhttp3.Credentials
import java.util.ArrayList
object CBPurchase {

    var billingClientManager: BillingClientManager? = null
    val SUBS_IDS = arrayListOf("gaurav_test", "cb_weekly_premium", "test_123_gaurav", "test_weekly_premium")
    var price: String = ""

    annotation class SkuType {
        companion object {
            var INAPP = "inapp"
            var SUBS = "subs"
        }
    }
    @JvmStatic
    fun retrieveProductIDs(context: Context, callBack : CBCallback.ListProductIDsCallback<ArrayList<String>>) {
        callBack.onSuccess(SUBS_IDS)
    }
    @JvmStatic
    fun retrieveProducts(context: Context, params: ArrayList<String>, callBack : CBCallback.ListProductsCallback<ArrayList<Products>>) {
        try {
            billingClientManager = BillingClientManager(context,SkuType.SUBS, params, callBack)
           /* GlobalScope.launch {
                billingClientManager?.loadProductDetails(SkuType.SUBS, params, callBack)
            }*/
        }catch (ex: CBException){
            callBack.onError(ex)
        }
    }
    @JvmStatic
    fun purchaseProduct(
        params: Products,
        callback: CBCallback.PurchaseCallback<PurchaseModel>) {
        if (!TextUtils.isEmpty(Chargebee.sdkKey)){
            CBAuthentication.isSDKKeyValid(Chargebee.sdkKey){
                when(it){
                    is ChargebeeResult.Success -> {
                        if (billingClientManager?.isBillingClientReady() == true && billingClientManager?.isFeatureSupported() == true) {
                            billingClientManager?.purchase(params, callback)
                        } else {
                            callback.onError(CBException(ErrorDetail("Play services not available")))
                        }
                    }
                    is ChargebeeResult.Error ->{
                        Log.i(javaClass.simpleName, "Exception from server :${it.exp.message}")
                        callback.onError(CBException(ErrorDetail(it.exp.message)))
                    }
                }
            }
        }else{
            Log.i(javaClass.simpleName, "SDK key not available to proceed purchase")
            callback.onError(CBException(ErrorDetail("SDK key not available to proceed purchase")))
        }
    }

    @JvmStatic
    @Throws(InvalidRequestException::class, OperationFailedException::class)
    fun validateReceipt(purchaseToken: String, products: Products, completion : (ChargebeeResult<Any>) -> Unit) {
        try {
            val logger = CBLogger(name = "receipt", action = "validate_receipt")
            price = products.productPrice.drop(1).dropLast(2).replace(".","").replace(",","")

            val params = Params(
                purchaseToken,
                products.productId,
                price,
                products.skuDetails.priceCurrencyCode,
                Chargebee.site,
                Chargebee.channel
            )

            ResultHandler.safeExecuter(
                { ReceiptResource().validateReceipt(params) },
                completion,
                logger
            )
        }catch (exp: Exception){
            Log.e(javaClass.simpleName, "Exception in validateReceipt() :"+exp.message)
            ChargebeeResult.Error(
                exp = CBException(
                    error = ErrorDetail(
                        exp.message
                    )
                )
            )
        }
    }
    @JvmStatic
    @Throws(InvalidRequestException::class, OperationFailedException::class)
    fun queryPurchaseHistory() {
        try {
            billingClientManager?.queryPurchaseHistory()
        }catch (exp: Exception){
            Log.i(javaClass.simpleName, "Exception in validateReceipt() :"+exp.message)
            ChargebeeResult.Error(
                exp = CBException(
                    error = ErrorDetail(
                        exp.message
                    )
                )
            )
        }
    }
    @JvmStatic
    @Throws(InvalidRequestException::class, OperationFailedException::class)
    fun queryAllPurchases() {
        try {
            billingClientManager?.queryAllPurchases()
        }catch (exp: Exception){
            Log.i(javaClass.simpleName, "Exception in validateReceipt() :"+exp.message)
            ChargebeeResult.Error(
                exp = CBException(
                    error = ErrorDetail(
                        exp.message
                    )
                )
            )
        }
    }

}