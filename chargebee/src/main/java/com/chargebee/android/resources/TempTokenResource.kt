package com.chargebee.android.resources

import com.chargebee.android.Chargebee
import com.chargebee.android.ErrorDetail
import com.chargebee.android.fromResponse
import com.chargebee.android.models.PaymentMethodType
import com.chargebee.android.repository.TokenRepository

internal class TempTokenResource : BaseResource(Chargebee.baseUrl) {

    suspend fun create(gatewayToken: String, paymentMethod: PaymentMethodType, gatewayId: String ): String {
        val service = apiClient.create(TokenRepository::class.java)
        val createTempToken = service.create(
            gatewayId = gatewayId,
            gatewayToken = gatewayToken,
            paymentMethodType = paymentMethod.displayName
        )
        val result = fromResponse(
            createTempToken,
            ErrorDetail::class.java
        )
        return result.getData().token.id
    }
}