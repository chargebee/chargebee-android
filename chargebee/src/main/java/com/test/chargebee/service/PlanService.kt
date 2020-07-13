package com.test.chargebee.service

import com.test.chargebee.CBEnvironment
import com.test.chargebee.models.PlanWrapper
import okhttp3.Credentials
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface PlanService {

    @GET("plans/{planId}")
    fun retrievePlan(
        @Header("Authorization") token: String = getTokenValue(),
        @Path("planId") planId: String
    ): Call<PlanWrapper?>?
}

fun getTokenValue(): String {
    return Credentials.basic(CBEnvironment.apiKey, "")
}