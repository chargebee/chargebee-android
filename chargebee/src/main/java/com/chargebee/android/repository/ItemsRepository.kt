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

    @GET("v2/items")
    suspend fun retrieveAllItems(
        @Header("Authorization") token: String = Chargebee.encodedApiKey,
        @Query("limit") limit: String,
        @Query("sort_by[desc]") name: String
    ): Response<ItemsWrapper?>

    @GET("v2/items/{itemId}")
    suspend fun retrieveItem(
        @Header("Authorization") token: String = Chargebee.encodedApiKey,
        @Path("itemId") itemId: String
    ): Response<ItemWrapper?>


}