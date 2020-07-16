package com.chargebee.android;

internal interface ChargebeeError {
    fun toCBError(statusCode: Int): ErrorDetail
}

data class ErrorDetail(
    val message: String,
    val type: String? = null,
    val apiErrorCode: String? = null,
    val param: String? = null,
    val httpStatusCode: Int? = null
) : ChargebeeError {
    override fun toCBError(statusCode: Int): ErrorDetail {
        return this
    }
}

internal data class InternalErrorWrapper(val errors: Array<InternalErrorDetail>): ChargebeeError {
    override fun toCBError(statusCode: Int): ErrorDetail {
        val message = errors.getOrNull(0)?.message ?: ""
        return ErrorDetail(message, httpStatusCode=statusCode)
    }
}

internal data class InternalErrorDetail(val message: String)
