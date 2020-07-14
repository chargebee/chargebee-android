package com.test.chargebee.service

import com.test.chargebee.models.StripeCard
import com.test.chargebee.models.StripeToken
import retrofit2.Call
import retrofit2.http.*

interface StripeService {

    @FormUrlEncoded
    @POST("tokens")
    fun createToken(
        @Header("Authorization") bearerToken: String,
        @FieldMap body: Map<String, String>
    ): Call<StripeToken?>?
}