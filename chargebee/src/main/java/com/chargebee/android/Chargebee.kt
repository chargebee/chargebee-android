package com.chargebee.android

import android.text.TextUtils
import android.util.Log
import com.chargebee.android.BuildConfig
import com.chargebee.android.billingservice.CBPurchase
import com.chargebee.android.exceptions.*
import com.chargebee.android.gateway.GatewayTokenizer
import com.chargebee.android.loggers.CBLogger
import com.chargebee.android.models.Addon
import com.chargebee.android.models.PaymentDetail
import com.chargebee.android.models.ResultHandler
import com.chargebee.android.network.Auth
import com.chargebee.android.network.CBAuthResponse
import com.chargebee.android.network.CBAuthentication
import com.chargebee.android.resources.*
import com.chargebee.android.resources.ItemsResource
import com.chargebee.android.resources.PlanResource
import com.chargebee.android.resources.SubscriptionResource
import okhttp3.Credentials

object Chargebee {
    var site: String = ""
    var publishableApiKey: String = ""
    var encodedApiKey: String = ""
    var sdkKey: String = ""
    var baseUrl: String = ""
    var allowErrorLogging: Boolean = true
    var version: String = CatalogVersion.Unknown.value
    var applicationId: String = ""
    const val channel: String = "play_store"
    var appName: String = "Chargebee"
    var environment: String = "cb_android_sdk"
    const val platform: String = "Android"
    const val sdkVersion: String = BuildConfig.VERSION_NAME
    const val limit: String = "100"

    /* Configure the app details with chargebee system */
    fun configure(site: String, publishableApiKey: String, allowErrorLogging: Boolean = true, sdkKey: String="", packageName: String="" ) {
        this.applicationId = packageName
        this.publishableApiKey = publishableApiKey
        this.site = site
        this.encodedApiKey = Credentials.basic(publishableApiKey, "")
        this.baseUrl = "https://${site}.chargebee.com/api/"
        this.allowErrorLogging = allowErrorLogging
        this.sdkKey = sdkKey
        val auth = Auth(sdkKey,applicationId,appName, channel)

        CBAuthentication.authenticate(auth) {
            when(it){
                is ChargebeeResult.Success ->{
                    Log.i(javaClass.simpleName, "Environment Setup Completed")
                    Log.i(javaClass.simpleName, " Response :${it.data}")
                    val response = it.data as CBAuthResponse
                    this.version = response.in_app_detail.product_catalog_version
                    this.applicationId = response.in_app_detail.app_id
                    this.appName = response.in_app_detail.app_name
                }
                is ChargebeeResult.Error ->{
                    Log.i(javaClass.simpleName, "Exception from server :${it.exp.message}")
                    this.version = CatalogVersion.Unknown.value
                }
            }
        }
    }

    /* Configure the app details with chargebee system */
    fun configure(
        site: String,
        publishableApiKey: String,
        allowErrorLogging: Boolean = true,
        sdkKey: String = "",
        packageName: String = "",
        completion: (ChargebeeResult<Any>) -> Unit
    ) {
        this.applicationId = packageName
        this.publishableApiKey = publishableApiKey
        this.site = site
        this.encodedApiKey = Credentials.basic(publishableApiKey, "")
        this.baseUrl = "https://${site}.chargebee.com/api/"
        this.allowErrorLogging = allowErrorLogging
        this.sdkKey = sdkKey
        val auth = Auth(sdkKey, applicationId, appName, channel)

        CBAuthentication.authenticate(auth) {
            when (it) {
                is ChargebeeResult.Success -> {
                    Log.i(javaClass.simpleName, "Environment Setup Completed")
                    Log.i(javaClass.simpleName, " Response :${it.data}")
                    val response = it.data as CBAuthResponse
                    this.version = response.in_app_detail.product_catalog_version
                    this.applicationId = response.in_app_detail.app_id
                    this.appName = response.in_app_detail.app_name
                    completion(ChargebeeResult.Success(response))
                }
                is ChargebeeResult.Error -> {
                    Log.i(javaClass.simpleName, "Exception from server :${it.exp.message}")
                    this.version = CatalogVersion.Unknown.value
                    completion(
                        ChargebeeResult.Error(
                            exp = CBException(
                                error = ErrorDetail(
                                    message = "${it.exp.message}"
                                )
                            )
                        )
                    )
                }
            }
        }
    }

