package com.chargebee.android.repository

import com.chargebee.android.Chargebee
import com.chargebee.android.models.KeyValidationWrapper
import com.chargebee.android.models.SubscriptionDetailsWrapper
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

internal interface PurchaseRepository {
    @GET("v2/plans/{sdkKey}")
    suspend fun validateSDKKey(
        @Header("Authorization") token: String = Chargebee.encodedApiKey,
        @Header("platform") platform: String = Chargebee.platform,
        @Header("sdkVersion") sdkVersion: String = Chargebee.sdkVersion,
        @Path("sdkKey") sdkKey: String, @Path("customerId") customerId: String
    ): Response<KeyValidationWrapper?>

    @GET("v2/in_app_subscriptions/{subscription_id}")
    suspend fun retrieveSubscription(
        @Header("Authorization") token: String = Chargebee.encodedApiKey,
        @Header("platform") platform: String = Chargebee.platform,
        @Header("sdkVersion") sdkVersion: String = Chargebee.sdkVersion,
        @Path("subscription_id") subscriptionId: String
    ): Response<SubscriptionDetailsWrapper?>
}