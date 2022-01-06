package com.chargebee.android.repository

import com.chargebee.android.Chargebee
import com.chargebee.android.models.ItemWrapper
import com.chargebee.android.models.ItemsWrapper
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface ItemsRepository {

    @GET("{version}/items")
    suspend fun retrieveAllItems(
        @Header("Authorization") token: String = Chargebee.encodedApiKey,
        @Header("platform") platform: String = Chargebee.platform,
        @Header("sdkVersion") sdkVersion: String = Chargebee.sdkVersion,
        @Path("version") version: String = Chargebee.version,
        @Query("limit") limit: String,
        @Query("sort_by[desc]") name: String,
        @Query("channel") channel: String
    ): Response<ItemsWrapper?>

    @GET("{version}/items/{itemId}")
    suspend fun retrieveItem(
        @Header("Authorization") token: String = Chargebee.encodedApiKey,
        @Header("platform") platform: String = Chargebee.platform,
        @Header("sdkVersion") sdkVersion: String = Chargebee.sdkVersion,
        @Path("version") version: String = Chargebee.version,
        @Path("itemId") itemId: String
    ): Response<ItemWrapper?>


}