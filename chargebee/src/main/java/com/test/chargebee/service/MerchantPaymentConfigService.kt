package com.test.chargebee.service

import com.test.chargebee.CBEnvironment
import com.test.chargebee.models.CBMerchantPaymentConfig
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers

internal interface MerchantPaymentConfigService {

    @Headers("X-Requested-With: XMLHttpRequest")
    @GET("internal/component/retrieve_config")
    suspend fun retrieveConfig(
        @Header("Authorization") token: String = "Basic ${CBEnvironment.apiKey}"
    ): Response<CBMerchantPaymentConfig?>
}