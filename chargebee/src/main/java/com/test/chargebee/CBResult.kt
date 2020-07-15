package com.test.chargebee

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.test.chargebee.exceptions.CBException
import com.test.chargebee.exceptions.InvalidRequestException
import com.test.chargebee.exceptions.OperationFailedException
import com.test.chargebee.exceptions.PaymentException
import com.test.chargebee.gateway.stripe.StripeError
import retrofit2.Response

interface CBResult<T> {
    @Throws(CBException::class)
    fun getData(): T
}

internal class Success<T>(private val value: T) : CBResult<T> {

    override fun getData(): T {
        return value
    }
}

internal class Failure<T>(private val exception:CBException? = null, private val error: CBError? = null, private val statusCode: Int = 400) : CBResult<T> {
    override fun getData(): T {
        if (exception != null) {
            throw exception
        }
        val commonError = error?.toCBError(statusCode) ?: CBErrorDetail("")
        when {
            (error is CBInternalErrorWrapper || error is CBErrorDetail) && statusCode in 400..499 -> throw InvalidRequestException(commonError)
            error is StripeError -> throw PaymentException(commonError)
            else -> throw OperationFailedException(commonError)
        }
    }
}

internal fun <T, E : CBError> fromResponse(response: Response<T?>, type: Class<E>): CBResult<T> {
    if (response.isSuccessful) {
        val value = response.body()
        if (value != null)
            return Success(value)
        // TODO: Fix message
        return Failure(error=CBErrorDetail("Empty response"), statusCode = 400)
    }
    val errorBody = response.errorBody()
    val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()
    val adapter = gson.getAdapter(type)
    val errorParser = adapter.fromJson(errorBody?.string())
    return Failure(error = errorParser, statusCode = response.code())
}
