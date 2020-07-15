package com.test.chargebee

import com.test.chargebee.models.CBGatewayDetail
import com.test.chargebee.models.CBPaymentDetail

class GatewayTokenizer {
    internal suspend fun createToken(detail: CBPaymentDetail, paymentConfig: CBGatewayDetail): String {
        val createToken = StripeResource().createToken(detail, paymentConfig)
        return createToken.getData().id
    }
}