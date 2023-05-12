package com.chargebee.android.resources

import com.chargebee.android.Chargebee
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.repository.PurchaseRepository
import com.chargebee.android.responseFromServer

internal class RestorePurchaseResource : BaseResource(Chargebee.baseUrl) {

    internal suspend fun retrieveStoreSubscription(purchaseToken: String): ChargebeeResult<Any> {
        val dataMap = convertToMap(purchaseToken)
        val response = apiClient.create(PurchaseRepository::class.java)
            .restoreSubscription(data = dataMap)
        return responseFromServer(
            response
        )
    }

    private fun convertToMap(receipt: String): Map<String, String> {
        return mapOf(
            "receipt" to receipt
        )
    }
}