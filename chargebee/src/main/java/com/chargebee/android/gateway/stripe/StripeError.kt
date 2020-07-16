package com.chargebee.android.gateway.stripe

import com.chargebee.android.CBError
import com.chargebee.android.CBErrorDetail

internal data class StripeError(
    val error: StripeErrorDetail
) : CBError {
    override fun toCBError(statusCode: Int): CBErrorDetail {
        return CBErrorDetail(error.message, error.type, error.code, error.param, statusCode)
    }
}

internal data class StripeErrorDetail(
    val message: String,
    val code: String? = null,
    val param: String? = null,
    val type: String? = null
)