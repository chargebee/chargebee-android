package com.chargebee.android.resources

import com.chargebee.android.Chargebee
import com.chargebee.android.ErrorDetail
import com.chargebee.android.InternalErrorWrapper
import com.chargebee.android.exceptions.InvalidRequestException
import com.chargebee.android.fromResponse
import com.chargebee.android.models.CBGatewayDetail
import com.chargebee.android.models.MerchantPaymentConfig
import com.chargebee.android.models.PaymentMethodType
import com.chargebee.android.repository.MerchantPaymentConfigRepository

internal class MerchantPaymentConfigResource: BaseResource(Chargebee.baseUrl) {

    suspend fun retrieve(currencyCode: String, paymentType: PaymentMethodType): CBGatewayDetail {
        val merchantPaymentConfig = retrieveConfig()
        return merchantPaymentConfig.getPaymentProviderConfig(currencyCode, paymentType)
            ?: throw InvalidRequestException(
                ErrorDetail("Unable to retrieve gateway info for given payment details")
            )
    }

    private suspend fun retrieveConfig(): MerchantPaymentConfig {
        val paymentConfig =
            apiClient.create(MerchantPaymentConfigRepository::class.java).retrieveConfig()
        val result = fromResponse(
            paymentConfig,
            InternalErrorWrapper::class.java
        )
        return result.getData()
    }
}