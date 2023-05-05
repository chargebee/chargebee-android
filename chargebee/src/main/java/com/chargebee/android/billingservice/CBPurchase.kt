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

object CBPurchase {

    private var billingClientManager: BillingClientManager? = null
    val productIdList = arrayListOf<String>()
    private var customer: CBCustomer? = null
    internal var includeInActivePurchases = false

    internal enum class ProductType(val value: String) {
        SUBS("subs"),
        INAPP("inapp")
    }

    /*
    * Get the product ID's from chargebee system
    */
    @JvmStatic
    fun retrieveProductIdentifers(
        params: Array<String> = arrayOf(),
        completion: (CBProductIDResult<ArrayList<String>>) -> Unit
    ) {
        if (params.isNotEmpty()) {
            params[0] = params[0].ifEmpty { Chargebee.limit }
            val queryParams = append(params)
            retrieveProductIDList(queryParams, completion)
        } else {
            retrieveProductIDList(arrayOf(), completion)
        }
    }

    /**
     * Get the CBProducts for the given list of product Ids
     * @param [context] current activity context
     * @param [params] list of product Ids
     * @param [callBack] The listener will be called when retrieve products completes.
     */
    @JvmStatic
    fun retrieveProducts(
        context: Context,
        params: ArrayList<String>,
        callBack: CBCallback.ListProductsCallback<ArrayList<CBProduct>>
    ) {
        sharedInstance(context).retrieveProducts(ProductType.SUBS.value, params, callBack)
    }

    /**
     * Buy the product with/without customer id
     * @param [product] The product that wish to purchase
     * @param [callback] listener will be called when product purchase completes.
     */
    @Deprecated(
        message = "This will be removed in upcoming release, Please use API fun - purchaseProduct(product: CBProduct, customer : CBCustomer? = null, callback)",
        level = DeprecationLevel.WARNING
    )
    @JvmStatic
    fun purchaseProduct(
        product: CBProduct, customerID: String,
        callback: CBCallback.PurchaseCallback<String>
    ) {
        customer = CBCustomer(customerID, "", "", "")
        purchaseProduct(product, callback)
    }

    /**
     * Buy the product with/without customer data
     * @param [product] The product that wish to purchase
     * @param [callback] listener will be called when product purchase completes.
     */
    @JvmStatic
    fun purchaseProduct(
        product: CBProduct, customer: CBCustomer? = null,
        callback: CBCallback.PurchaseCallback<String>
    ) {
        this.customer = customer
        purchaseProduct(product, callback)
    }

    private fun purchaseProduct(product: CBProduct, callback: CBCallback.PurchaseCallback<String>) {
        if (!TextUtils.isEmpty(Chargebee.sdkKey)) {
            CBAuthentication.isSDKKeyValid(Chargebee.sdkKey) {
                when (it) {
                    is ChargebeeResult.Success -> {
                        if (billingClientManager?.isFeatureSupported() == true) {
                            if (billingClientManager?.isBillingClientReady() == true) {
                                billingClientManager?.purchase(product, callback)
                            } else {
                                callback.onError(CBException(ErrorDetail(GPErrorCode.BillingClientNotReady.errorMsg)))
                            }
                        } else {
                            callback.onError(CBException(ErrorDetail(GPErrorCode.FeatureNotSupported.errorMsg)))
                        }
                    }
                    is ChargebeeResult.Error -> {
                        Log.i(javaClass.simpleName, "Exception from server :${it.exp.message}")
                        callback.onError(it.exp)
                    }
                }
            }
        } else {
            callback.onError(
                CBException(
                    ErrorDetail(
                        message = GPErrorCode.SDKKeyNotAvailable.errorMsg,
                        httpStatusCode = 400
                    )
                )
            )
        }
    }

    /**
     * This method will provide all the purchases associated with the current account based on the [includeInActivePurchases] flag set.
     * And the associated purchases will be synced with Chargebee.
     *
     * @param [context] Current activity context
     * @param [includeInActivePurchases] False by default. if true, only active purchases restores and synced with Chargebee.
     * @param [completionCallback] The listener will be called when restore purchase completes.
     */
    @JvmStatic
    fun restorePurchases(
        context: Context,
        includeInActivePurchases: Boolean = false,
        completionCallback: CBCallback.RestorePurchaseCallback
    ) {
        this.includeInActivePurchases = includeInActivePurchases
        sharedInstance(context).restorePurchases(completionCallback)
    }

