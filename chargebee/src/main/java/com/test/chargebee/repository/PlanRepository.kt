package com.test.chargebee.repository

import com.test.chargebee.CBEnvironment
import com.test.chargebee.models.PlanWrapper
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

internal interface PlanRepository {

    @GET("v2/plans/{planId}")
    suspend fun retrievePlan(
        @Header("Authorization") token: String = CBEnvironment.encodedApiKey,
        @Path("planId") planId: String
    ): Response<PlanWrapper?>
}