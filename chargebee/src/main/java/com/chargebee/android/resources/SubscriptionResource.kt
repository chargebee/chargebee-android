package com.chargebee.android.resources

import android.util.Log
import com.chargebee.android.*
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.repository.PurchaseRepository
import com.chargebee.android.responseFromServer

internal class SubscriptionResource : BaseResource(Chargebee.baseUrl) {

    suspend fun retrieveSubscription(subscriptionId: String): ChargebeeResult<Any> {
        val subscriptionResponse = apiClient.create(PurchaseRepository::class.java)
            .retrieveSubscription(subscriptionId = subscriptionId)
        Log.i(javaClass.simpleName, " Response :$subscriptionResponse")
        return responseFromServer(
            subscriptionResponse
        )
    }

}