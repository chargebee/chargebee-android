package com.chargebee.android.models

import com.chargebee.android.CBResult
import com.chargebee.android.Success
import com.chargebee.android.exceptions.InvalidRequestException
import com.chargebee.android.exceptions.OperationFailedException
import com.chargebee.android.exceptions.PaymentException
import com.chargebee.android.gateway.GatewayTokenizer
import com.chargebee.android.loggers.CBLogger
import com.chargebee.android.resources.MerchantPaymentConfigResource
import com.chargebee.android.resources.TempTokenResource

class Token {
    companion object {
        @JvmStatic
        @Throws(InvalidRequestException::class, OperationFailedException::class, PaymentException::class)
        fun createTempToken(detail: PaymentDetail, completion: (CBResult<String>) -> Unit) {
            val logger = CBLogger(name = "cb_temp_token", action = "create_temp_token")
            ResultHandler.safeExecute({
                val paymentConfig =
                    MerchantPaymentConfigResource().retrieve(detail.currencyCode, detail.type)
                val gatewayToken = GatewayTokenizer().createToken(detail, paymentConfig)
                val cbTempToken = TempTokenResource().create(
                    gatewayToken,
                    detail.type,
                    paymentConfig.gatewayId
                )
                Success(cbTempToken)
            }, completion, logger)
        }
    }
}