    /* Chargebee Method - used to validate the receipt of purchase  */
    @JvmStatic
    fun validateReceipt(
        purchaseToken: String,
        product: CBProduct,
        completion: (ChargebeeResult<Any>) -> Unit
    ) {
        try {
            validateReceipt(purchaseToken, product.productId, completion)
        } catch (exp: Exception) {
            Log.e(javaClass.simpleName, "Exception in validateReceipt() :" + exp.message)
            ChargebeeResult.Error(
                exp = CBException(
                    error = ErrorDetail(
                        exp.message
                    )
                )
            )
        }
    }

    internal fun validateReceipt(
        purchaseToken: String,
        productId: String,
        completion: (ChargebeeResult<Any>) -> Unit
    ) {
        val logger = CBLogger(name = "buy", action = "process_purchase_command")
        val params = Params(
            purchaseToken,
            productId,
            customer,
            Chargebee.channel
        )
        ResultHandler.safeExecuter(
            { ReceiptResource().validateReceipt(params) },
            completion,
            logger
        )
    }

    /*
  * Get the product ID's from chargebee system.
  */
    private fun retrieveProductIDList(
        params: Array<String>,
        completion: (CBProductIDResult<ArrayList<String>>) -> Unit
    ) {
        // The Plan will be fetched based on the user catalog versions in chargebee system.
        when (Chargebee.version) {
            // If user catalog version1 then get the plan's
            CatalogVersion.V1.value -> {
                Chargebee.retrieveAllPlans(params) {
                    when (it) {
                        is ChargebeeResult.Success -> {
                            Log.i(javaClass.simpleName, "list plan ID's :  ${it.data}")
                            val productsList = (it.data as PlansWrapper).list
                            productIdList.clear()
                            for (plan in productsList) {
                                if (!TextUtils.isEmpty(plan.plan.channel)) {
                                    val id = plan.plan.id.split("-")
                                    productIdList.add(id[0])

                                }
                            }
                            completion(CBProductIDResult.ProductIds(productIdList))
                        }
                        is ChargebeeResult.Error -> {
                            Log.e(
                                javaClass.simpleName,
                                "Error retrieving all plans :  ${it.exp.message}"
                            )
                            completion(CBProductIDResult.Error(CBException(ErrorDetail(it.exp.message))))
                        }
                    }
                }
            }
            // If user catalog version2 then get the Item's
            CatalogVersion.V2.value -> {
                Chargebee.retrieveAllItems(params) {
                    when (it) {
                        is ChargebeeResult.Success -> {
                            Log.i(javaClass.simpleName, "list item ID's :  ${it.data}")
                            val productsList = (it.data as ItemsWrapper).list
                            productIdList.clear()
                            for (item in productsList) {
                                productIdList.add(item.item.id)
                            }
                            completion(CBProductIDResult.ProductIds(productIdList))
                        }
                        is ChargebeeResult.Error -> {
                            Log.e(
                                javaClass.simpleName,
                                "Error retrieving all items :  ${it.exp.message}"
                            )
                            completion(CBProductIDResult.Error(CBException(ErrorDetail(it.exp.message))))
                        }
                    }
                }
            }
            // Check the catalog version
            CatalogVersion.Unknown.value -> {
                val auth = Auth(
                    Chargebee.sdkKey,
                    Chargebee.applicationId,
                    Chargebee.appName,
                    Chargebee.channel
                )
                CBAuthentication.authenticate(auth) {
                    when (it) {
                        is ChargebeeResult.Success -> {
                            Log.i(javaClass.simpleName, " Response :${it.data}")
                            val response = it.data as CBAuthResponse
                            Chargebee.version = response.in_app_detail.product_catalog_version
                            retrieveProductIDList(params, completion)
                        }
                        is ChargebeeResult.Error -> {
                            Log.i(javaClass.simpleName, "Invalid catalog version")
                            completion(CBProductIDResult.Error(CBException(ErrorDetail("Invalid catalog version"))))
                        }
                    }
                }
            }
            else -> {
                Log.i(javaClass.simpleName, "Unknown error")
                completion(CBProductIDResult.Error(CBException(ErrorDetail("Unknown error"))))
            }
        }
    }

    internal fun append(arr: Array<String>): Array<String> {
        val list: MutableList<String> = arr.toMutableList()
        if (arr.size == 1) list.add("Standard")
        return list.toTypedArray()
    }

    private fun sharedInstance(context: Context): BillingClientManager {
        if (billingClientManager == null) {
            billingClientManager = BillingClientManager(context)
        }
        return billingClientManager as BillingClientManager
    }
}