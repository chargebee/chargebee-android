package com.chargebee.android.exceptions

import com.chargebee.android.ErrorDetail

open class CBException internal constructor(error: ErrorDetail) : RuntimeException(error.message) {
    val type: String? = error.type
    val apiErrorCode: String? = error.apiErrorCode
    val param: String? = error.param
    val httpStatusCode: Int? = error.httpStatusCode
}
