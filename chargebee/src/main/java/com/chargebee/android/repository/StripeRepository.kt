package com.chargebee.android.repository

import com.chargebee.android.gateway.stripe.StripeToken
import retrofit2.Response
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

internal interface StripeRepository {

    @FormUrlEncoded
    @POST("tokens")
    suspend fun createToken(
        @Header("Authorization") bearerToken: String,
        @FieldMap body: Map<String, String>
    ): Response<StripeToken?>
}