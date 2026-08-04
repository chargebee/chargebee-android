package com.chargebee.android

import android.content.Context
import android.content.Intent
import android.content.Intent.*
import android.net.Uri
import androidx.core.content.ContextCompat
import android.text.TextUtils
import android.util.Log
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

/**
 * Closure that returns a fresh mobile token from the backend.
 * The SDK invokes it during [Chargebee.configure] and whenever a request fails with a 401.
 * The provided callback must be invoked with a token, or with null when one cannot be obtained.
 */
typealias CBMobileTokenProvider = (completion: (String?) -> Unit) -> Unit

object Chargebee {
    var site: String = ""
    var publishableApiKey: String = ""
    private var encodedPublishableApiKey: String = ""
    var sdkKey: String = ""
    var baseUrl: String = ""
    var allowErrorLogging: Boolean = true
    var version: String = CatalogVersion.Unknown.value
    var applicationId: String = ""
    const val channel: String = "play_store"
    var appName: String = "Chargebee"
    var environment: String = "cb_android_sdk"
    const val platform: String = "Android"
    const val sdkVersion: String = "2.0.0-beta-5"
    const val limit: String = "100"

    /*
     * Mobile token auth. When a token is present the SDK sends it as the Authorization header
     * instead of the publishable key. If empty, we fall back to using publishable-key.
     */
    var mobileToken: String = ""
    var tokenProvider: CBMobileTokenProvider? = null

    val encodedMobileToken: String
        get() = if (mobileToken.isNotEmpty()) Credentials.basic(mobileToken, "") else ""

    /*
     * The Authorization header the SDK sends on every request: the mobile token when one is configured,
     * otherwise the publishable key. Resolved lazily so the value picks up a refreshed token.
     */
    val encodedApiKey: String
        get() = if (mobileToken.isNotEmpty()) encodedMobileToken else encodedPublishableApiKey
    private const val PLAY_STORE_SUBSCRIPTION_URL =
        "https://play.google.com/store/account/subscriptions"
    private const val SUBSCRIPTION_URL =
        "https://play.google.com/store/account/subscriptions?sku=%s&package=%s"

