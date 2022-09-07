package com.chargebee.android.resources

import android.util.Log
import com.chargebee.android.Chargebee
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.repository.PurchaseRepository
import com.chargebee.android.responseFromServer

internal class EntitlementsResource: BaseResource(baseUrl = Chargebee.baseUrl) {

    suspend fun retrieveEntitlements(subscriptionId: String): ChargebeeResult<Any> {
        val entitlementsResponse = apiClient.create(PurchaseRepository::class.java).retrieveEntitlements(subscriptionId = subscriptionId )
        Log.i(javaClass.simpleName, " Response :$entitlementsResponse")
        return responseFromServer(
            entitlementsResponse)
    }

}