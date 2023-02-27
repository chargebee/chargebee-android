package com.chargebee.android.exceptions

import com.chargebee.android.ErrorDetail

open class CBException(error: ErrorDetail) : RuntimeException(error.message) {
    val type: String? = error.type
    @Deprecated("This property will be removed in upcoming release, use httpStatusCode instead.")
    val apiErrorCode: String? = error.apiErrorCode
    val param: String? = error.param
    val httpStatusCode: Int? = error.httpStatusCode
}