    /* Configure the app details with chargebee system */
    fun configure(
        site: String,
        publishableApiKey: String,
        allowErrorLogging: Boolean = true,
        sdkKey: String = "",
        packageName: String = ""
    ) {
        this.applicationId = packageName
        this.publishableApiKey = publishableApiKey
        this.site = site
        this.encodedPublishableApiKey = Credentials.basic(publishableApiKey, "")
        this.mobileToken = ""
        this.tokenProvider = null
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
                }
                is ChargebeeResult.Error -> {
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
        this.encodedPublishableApiKey = Credentials.basic(publishableApiKey, "")
        this.mobileToken = ""
        this.tokenProvider = null
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
                    completion(it)
                }
            }
        }
    }

    /*
     * Configure the SDK using a mobile token instead of a publishable API key. The [tokenProvider] is
     * invoked to obtain a token from the merchant's backend, both now and whenever a request is
     * rejected with a 401 (expired/revoked token).
     */
    fun configure(
        site: String,
        sdkKey: String = "",
        packageName: String = "",
        allowErrorLogging: Boolean = true,
        tokenProvider: CBMobileTokenProvider,
        completion: (ChargebeeResult<Any>) -> Unit
    ) {
        this.site = site
        this.publishableApiKey = ""
        this.encodedPublishableApiKey = ""
        this.baseUrl = "https://${site}.chargebee.com/api/"
        this.allowErrorLogging = allowErrorLogging
        this.sdkKey = sdkKey
        this.applicationId = packageName
        this.tokenProvider = tokenProvider

        refreshMobileToken { success ->
            if (!success) {
                completion(
                    ChargebeeResult.Error(
                        exp = CBException(
                            error = ErrorDetail(
                                message = "Unable to fetch a mobile token from the token provider",
                                apiErrorCode = "401",
                                httpStatusCode = 401
                            )
                        )
                    )
                )
                return@refreshMobileToken
            }
            // Nothing to verify without an SDK key; the environment is ready.
            if (TextUtils.isEmpty(sdkKey)) {
                completion(ChargebeeResult.Success("Environment Setup Completed"))
                return@refreshMobileToken
            }
            val auth = Auth(sdkKey, applicationId, appName, channel)
            CBAuthentication.authenticate(auth) {
                when (it) {
                    is ChargebeeResult.Success -> {
                        val response = it.data as CBAuthResponse
                        this.version = response.in_app_detail.product_catalog_version
                        this.applicationId = response.in_app_detail.app_id
                        this.appName = response.in_app_detail.app_name
                        completion(ChargebeeResult.Success(response))
                    }
                    is ChargebeeResult.Error -> {
                        this.version = CatalogVersion.Unknown.value
                        completion(it)
                    }
                }
            }
        }
    }

    /*
     * Fetches a fresh token from the merchant-supplied provider and stores it. Invokes [completion]
     * with false when no provider is configured or the provider returns an empty token.
     */
    fun refreshMobileToken(completion: (Boolean) -> Unit) {
        val provider = tokenProvider
        if (provider == null) {
            completion(false)
            return
        }
        provider { token ->
            if (!token.isNullOrEmpty()) {
                this.mobileToken = token
                completion(true)
            } else {
                completion(false)
            }
        }
    }

    /* Get the subscription details from chargebee system */
    @Throws(InvalidRequestException::class, OperationFailedException::class)
    fun retrieveSubscription(subscriptionId: String, completion: (ChargebeeResult<Any>) -> Unit) {
        val logger = CBLogger(name = "Subscription", action = "Fetch Subscription")
        ResultHandler.safeExecuter(
            { SubscriptionResource().retrieveSubscription(subscriptionId) },
            completion,
            logger
        )
    }

    /* Get the subscriptions list from chargebee system by using Customer Id */
    @Throws(InvalidRequestException::class, OperationFailedException::class)
    fun retrieveSubscriptions(
        queryParams: Map<String, String> = mapOf(),
        completion: (ChargebeeResult<Any>) -> Unit
    ) {
        val logger =
            CBLogger(name = "Subscription", action = "Fetch Subscription by using CustomerId")
        if (queryParams.isNotEmpty()) {

            ResultHandler.safeExecuter(
                { SubscriptionResource().retrieveSubscriptions(queryParams) },
                completion,
                logger
            )
        } else {
            completion(
                ChargebeeResult.Error(
                    exp = CBException(
                        error = ErrorDetail(
                            message = "Array/Query Param is empty",
                            apiErrorCode = "400",
                            httpStatusCode = 400
                        )
                    )
                )
            )
        }
    }

    /* Get the Plan details from chargebee system */
    @Throws(InvalidRequestException::class, OperationFailedException::class)
    fun retrievePlan(planId: String, completion: (ChargebeeResult<Any>) -> Unit) {
        val logger = CBLogger(name = "plan", action = "getPlan")
        if (TextUtils.isEmpty(planId))
            completion(
                ChargebeeResult.Error(
                    exp = CBException(
                        error = ErrorDetail(
                            message = "Plan ID is empty",
                            apiErrorCode = "400",
                            httpStatusCode = 400
                        )
                    )
                )
            )
        else
            ResultHandler.safeExecuter({ PlanResource().retrievePlan(planId) }, completion, logger)
    }

    /* Get the list of Plan's from chargebee system */
    @Throws(InvalidRequestException::class, OperationFailedException::class)
    fun retrieveAllPlans(
        params: Array<String> = arrayOf(),
        completion: (ChargebeeResult<Any>) -> Unit
    ) {
        val logger = CBLogger(name = "plans", action = "getAllPlan")
        if (params.isNotEmpty()) {
            ResultHandler.safeExecuter(
                { PlanResource().retrieveAllPlans(params) },
                completion,
                logger
            )
        } else {
            ResultHandler.safeExecuter({
                PlanResource().retrieveAllPlans(
                    arrayOf(
                        limit
                    )
                )
            }, completion, logger)
        }
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
    fun retrieveItem(itemId: String, completion: (ChargebeeResult<Any>) -> Unit) {
        val logger = CBLogger(name = "item", action = "getItem")
        if (TextUtils.isEmpty(itemId))
            completion(
                ChargebeeResult.Error(
                    exp = CBException(
                        error = ErrorDetail(
                            message = "Item ID is empty",
                            apiErrorCode = "400",
                            httpStatusCode = 400
                        )
                    )
                )
            )
        else
            ResultHandler.safeExecuter({ ItemsResource().retrieveItem(itemId) }, completion, logger)
    }

    /* Get the entitlement details from chargebee system */
    @Throws(InvalidRequestException::class, OperationFailedException::class)
    fun retrieveEntitlements(subscriptionId: String, completion: (ChargebeeResult<Any>) -> Unit) {
        val logger = CBLogger(name = "Entitlements", action = "retrieve_entitlements")
        ResultHandler.safeExecuter(
            { EntitlementsResource().retrieveEntitlements(subscriptionId) },
            completion,
            logger
        )
    }

    @Throws(InvalidRequestException::class, OperationFailedException::class)
    fun retrieve(addonId: String, handler: (CBResult<Addon>) -> Unit) {
        val logger = CBLogger(name = "addon", action = "retrieve_addon")
        ResultHandler.safeExecute({ AddonResource().retrieve(addonId) }, handler, logger)
    }

    @Throws(
        InvalidRequestException::class,
        OperationFailedException::class,
        PaymentException::class
    )
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

    /**
     * This method will be used to show the Manage Subscriptions Settings in your App,
     *
     * @param [context] Current activity context
     * @param [productId] Optional. Product Identifier.
     * @param [packageName] Optional. Application Id.
     */
    fun showManageSubscriptionsSettings(
        context: Context,
        productId: String? = null,
        packageName: String? = null
    ) {
        val uriString = if (productId == null && packageName == null) {
            PLAY_STORE_SUBSCRIPTION_URL
        } else {
            String.format(
                SUBSCRIPTION_URL,
                productId, packageName
            );
        }
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(uriString)
        intent.flags = FLAG_ACTIVITY_NEW_TASK
        ContextCompat.startActivity(context, intent, null)
    }
}