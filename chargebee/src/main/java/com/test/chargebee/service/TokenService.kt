package com.test.chargebee.service

import com.test.chargebee.CBEnvironment
import com.test.chargebee.models.CBTokenWrapper
import retrofit2.Call
import retrofit2.http.*

interface TokenService {

    @FormUrlEncoded
    @POST("v2/tokens/create_using_temp_token")
    fun create(
        @Header("Authorization") token: String = CBEnvironment.encodedApiKey,
        @Field("id_at_vault") gatewayToken: String,
        @Field("payment_method_type") paymentMethodType: String,
        @Field("gateway_account_id") gatewayId: String
    ): Call<CBTokenWrapper?>?
}