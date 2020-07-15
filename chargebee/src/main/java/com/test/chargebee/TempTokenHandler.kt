package com.test.chargebee

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.test.chargebee.models.CBPaymentMethodType
import com.test.chargebee.service.TokenService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal class TempTokenHandler {

    internal suspend fun createTempToken(
        gatewayToken: String,
        paymentMethod: CBPaymentMethodType,
        gatewayId: String
    ): String {
        val gson: Gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(CBEnvironment.baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val service = retrofit.create(TokenService::class.java)
        val createTempToken = service.create(
            gatewayId = gatewayId,
            gatewayToken = gatewayToken,
            paymentMethodType = paymentMethod.displayName
        )
        val result = fromResponse(createTempToken, CBErrorDetail::class.java)
        return result.getData().token.id
    }
}