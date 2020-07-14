package com.test.chargebee.models

import android.util.Log
import com.google.gson.annotations.SerializedName

public enum class CBPaymentType(val displayName: String) {
    CARD("card")
}

data class CBGatewayDetail(
    val clientIdval: String,
    val gatewayId: String
)

public class CBMerchantPaymentConfig(
    val apmConfig: Map<String, PaymentConfigs>,
    val currencyList: Array<String>,
    val defaultCurrency: String
) {
    fun getPaymentProviderConfig (currencyCode: String, paymentType: CBPaymentType): CBGatewayDetail? {
        val paymentMethod = this.apmConfig[currencyCode]?.pmList?.find {
            it.type == paymentType.displayName && it.gatewayName == "STRIPE"
        }

        return if (paymentMethod == null) null else CBGatewayDetail(
            paymentMethod.tokenizationConfig.STRIPE.clientId,
            paymentMethod.id
        )
    }
}

data class PaymentConfigs(
    val pmList: Array<PaymentMethod>
)

data class PaymentMethod(
    val type: String,
    val id: String,
    val gatewayName: String,
    val gatewayCurrency: String,
    val tokenizationConfig: TokenizationConfig
)

data class TokenizationConfig(
    @SerializedName("STRIPE") val STRIPE: PaymentProviderConfig
)

data class PaymentProviderConfig(
    val clientId: String
)
