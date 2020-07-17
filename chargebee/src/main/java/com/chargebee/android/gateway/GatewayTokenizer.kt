package com.chargebee.android.gateway

import com.chargebee.android.models.CBGatewayDetail
import com.chargebee.android.models.PaymentDetail
import com.chargebee.android.resources.StripeResource

internal class GatewayTokenizer {
    internal suspend fun createToken(detail: PaymentDetail, paymentConfig: CBGatewayDetail): String {
        val createToken = StripeResource()
            .createToken(detail, paymentConfig)
        return createToken.getData().id
    }
}