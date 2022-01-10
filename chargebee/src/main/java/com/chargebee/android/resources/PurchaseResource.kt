package com.chargebee.android.resources

import android.util.Log
import com.chargebee.android.*
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.models.KeyValidation
import com.chargebee.android.repository.PurchaseRepository

internal class PurchaseResource : BaseResource(Chargebee.baseUrl) {
    suspend fun validateSDKKey(sdkKey: String, customerId: String): CBResult<KeyValidation> {
        val planResponse = apiClient.create(PurchaseRepository::class.java).validateSDKKey(sdkKey = sdkKey,customerId = customerId )
        Log.i(javaClass.simpleName, " Response validateSDKKey() :$planResponse")
        val result = fromResponse(
            planResponse,
            ErrorDetail::class.java
        )
        return Success(result.getData().validation)
    }
    suspend fun retrieveSubscription(subscriptionId: String): ChargebeeResult<Any> {
        val subscriptionResponse = apiClient.create(PurchaseRepository::class.java).retrieveSubscription(subscriptionId = subscriptionId )
        return responseFromServer(
            subscriptionResponse,
            ErrorDetail::class.java)
    }
}