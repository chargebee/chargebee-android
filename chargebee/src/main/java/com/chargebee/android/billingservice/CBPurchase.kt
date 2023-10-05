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
import com.chargebee.android.network.*
import com.chargebee.android.resources.CatalogVersion
import com.chargebee.android.resources.ReceiptResource

object CBPurchase {

    private var billingClientManager: BillingClientManager? = null
    val productIdList = mutableSetOf<String>()
    private var customer: CBCustomer? = null
    internal var includeInActivePurchases = false
    internal var productType = OneTimeProductType.UNKNOWN

    /*
    * Get the product ID's from chargebee system
    */
    @JvmStatic
    fun retrieveProductIdentifers(
        params: Array<String> = arrayOf(),
        completion: (CBProductIDResult<MutableSet<String>>) -> Unit
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
        sharedInstance(context).retrieveProducts(params, callBack)
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
        isSDKKeyValid({
            log(customer, product)
            billingClientManager?.purchase(product, callback)
        }, {
            callback.onError(it)
        })
    }

    /**
     * Buy the non-subscription product with/without customer data
     * @param [product] The product that wish to purchase
     * @param [customer] Optional. Customer Object.
     * @param [productType] One time Product Type. Consumable or Non-Consumable
     * @param [callback] listener will be called when product purchase completes.
     */
    @JvmStatic
    fun purchaseNonSubscriptionProduct(
        product: CBProduct, customer: CBCustomer? = null,
        productType: OneTimeProductType,
        callback: CBCallback.OneTimePurchaseCallback
    ) {
        this.customer = customer
        this.productType = productType

        isSDKKeyValid({
            log(CBPurchase.customer, product, productType)
            billingClientManager?.purchaseNonSubscriptionProduct(product, callback)
        }, {
            callback.onError(it)
        })
    }

    private fun isSDKKeyValid(success: () -> Unit, error: (CBException) -> Unit) {
        if (!TextUtils.isEmpty(Chargebee.sdkKey)) {
            CBAuthentication.isSDKKeyValid(Chargebee.sdkKey) {
                when (it) {
                    is ChargebeeResult.Success -> {
                        if (billingClientManager?.isFeatureSupported() == true) {
                            success()
                        } else {
                            error(CBException(ErrorDetail(GPErrorCode.FeatureNotSupported.errorMsg, httpStatusCode = BillingErrorCode.FEATURE_NOT_SUPPORTED.code)))
                        }
                    }
                    is ChargebeeResult.Error -> {
                        error(it.exp)
                    }
                }
            }
        } else {
            error(
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
     * @param [customer] Optional. Customer Object.
     * @param [includeInActivePurchases] False by default. if true, only active purchases restores and synced with Chargebee.
     * @param [completionCallback] The listener will be called when restore purchase completes.
     */
    @JvmStatic
    fun restorePurchases(
        context: Context,
        customer: CBCustomer? = null,
        includeInActivePurchases: Boolean = false,
        completionCallback: CBCallback.RestorePurchaseCallback
    ) {
        this.includeInActivePurchases = includeInActivePurchases
        this.customer = customer
        sharedInstance(context).restorePurchases(completionCallback)
    }

    /**
     * This method will be used to validate the receipt with Chargebee,
     * when syncing with Chargebee fails after the successful purchase in Google Play Store.
     *
     * @param [context] Current activity context
     * @param [productId] Product Identifier.
     * @param [customer] Optional. Customer Object.
     * @param [completionCallback] The listener will be called when validate receipt completes.
     */
    @JvmStatic
    fun validateReceipt(
        context: Context,
        product: CBProduct,
        customer: CBCustomer? = null,
        completionCallback: CBCallback.PurchaseCallback<String>
    ) {
        this.customer = customer
        sharedInstance(context).validateReceiptWithChargebee(product, completionCallback)
    }

    /* Chargebee Method - used to validate the receipt of purchase  */
    @JvmStatic
    internal fun validateReceipt(
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
        val logger = CBLogger(name = "buy", action = "process_purchase_command",
            additionalInfo = mapOf("customerId" to (customer?.id ?: ""), "product" to productId, "purchaseToken" to purchaseToken))
        val params = Params(
            purchaseToken,
            productId,
            customer,
            Chargebee.channel,
            null
        )
        ResultHandler.safeExecuter(
            { ReceiptResource().validateReceipt(params) },
            completion,
            logger
        )
    }

    /**
     * This method will be used to validate the receipt with Chargebee,
     * when syncing with Chargebee fails after the successful purchase in Google Play Store.
     *
     * @param [context] Current activity context
     * @param [productId] Product Identifier.
     * @param [customer] Optional. Customer Object.
     * @param [productType] Product Type. Consumable or Non-Consumable product
     * @param [completionCallback] The listener will be called when validate receipt completes.
     */
    @JvmStatic
    fun validateReceiptForNonSubscriptions(
        context: Context,
        product: CBProduct,
        customer: CBCustomer? = null,
        productType: OneTimeProductType,
        completionCallback: CBCallback.OneTimePurchaseCallback
    ) {
        this.customer = customer
        this.productType = productType
        sharedInstance(context).validateNonSubscriptionReceiptWithChargebee(product, completionCallback)
    }

    internal fun validateNonSubscriptionReceipt(
        purchaseToken: String,
        product: CBProduct,
        completion: (ChargebeeResult<Any>) -> Unit
    ) {
        validateNonSubscriptionReceipt(purchaseToken, product.productId, completion)
    }

    internal fun validateNonSubscriptionReceipt(
        purchaseToken: String,
        productId: String,
        completion: (ChargebeeResult<Any>) -> Unit
    ) {
        val logger = CBLogger(name = "buy", action = "one_time_purchase",
            additionalInfo = mapOf("customerId" to (customer?.id ?: ""), "product" to productId, "purchaseToken" to purchaseToken))
        val params = Params(
            purchaseToken,
            productId,
            customer,
            Chargebee.channel,
            productType
        )
        ResultHandler.safeExecuter(
            { ReceiptResource().validateReceiptForNonSubscription(params) },
            completion,
            logger
        )
    }

    /*
  * Get the product ID's from chargebee system.
  */
    internal fun retrieveProductIDList(
        params: Array<String>,
        completion: (CBProductIDResult<MutableSet<String>>) -> Unit
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
                                val id = item.item.id.split("-")
                                productIdList.add(id[0])
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

    private fun log(customer: CBCustomer?, product: CBProduct, productType: OneTimeProductType? = null) {
        val additionalInfo = additionalInfo(customer, product, productType)
        val logger = CBLogger(
            name = "buy",
            action = "before_purchase_command",
            additionalInfo = additionalInfo
        )
        ResultHandler.safeExecute { logger.info() }
    }
    private fun additionalInfo(customer: CBCustomer?, product: CBProduct, productType: OneTimeProductType? = null): Map<String, String> {
        val map = mutableMapOf("product" to product.productId)
        customer?.let { map["customerId"] = (it.id ?: "") }
        productType?.let { map["productType"] = it.toString() }
        return map
    }

}