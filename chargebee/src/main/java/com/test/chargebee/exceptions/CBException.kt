package com.test.chargebee.exceptions

import com.test.chargebee.CBErrorDetail

open class CBException internal constructor(error: CBErrorDetail) : RuntimeException(error.message) {
    val type: String? = error.type
    val apiErrorCode: String? = error.apiErrorCode
    val param: String? = error.param
    val httpStatusCode: Int? = error.httpStatusCode
}
