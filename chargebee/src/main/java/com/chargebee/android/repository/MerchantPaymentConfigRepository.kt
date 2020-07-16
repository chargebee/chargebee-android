package com.chargebee.android.repository

import com.chargebee.android.Chargebee
import com.chargebee.android.models.MerchantPaymentConfig
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers

internal interface MerchantPaymentConfigRepository {

    @Headers("X-Requested-With: XMLHttpRequest")
    @GET("internal/component/retrieve_config")
    suspend fun retrieveConfig(
        @Header("Authorization") token: String = "Basic ${Chargebee.apiKey}"
    ): Response<MerchantPaymentConfig?>
}