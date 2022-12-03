package com.chargebee.android.resources

import android.util.Log
import com.chargebee.android.Chargebee
import com.chargebee.android.ErrorDetail
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.repository.ItemsRepository
import com.chargebee.android.responseFromServer
import com.chargebee.android.billingservice.CBPurchase

internal class ItemsResource: BaseResource(Chargebee.baseUrl){

    suspend fun retrieveAllItems(params: Array<String>): ChargebeeResult<Any> {
        val param = CBPurchase.append(params)
        val itemsResponse = apiClient.create(ItemsRepository::class.java).retrieveAllItems(limit = param[0])

        Log.i(javaClass.simpleName, " Response :$itemsResponse")
        return responseFromServer(
            itemsResponse)
    }

    suspend fun retrieveItem(itemId: String): ChargebeeResult<Any> {
        val itemsResponse = apiClient.create(ItemsRepository::class.java).retrieveItem(itemId = itemId)

        Log.i(javaClass.simpleName, " Response :$itemsResponse")
        return responseFromServer(
            itemsResponse)
    }

}