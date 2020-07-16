package com.chargebee.android.models

import com.google.gson.annotations.SerializedName

internal class MerchantPaymentConfig(
    val apmConfig: Map<String, PaymentConfigs>,
    val currencyList: Array<String>,
    val defaultCurrency: String
) {
    internal fun getPaymentProviderConfig (currencyCode: String, paymentMethodType: PaymentMethodType): CBGatewayDetail? {
        val paymentMethod = this.apmConfig[currencyCode]?.pmList?.find {
            it.type == paymentMethodType.displayName && it.gatewayName == "STRIPE"
        }

        return if (paymentMethod == null) null else CBGatewayDetail(
            paymentMethod.tokenizationConfig.STRIPE.clientId,
            paymentMethod.id
        )
    }
}

internal data class CBGatewayDetail(
    val clientId: String,
    val gatewayId: String
)

internal data class PaymentConfigs(
    val pmList: Array<PaymentMethod>
)

internal data class PaymentMethod(
    val type: String,
    val id: String,
    val gatewayName: String,
    val gatewayCurrency: String,
    val tokenizationConfig: TokenizationConfig
)

internal data class TokenizationConfig(
    @SerializedName("STRIPE") val STRIPE: PaymentProviderConfig
)

internal data class PaymentProviderConfig(
    val clientId: String
)
