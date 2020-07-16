package com.chargebee.android.gateway.stripe

import com.chargebee.android.ChargebeeError
import com.chargebee.android.ErrorDetail

internal data class StripeError(
    val error: StripeErrorDetail
) : ChargebeeError {
    override fun toCBError(statusCode: Int): ErrorDetail {
        return ErrorDetail(error.message, error.type, error.code, error.param, statusCode)
    }
}

internal data class StripeErrorDetail(
    val message: String,
    val code: String? = null,
    val param: String? = null,
    val type: String? = null
)