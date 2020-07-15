package com.test.chargebee

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.test.chargebee.models.CBGatewayDetail
import com.test.chargebee.models.CBPaymentDetail
import com.test.chargebee.models.StripeCard
import com.test.chargebee.models.StripeToken
import com.test.chargebee.service.StripeService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal class StripeHandler {

    internal suspend fun createToken(detail: CBPaymentDetail, gatewayInfo: CBGatewayDetail): CBResult<StripeToken> {
        val gson: Gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.stripe.com/v1/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val service = retrofit.create(StripeService::class.java)
        val card = StripeCard.fromCBCard(detail.card)
        val bearerToken = "Bearer ${gatewayInfo.clientId}"
        val gatewayToken = service.createToken(bearerToken, card.toFormBody())
        return fromResponse(gatewayToken, StripeErrorDetailWrapper::class.java)
    }
}