package com.chargebee.android.repository

import com.chargebee.android.CBEnvironment
import com.chargebee.android.models.TokenWrapper
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