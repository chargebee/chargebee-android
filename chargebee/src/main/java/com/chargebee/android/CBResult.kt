package com.chargebee.android

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.chargebee.android.exceptions.CBException
import com.chargebee.android.exceptions.InvalidRequestException
import com.chargebee.android.exceptions.OperationFailedException
import com.chargebee.android.exceptions.PaymentException
import com.chargebee.android.gateway.stripe.StripeError
import retrofit2.Response
import java.lang.Exception

interface CBResult<T> {
    @Throws(PaymentException::class, InvalidRequestException::class, OperationFailedException::class, CBException::class)
    fun getData(): T
}

internal class Success<T>(private val value: T) : CBResult<T> {

    override fun getData(): T {
        return value
    }
}

internal class Failure<T>(
    private val exception: CBException? = null,
    private val error: ChargebeeError? = null,
    private val statusCode: Int = 400
) : CBResult<T> {
    override fun getData(): T {
        if (exception != null) {
            throw exception
        }
        val commonError = error?.toCBError(statusCode) ?: ErrorDetail("Unknown Error.")
        when {
            (error is InternalErrorWrapper || error is ErrorDetail)
                    && statusCode in 400..499 -> throw InvalidRequestException(commonError)
            error is StripeError -> throw PaymentException(commonError)
            else -> throw OperationFailedException(commonError)
        }
    }
}

internal fun <T, E : ChargebeeError> fromResponse(response: Response<T?>, type: Class<E>): CBResult<T> {
    if (response.isSuccessful) {
        val value = response.body()
        if (value != null)
            return Success(value)
        return Failure(error=ErrorDetail("No response body"), statusCode = 400)
    }
    val errorBody = response.errorBody()
    val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .setLenient()
        .create()
    val adapter = gson.getAdapter(type)
    val errorParser: ChargebeeError? = try {
        adapter.fromJson(errorBody?.string())
    } catch (ex: Exception){
        null
    }
    return Failure(error = errorParser, statusCode = response.code())
}
