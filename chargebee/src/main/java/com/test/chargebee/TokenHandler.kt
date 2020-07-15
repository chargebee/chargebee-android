package com.test.chargebee

import com.test.chargebee.models.CBPaymentDetail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TokenHandler {
    fun tokenize(detail: CBPaymentDetail, completion: (CBResult<String>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val paymentConfig = MerchantPaymentConfigResource().retrieve(detail.currencyCode, detail.type)
                val gatewayToken = GatewayTokenizer().createToken(detail, paymentConfig)
                val cbTempToken = TempTokenResource().create(gatewayToken, detail.type, paymentConfig.gatewayId)

                completion(Success(cbTempToken))
            } catch (ex: CBException) {
                completion(Failure(ex))
            } catch (ex: Exception) {
                completion(Failure(error = CBErrorDetail("Unknown Exception")))
            }
        }
    }
}