package com.test.chargebee.resources

import com.test.chargebee.CBEnvironment
import com.test.chargebee.CBErrorDetail
import com.test.chargebee.fromResponse
import com.test.chargebee.models.CBPaymentMethodType
import com.test.chargebee.repository.TokenRepository

internal class TempTokenResource : BaseResource(CBEnvironment.baseUrl) {

    suspend fun create(gatewayToken: String, paymentMethod: CBPaymentMethodType, gatewayId: String ): String {
        val service = apiClient.create(TokenRepository::class.java)
        val createTempToken = service.create(
            gatewayId = gatewayId,
            gatewayToken = gatewayToken,
            paymentMethodType = paymentMethod.displayName
        )
        val result = fromResponse(
            createTempToken,
            CBErrorDetail::class.java
        )
        return result.getData().token.id
    }
}