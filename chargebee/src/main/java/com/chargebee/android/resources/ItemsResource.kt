package com.chargebee.android.resources

import android.util.Log
import com.chargebee.android.Chargebee
import com.chargebee.android.ErrorDetail
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.repository.ItemsRepository
import com.chargebee.android.responseFromServer

internal class ItemsResource: BaseResource(Chargebee.baseUrl){

    suspend fun retrieveAllItems(params: Array<String>): ChargebeeResult<Any> {
        val itemsResponse = apiClient.create(ItemsRepository::class.java).retrieveAllItems(limit = params.get(0), name = params.get(1), channel=params.get(2))

        Log.i(javaClass.simpleName, " Response :$itemsResponse")
        return responseFromServer(
            itemsResponse,
            ErrorDetail::class.java)
    }

    suspend fun retrieveItem(itemId: String): ChargebeeResult<Any> {
        val itemsResponse = apiClient.create(ItemsRepository::class.java).retrieveItem(itemId = itemId)

        Log.i(javaClass.simpleName, " Response :$itemsResponse")
        return responseFromServer(
            itemsResponse,
            ErrorDetail::class.java)
    }

}