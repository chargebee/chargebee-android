package com.test.chargebee.service

import com.test.chargebee.models.StripeToken
import retrofit2.Response
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

interface StripeRepository {

    @FormUrlEncoded
    @POST("tokens")
    suspend fun createToken(
        @Header("Authorization") bearerToken: String,
        @FieldMap body: Map<String, String>
    ): Response<StripeToken?>
}