package com.chargebee.android.billingservice

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.chargebee.android.Chargebee
import com.chargebee.android.ErrorDetail
import com.chargebee.android.exceptions.*
import com.chargebee.android.loggers.CBLogger
import com.chargebee.android.models.*
import com.chargebee.android.models.ResultHandler
import com.chargebee.android.network.*
import com.chargebee.android.resources.CatalogVersion
import com.chargebee.android.resources.ReceiptResource
import java.util.ArrayList
object CBPurchase {

    var billingClientManager: BillingClientManager? = null
    val productIdList = arrayListOf<String>()
    private var customer : CBCustomer? = null

    annotation class SkuType {
        companion object {
            var INAPP = "inapp"
            var SUBS = "subs"
        }
    }
    /*
    * Get the product ID's from chargebee system
    */
    @JvmStatic
    fun retrieveProductIdentifers(params: Array<String> = arrayOf(), completion : (CBProductIDResult<ArrayList<String>>) -> Unit) {
        if (params.isNotEmpty()) {
            params[0] = params[0].ifEmpty { Chargebee.limit }
            val queryParams = append(params)
            retrieveProductIDList(queryParams, completion)
        }else{retrieveProductIDList(arrayOf(), completion) }
    }
    /* Get the product/sku details from Play console */
    @JvmStatic
    fun retrieveProducts(context: Context, params: ArrayList<String>, callBack : CBCallback.ListProductsCallback<ArrayList<CBProduct>>) {
        try {
            val connectionState = billingClientManager?.billingClient?.connectionState
            if (connectionState!=null && connectionState == BillingClient.ConnectionState.CONNECTED){
                billingClientManager?.billingClient?.endConnection()
            }
            billingClientManager = BillingClientManager(context,SkuType.SUBS, params, callBack)
        }catch (ex: CBException){
            callBack.onError(ex)
        }
    }

    /* Buy the product with/without customer Id */
    @Deprecated(message = "This will be removed in upcoming release, Please use API fun - purchaseProduct(product: CBProduct, customer : CBCustomer? = null, callback)", level = DeprecationLevel.WARNING)
    @JvmStatic
    fun purchaseProduct(
        product: CBProduct, customerID: String,
        callback: CBCallback.PurchaseCallback<String>) {
        customer = CBCustomer(customerID,"","","")
        purchaseProduct(product, callback)
    }

    /* Buy the product with/without customer info */
    @JvmStatic
    fun purchaseProduct(
        product: CBProduct, customer: CBCustomer? = null,
        callback: CBCallback.PurchaseCallback<String>) {
        this.customer = customer
        purchaseProduct(product, callback)
    }

    private fun purchaseProduct(product: CBProduct,callback: CBCallback.PurchaseCallback<String>){
        if (!TextUtils.isEmpty(Chargebee.sdkKey)){
            CBAuthentication.isSDKKeyValid(Chargebee.sdkKey){
                when(it){
                    is ChargebeeResult.Success -> {
                        if (billingClientManager?.isFeatureSupported() == true) {
                            if (billingClientManager?.isBillingClientReady() == true) {
                                billingClientManager?.purchase(product, callback)
                            } else {
                                callback.onError(CBException(ErrorDetail(GPErrorCode.BillingClientNotReady.errorMsg)))
                            }
                        }else {
                            callback.onError(CBException(ErrorDetail(GPErrorCode.FeatureNotSupported.errorMsg)))
                        }
                    }
                    is ChargebeeResult.Error ->{
                        Log.i(javaClass.simpleName, "Exception from server :${it.exp.message}")
                        callback.onError(it.exp)
                    }
                }
            }
        }else{
            callback.onError(CBException(ErrorDetail(message = GPErrorCode.SDKKeyNotAvailable.errorMsg, httpStatusCode = 400)))
        }
    }

    /* Chargebee Method - used to validate the receipt of purchase  */
    @JvmStatic
    fun validateReceipt(purchaseToken: String, product: CBProduct, completion : (ChargebeeResult<Any>) -> Unit) {
        try {
            val logger = CBLogger(name = "buy", action = "process_purchase_command")
            val params = Params(
                purchaseToken,
                product.productId,
                customer,
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

    /*
  * Get the product ID's from chargebee system.
  */
    fun retrieveProductIDList(params: Array<String>, completion: (CBProductIDResult<ArrayList<String>>) -> Unit){
        // The Plan will be fetched based on the user catalog versions in chargebee system.
        when(Chargebee.version){
            // If user catalog version1 then get the plan's
            CatalogVersion.V1.value ->{
                Chargebee.retrieveAllPlans(params){
                    when (it) {
                        is ChargebeeResult.Success -> {
                            Log.i(javaClass.simpleName, "list plan ID's :  ${it.data}")
                            val productsList = (it.data as PlansWrapper).list
                            productIdList.clear()
                            for (plan in  productsList){
                                if (!TextUtils.isEmpty(plan.plan.channel)) {
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
            // If user catalog version2 then get the Item's
            CatalogVersion.V2.value ->{
                Chargebee.retrieveAllItems(params){
                    when (it) {
                        is ChargebeeResult.Success -> {
                            Log.i(javaClass.simpleName, "list item ID's :  ${it.data}")
                            val productsList = (it.data as ItemsWrapper).list
                            productIdList.clear()
                            for (item in  productsList){
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
            // Check the catalog version
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
    fun append(arr: Array<String>): Array<String> {
        val list: MutableList<String> = arr.toMutableList()
        if (arr.size==1) list.add("Standard")
        return list.toTypedArray()
    }

}