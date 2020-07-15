package com.test.chargebee.resources

import com.test.chargebee.*
import com.test.chargebee.models.CBGatewayDetail
import com.test.chargebee.models.CBMerchantPaymentConfig
import com.test.chargebee.models.CBPaymentMethodType
import com.test.chargebee.service.MerchantPaymentConfigService

internal class MerchantPaymentConfigResource : BaseResource(CBEnvironment.baseUrl) {

    suspend fun retrieve(currencyCode: String, paymentType: CBPaymentMethodType): CBGatewayDetail {
        val merchantPaymentConfig = retrieveConfig()
        return merchantPaymentConfig.getPaymentProviderConfig(currencyCode, paymentType)
            ?: throw CBException(
                CBErrorDetail(
                    "Unable to retrieve gateway info for given payment details"
                )
            )
    }

    private suspend fun retrieveConfig(): CBMerchantPaymentConfig {
        val paymentConfig =
            apiClient.create(MerchantPaymentConfigService::class.java).retrieveConfig()
        val result = fromResponse(
            paymentConfig,
            CBInternalErrorWrapper::class.java
        )
        return result.getData()
    }
}