    /* Get the subscription details from chargebee system */
    @Throws(InvalidRequestException::class, OperationFailedException::class)
    fun retrieveSubscription(subscriptionId: String, completion: (ChargebeeResult<Any>) -> Unit) {
        val logger = CBLogger(name = "Subscription", action = "Fetch Subscription")
        ResultHandler.safeExecuter({ SubscriptionResource().retrieveSubscription(subscriptionId) }, completion, logger)
    }
    /* Get the subscriptions list from chargebee system by using Customer Id */
    @Throws(InvalidRequestException::class, OperationFailedException::class)
    fun retrieveSubscriptions(queryParams: Map<String, String> = mapOf(), completion: (ChargebeeResult<Any>) -> Unit) {
        val logger = CBLogger(name = "Subscription", action = "Fetch Subscription by using CustomerId")
        if (queryParams.isNotEmpty()) {

                ResultHandler.safeExecuter(
                    { SubscriptionResource().retrieveSubscriptions(queryParams) },
                    completion,
                    logger
                )
        }else{
            completion(ChargebeeResult.Error(
                exp = CBException(
                    error = ErrorDetail(message = "Array/Query Param is empty", apiErrorCode = "400")
                )
            ))
        }
    }

    /* Get the Plan details from chargebee system */
    @Throws(InvalidRequestException::class, OperationFailedException::class)
    fun retrievePlan(planId: String, completion : (ChargebeeResult<Any>) -> Unit) {
        val logger = CBLogger(name = "plan", action = "getAllPlan")
        if (TextUtils.isEmpty(planId))
            completion(ChargebeeResult.Error(
                exp = CBException(
                    error = ErrorDetail(message = "Plan ID is empty", apiErrorCode = "400")
                )
            ))
        else
            ResultHandler.safeExecuter({ PlanResource().retrievePlan(planId) }, completion, logger)
    }

    /* Get the list of Plan's from chargebee system */
    @Throws(InvalidRequestException::class, OperationFailedException::class)
    fun retrieveAllPlans(params: Array<String>, completion : (ChargebeeResult<Any>) -> Unit) {
        val logger = CBLogger(name = "plans", action = "getPlan")
        if (params.isNullOrEmpty())
            completion(ChargebeeResult.Error(
                exp = CBException(
                    error = ErrorDetail(message = "Query param is empty", apiErrorCode = "400")
                )
            ))
        else
            ResultHandler.safeExecuter({ PlanResource().retrieveAllPlans(params) }, completion, logger)
    }

    /* Get the list of Items's from chargebee system */
    @Throws(InvalidRequestException::class, OperationFailedException::class)
    fun retrieveAllItems(
        params: Array<String> = arrayOf<String>(),
        completion: (ChargebeeResult<Any>) -> Unit
    ) {
        val logger = CBLogger(name = "items", action = "getAllItems")
        if (params.isNotEmpty()) {
            ResultHandler.safeExecuter(
                { ItemsResource().retrieveAllItems(params) },
                completion,
                logger
            )
        } else {
            ResultHandler.safeExecuter({
                ItemsResource().retrieveAllItems(
                    arrayOf(
                        limit,
                        "Standard"
                    )
                )
            }, completion, logger)
        }
    }

    /* Get the Item details from chargebee system */
    @Throws(InvalidRequestException::class, OperationFailedException::class)
    fun retrieveItem(itemId: String, completion : (ChargebeeResult<Any>) -> Unit) {
        val logger = CBLogger(name = "item", action = "getItem")
        if (TextUtils.isEmpty(itemId))
            completion(ChargebeeResult.Error(
                exp = CBException(
                    error = ErrorDetail(message = "Item ID is empty", apiErrorCode = "400")
                )
            ))
        else
            ResultHandler.safeExecuter({ ItemsResource().retrieveItem(itemId) }, completion, logger)
    }

    /* Get the entitlement details from chargebee system */
    @Throws(InvalidRequestException::class, OperationFailedException::class)
    fun retrieveEntitlements(subscriptionId: String, completion: (ChargebeeResult<Any>) -> Unit) {
        val logger = CBLogger(name = "Entitlements", action = "retrieve_entitlements")
        ResultHandler.safeExecuter({ EntitlementsResource().retrieveEntitlements(subscriptionId) }, completion, logger)
    }

    @Throws(InvalidRequestException::class, OperationFailedException::class)
    fun retrieve(addonId: String, handler: (CBResult<Addon>) -> Unit) {
        val logger = CBLogger(name = "addon", action = "retrieve_addon")
        ResultHandler.safeExecute({ AddonResource().retrieve(addonId) }, handler, logger)
    }

    @Throws(InvalidRequestException::class, OperationFailedException::class, PaymentException::class)
    fun createTempToken(detail: PaymentDetail, completion: (CBResult<String>) -> Unit) {
        val logger = CBLogger(name = "cb_temp_token", action = "create_temp_token")
        ResultHandler.safeExecute({
            val paymentConfig =
                MerchantPaymentConfigResource().retrieve(detail.currencyCode, detail.type)
            val gatewayToken = GatewayTokenizer().createToken(detail, paymentConfig)
            val cbTempToken = TempTokenResource().create(
                gatewayToken,
                detail.type,
                paymentConfig.gatewayId
            )
            Success(cbTempToken)
        }, completion, logger)
    }
}