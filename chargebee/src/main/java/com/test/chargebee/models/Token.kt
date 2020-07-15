package com.test.chargebee.models

import com.test.chargebee.*
import com.test.chargebee.Failure
import com.test.chargebee.gateway.GatewayTokenizer
import com.test.chargebee.Success
import com.test.chargebee.exceptions.CBException
import com.test.chargebee.exceptions.InvalidRequestException
import com.test.chargebee.exceptions.OperationFailedException
import com.test.chargebee.exceptions.PaymentException
import com.test.chargebee.resources.MerchantPaymentConfigResource
import com.test.chargebee.resources.TempTokenResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Token {
    companion object {
        @JvmStatic
        @Throws(InvalidRequestException::class, OperationFailedException::class, PaymentException::class)
        fun createTempToken(detail: CBPaymentDetail, completion: (CBResult<String>) -> Unit) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val paymentConfig = MerchantPaymentConfigResource()
                        .retrieve(detail.currencyCode, detail.type)
                    val gatewayToken = GatewayTokenizer()
                        .createToken(detail, paymentConfig)
                    val cbTempToken = TempTokenResource()
                        .create(gatewayToken, detail.type, paymentConfig.gatewayId)
                    completion(Success(cbTempToken))
                } catch (ex: CBException) {
                    completion(Failure(ex))
                } catch (ex: Exception) {
                    completion(
                        Failure(
                            error = CBErrorDetail(
                                "Unknown Exception"
                            )
                        )
                    )
                }
            }
        }
    }
}