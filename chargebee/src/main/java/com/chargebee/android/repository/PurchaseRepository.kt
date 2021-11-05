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
        @Path("sdkKey") sdkKey: String, @Path("customerId") customerId: String
    ): Response<KeyValidationWrapper?>

    @GET("v2/plans/{purchaseToken}")
    suspend fun updatePurchaseToken(
        @Header("Authorization") token: String = Chargebee.encodedApiKey,
        @Path("purchaseToken") purchaseToken: String
    ): Response<SubscriptionDetailsWrapper?>
}