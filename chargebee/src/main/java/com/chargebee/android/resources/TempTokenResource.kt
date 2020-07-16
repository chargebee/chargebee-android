package com.chargebee.android.resources

import com.chargebee.android.CBEnvironment
import com.chargebee.android.CBErrorDetail
import com.chargebee.android.fromResponse
import com.chargebee.android.models.CBPaymentMethodType
import com.chargebee.android.repository.TokenRepository

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