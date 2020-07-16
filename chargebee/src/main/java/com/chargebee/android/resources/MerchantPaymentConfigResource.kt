package com.chargebee.android.resources

import com.chargebee.android.CBEnvironment
import com.chargebee.android.CBErrorDetail
import com.chargebee.android.CBInternalErrorWrapper
import com.chargebee.android.exceptions.InvalidRequestException
import com.chargebee.android.fromResponse
import com.chargebee.android.models.CBGatewayDetail
import com.chargebee.android.models.CBMerchantPaymentConfig
import com.chargebee.android.models.CBPaymentMethodType
import com.chargebee.android.repository.MerchantPaymentConfigRepository

internal class MerchantPaymentConfigResource: BaseResource(CBEnvironment.baseUrl) {

    suspend fun retrieve(currencyCode: String, paymentType: CBPaymentMethodType): CBGatewayDetail {
        val merchantPaymentConfig = retrieveConfig()
        return merchantPaymentConfig.getPaymentProviderConfig(currencyCode, paymentType)
            ?: throw InvalidRequestException(
                CBErrorDetail("Unable to retrieve gateway info for given payment details")
            )
    }

    private suspend fun retrieveConfig(): CBMerchantPaymentConfig {
        val paymentConfig =
            apiClient.create(MerchantPaymentConfigRepository::class.java).retrieveConfig()
        val result = fromResponse(
            paymentConfig,
            CBInternalErrorWrapper::class.java
        )
        return result.getData()
    }
}