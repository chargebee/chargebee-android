package com.chargebee.android.billingservice

import android.content.Context
import com.chargebee.android.exceptions.CBException
import com.chargebee.android.models.Products
import java.util.ArrayList

object CBPurchase {

    var billingClientManager: BillingClientManager? = null
    val SUBS_IDS = arrayListOf("gaurav_test", "cb_weekly_premium", "test_123_gaurav", "test_weekly_premium")

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
            billingClientManager?.purchase(params, callback)
    }

}