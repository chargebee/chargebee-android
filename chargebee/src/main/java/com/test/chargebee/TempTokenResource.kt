package com.test.chargebee

import com.test.chargebee.models.CBPaymentMethodType
import com.test.chargebee.service.TokenService

internal class TempTokenResource : BaseResource(CBEnvironment.baseUrl) {

    suspend fun create(gatewayToken: String, paymentMethod: CBPaymentMethodType, gatewayId: String ): String {
        val service = apiClient.create(TokenService::class.java)
        val createTempToken = service.create(
            gatewayId = gatewayId,
            gatewayToken = gatewayToken,
            paymentMethodType = paymentMethod.displayName
        )
        val result = fromResponse(createTempToken, CBErrorDetail::class.java)
        return result.getData().token.id
    }
}