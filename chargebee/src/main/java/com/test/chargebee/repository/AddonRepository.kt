package com.test.chargebee.repository

import com.test.chargebee.CBEnvironment
import com.test.chargebee.models.AddonWrapper
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

internal interface AddonRepository {

    @GET("v2/addons/{addonId}")
    suspend fun retrieveAddon(
        @Header("Authorization") token: String = CBEnvironment.encodedApiKey,
        @Path("addonId") addonId: String
    ): Response<AddonWrapper?>
}
