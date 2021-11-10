package com.chargebee.android.resources

import com.chargebee.android.*
import com.chargebee.android.models.KeyValidation
import com.chargebee.android.models.SubscriptionDetail
import com.chargebee.android.repository.PurchaseRepository

internal class PurchaseResource : BaseResource(Chargebee.baseUrl) {
    suspend fun validateSDKKey(sdkKey: String, customerId: String): CBResult<KeyValidation> {
        val planResponse = apiClient.create(PurchaseRepository::class.java).validateSDKKey(sdkKey = sdkKey,customerId = customerId )
        val result = fromResponse(
            planResponse,
            ErrorDetail::class.java
        )
        return Success(result.getData().validation)
    }
    suspend fun updatePurchaseToken(purchaseToken: String): CBResult<SubscriptionDetail> {
        val planResponse = apiClient.create(PurchaseRepository::class.java).updatePurchaseToken(purchaseToken = purchaseToken )
        val result = fromResponse(
            planResponse,
            ErrorDetail::class.java
        )
        return Success(result.getData().validation)
    }
}