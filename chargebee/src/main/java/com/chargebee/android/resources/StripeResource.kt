package com.chargebee.android.resources

import com.chargebee.android.CBResult
import com.chargebee.android.fromResponse
import com.chargebee.android.gateway.stripe.StripeCard
import com.chargebee.android.gateway.stripe.StripeError
import com.chargebee.android.gateway.stripe.StripeToken
import com.chargebee.android.models.CBGatewayDetail
import com.chargebee.android.models.PaymentDetail
import com.chargebee.android.repository.StripeRepository

internal class StripeResource() : BaseResource("https://api.stripe.com/v1/") {

    internal suspend fun createToken(detail: PaymentDetail, gatewayInfo: CBGatewayDetail): CBResult<StripeToken> {
        val card = StripeCard.fromCBCard(detail.card)
        val bearerToken = "Bearer ${gatewayInfo.clientId}"

        val gatewayToken = apiClient.create(StripeRepository::class.java)
            .createToken(bearerToken, card.toFormBody())
        return fromResponse(
            gatewayToken,
            StripeError::class.java
        )
    }

}