package com.chargebee.android.repository

import com.chargebee.android.CBEnvironment
import com.chargebee.android.models.AddonWrapper
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
