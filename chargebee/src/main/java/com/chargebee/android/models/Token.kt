package com.chargebee.android.models

import com.chargebee.android.*
import com.chargebee.android.Failure
import com.chargebee.android.gateway.GatewayTokenizer
import com.chargebee.android.Success
import com.chargebee.android.exceptions.CBException
import com.chargebee.android.exceptions.InvalidRequestException
import com.chargebee.android.exceptions.OperationFailedException
import com.chargebee.android.exceptions.PaymentException
import com.chargebee.android.resources.MerchantPaymentConfigResource
import com.chargebee.android.resources.TempTokenResource
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