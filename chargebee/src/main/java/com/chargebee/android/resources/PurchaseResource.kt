package com.chargebee.android.resources

import com.chargebee.android.*
import com.chargebee.android.repository.PurchaseRepository

internal class PurchaseResource : BaseResource(Chargebee.baseUrl) {
    suspend fun validateSDKKey(sdkKey: String, customerId: String) {
        val purchaseResource = apiClient.create(PurchaseRepository::class.java).validateSDKKey(sdkKey = sdkKey, customerId= customerId)

    }
}