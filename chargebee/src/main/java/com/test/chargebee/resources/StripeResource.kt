package com.test.chargebee.resources

import com.test.chargebee.CBResult
import com.test.chargebee.StripeErrorDetailWrapper
import com.test.chargebee.fromResponse
import com.test.chargebee.models.CBGatewayDetail
import com.test.chargebee.models.CBPaymentDetail
import com.test.chargebee.models.StripeCard
import com.test.chargebee.models.StripeToken
import com.test.chargebee.repository.StripeRepository

internal class StripeResource() : BaseResource("https://api.stripe.com/v1/") {

    internal suspend fun createToken(detail: CBPaymentDetail, gatewayInfo: CBGatewayDetail): CBResult<StripeToken> {
        val card = StripeCard.fromCBCard(detail.card)
        val bearerToken = "Bearer ${gatewayInfo.clientId}"

        val gatewayToken = apiClient.create(StripeRepository::class.java)
            .createToken(bearerToken, card.toFormBody())
        return fromResponse(
            gatewayToken,
            StripeErrorDetailWrapper::class.java
        )
    }

}