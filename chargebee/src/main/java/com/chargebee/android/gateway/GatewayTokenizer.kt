package com.chargebee.android.gateway

import com.chargebee.android.models.CBGatewayDetail
import com.chargebee.android.models.CBPaymentDetail
import com.chargebee.android.resources.StripeResource

internal class GatewayTokenizer {
    internal suspend fun createToken(detail: CBPaymentDetail, paymentConfig: CBGatewayDetail): String {
        val createToken = StripeResource()
            .createToken(detail, paymentConfig)
        return createToken.getData().id
    }
}