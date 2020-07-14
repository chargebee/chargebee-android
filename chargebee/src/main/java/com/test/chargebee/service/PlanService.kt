package com.test.chargebee.service

import com.test.chargebee.CBEnvironment
import com.test.chargebee.models.PlanWrapper
import okhttp3.Credentials
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface PlanService {

    @GET("v2/plans/{planId}")
    fun retrievePlan(
        @Header("Authorization") token: String = CBEnvironment.encodedApiKey,
        @Path("planId") planId: String
    ): Call<PlanWrapper?>?
}