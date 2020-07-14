package com.test.chargebee.service

import com.test.chargebee.models.StripeCard
import com.test.chargebee.models.StripeToken
import retrofit2.Call
import retrofit2.http.*

interface StripeService {

    @FormUrlEncoded
    @POST("tokens")
    fun createToken(
        @Header("Authorization") merchantKey: String = "Bearer pk_test_F97JzyrPUIFE5KSlCxBj8Eq000LP8an6pJ",
        @FieldMap body: Map<String, String>
    ): Call<StripeToken?>?
}