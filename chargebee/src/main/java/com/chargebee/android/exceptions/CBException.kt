package com.chargebee.android.exceptions

import com.chargebee.android.ErrorDetail

open class CBException(error: ErrorDetail) : RuntimeException(error.message) {
    val type: String? = error.type
    val apiErrorCode: String? = error.apiErrorCode
    val param: String? = error.param
    val httpStatusCode: Int? = error.httpStatusCode
}
