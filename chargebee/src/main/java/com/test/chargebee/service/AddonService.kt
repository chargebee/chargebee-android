package com.test.chargebee.service

import com.test.chargebee.models.AddonWrapper
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface AddonService {

    @GET("addons/{addonId}")
    fun retrieveAddon(
        @Header("Authorization") token: String = getTokenValue(),
        @Path("addonId") addonId: String
    ): Call<AddonWrapper?>?
}
