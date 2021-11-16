package com.chargebee.android.repository

import com.chargebee.android.Chargebee
import com.chargebee.android.models.ItemsWrapper
import com.chargebee.android.models.PlanWrapper
import com.chargebee.android.models.PlansWrapper
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

internal interface PlanRepository {

    @GET("v2/plans/{planId}")
    suspend fun retrievePlan(
        @Header("Authorization") token: String = Chargebee.encodedApiKey,
        @Path("planId") planId: String
    ): Response<PlanWrapper?>

    @GET("v2/plans")
    suspend fun retrieveAllPlans(
        @Header("Authorization") token: String = Chargebee.encodedApiKey,
        @Query("sort_by[desc]") sort: String,
        @Query("channel") channel: String
    ): Response<PlansWrapper?>
}