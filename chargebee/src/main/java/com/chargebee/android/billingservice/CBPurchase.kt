package com.chargebee.android.billingservice

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.chargebee.android.Chargebee
import com.chargebee.android.ErrorDetail
import com.chargebee.android.exceptions.*
import com.chargebee.android.loggers.CBLogger
import com.chargebee.android.models.*
import com.chargebee.android.models.ResultHandler
import com.chargebee.android.network.Auth
import com.chargebee.android.network.CBAuthResponse
import com.chargebee.android.network.CBAuthentication
import com.chargebee.android.network.Params
import com.chargebee.android.resources.CatalogVersion
import com.chargebee.android.resources.ReceiptResource
import okhttp3.Credentials
import java.util.ArrayList
object CBPurchase {

    var billingClientManager: BillingClientManager? = null
    val productIdList = arrayListOf<String>()
    var price: String = ""

    annotation class SkuType {
        companion object {
            var INAPP = "inapp"
            var SUBS = "subs"
        }
    }
    @JvmStatic
    fun retrieveProductIDs(params: Array<String>, completion : (CBProductIDResult<ArrayList<String>>) -> Unit) {
        val queryParams = append(params,"Standard",Chargebee.channel)
        retrieveProductIDList(queryParams, completion)
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
            val logger = CBLogger(name = "buy", action = "process_purchase_command")
            //price = products.productPrice.drop(1).dropLast(2).replace(".","").replace(",","")

            val params = Params(
                purchaseToken,
                products.productId,
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

    private fun retrieveProductIDList(params: Array<String>, completion: (CBProductIDResult<ArrayList<String>>) -> Unit){
        when(Chargebee.version){
            CatalogVersion.V1.value ->{
                Plan.retrieveAllPlans(params){
                    when (it) {
                        is ChargebeeResult.Success -> {
                            Log.i(javaClass.simpleName, "list plan ID's :  ${it.data}")
                            val productsList = (it.data as PlansWrapper).list
                            productIdList.clear()
                            for (plan in  productsList){
                                if (plan.plan.channel.trim() == Chargebee.channel.trim()){
                                    val id = plan.plan.id.split("-")
                                    productIdList.add(id[0])
                                }
                            }
                            completion(CBProductIDResult.ProductIds(productIdList))
                        }
                        is ChargebeeResult.Error -> {
                            Log.e(javaClass.simpleName, "Error retrieving all plans :  ${it.exp.message}")
                            completion(CBProductIDResult.Error(CBException(ErrorDetail(it.exp.message))))
                        }
                    }
                }
            }
            CatalogVersion.V2.value ->{
                Items.retrieveAllItems(params){
                    when (it) {
                        is ChargebeeResult.Success -> {
                            Log.i(javaClass.simpleName, "list item ID's :  ${it.data}")
                            val productsList = (it.data as ItemsWrapper).list
                            productIdList.clear()
                            for (item in  productsList){
                                if (item.item.channel.trim() == Chargebee.channel.trim())
                                productIdList.add(item.item.id)
                            }
                            completion(CBProductIDResult.ProductIds(productIdList))
                        }
                        is ChargebeeResult.Error -> {
                            Log.e(javaClass.simpleName, "Error retrieving all items :  ${it.exp.message}")
                            completion(CBProductIDResult.Error(CBException(ErrorDetail(it.exp.message))))
                        }
                    }
                }
            }
            CatalogVersion.Unknown.value ->{
                val auth = Auth(Chargebee.sdkKey,
                    Chargebee.applicationId,
                    Chargebee.appName,
                    Chargebee.channel
                )
                CBAuthentication.authenticate(auth) {
                    when(it){
                        is ChargebeeResult.Success ->{
                            Log.i(javaClass.simpleName, " Response :${it.data}")
                            val response = it.data as CBAuthResponse
                            Chargebee.version = response.in_app_detail.product_catalog_version
                            retrieveProductIDList(params,completion)
                        }
                        is ChargebeeResult.Error ->{
                            Log.i(javaClass.simpleName, "Invalid catalog version")
                            completion(CBProductIDResult.Error(CBException(ErrorDetail("Invalid catalog version"))))
                        }
                    }
                }
            }
            else ->{
                Log.i(javaClass.simpleName, "Unknown error")
                completion(CBProductIDResult.Error(CBException(ErrorDetail("Unknown error"))))
            }
        }
    }
    private fun append(arr: Array<String>, sort: String, channel: String ): Array<String> {
        val list: MutableList<String> = arr.toMutableList()
        list.add(sort)
        list.add(channel)
        return list.toTypedArray()
    }

}