package com.test.chargebee.repository

import com.test.chargebee.CBEnvironment
import com.test.chargebee.models.TokenWrapper
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

internal interface TokenRepository {

    @FormUrlEncoded
    @POST("v2/tokens/create_using_temp_token")
    suspend fun create(
        @Header("Authorization") token: String = CBEnvironment.encodedApiKey,
        @Field("id_at_vault") gatewayToken: String,
        @Field("payment_method_type") paymentMethodType: String,
        @Field("gateway_account_id") gatewayId: String
    ): Response<TokenWrapper?>
}