package com.test.chargebee

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.test.chargebee.models.CBGatewayDetail
import com.test.chargebee.models.CBMerchantPaymentConfig
import com.test.chargebee.models.CBPaymentDetail
import com.test.chargebee.service.MerchantPaymentConfigService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TokenHandler {

    fun tokenize(detail: CBPaymentDetail, completion: (CBResult<String>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val merchantPaymentConfig = retrieveConfig()
                val paymentProviderConfig =
                    merchantPaymentConfig.getPaymentProviderConfig(detail.currencyCode, detail.type)
                        ?: throw CBException(CBErrorDetail("Unable to retrieve gateway info for given payment details"))
                val gatewayToken = retrieveGatewayToken(detail, paymentProviderConfig)
                val cbTempToken = TempTokenHandler().createTempToken(
                    gatewayToken,
                    detail.type,
                    paymentProviderConfig.gatewayId
                )
                completion(Success(cbTempToken))
            } catch (ex: CBException) {
                completion(Failure(ex))
            } catch( ex: Exception) {
                completion(Failure(error=CBErrorDetail("Unknown Exception")))
            }
        }
    }

    private suspend fun retrieveGatewayToken(detail: CBPaymentDetail, paymentConfig: CBGatewayDetail): String {
        val createToken = StripeHandler().createToken(detail, paymentConfig)
        return createToken.getData().id
    }

    private suspend fun retrieveConfig(): CBMerchantPaymentConfig {
        val gson: Gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()
        val retrofit = Retrofit.Builder()
            .baseUrl(CBEnvironment.baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        val service = retrofit.create(MerchantPaymentConfigService::class.java)
        val paymentConfig = service.retrieveConfig()
        val result = fromResponse(paymentConfig, CBInternalErrorWrapper::class.java)
        return result.getData()
    }
}