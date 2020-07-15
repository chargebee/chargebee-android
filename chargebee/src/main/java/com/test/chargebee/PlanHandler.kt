package com.test.chargebee

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.test.chargebee.models.PlanWrapper
import com.test.chargebee.service.PlanService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PlanHandler {

    fun retrieve(planId: String, handler: (CBResult<PlanWrapper>) -> Unit) {
        retrieveInBackground(planId, handler)
    }

    private fun retrieveInBackground(planId: String, handler: (CBResult<PlanWrapper>) -> Unit) {
        val retrofit = Retrofit.Builder()
            .baseUrl(CBEnvironment.baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service: PlanService = retrofit.create(PlanService::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            val planWrapperResponse = service.retrievePlan(planId = planId)
            val result = fromResponse(planWrapperResponse, CBErrorDetail::class.java)
            handler(result)
        }
    }
}

interface CBError {
    fun convertCommonError(statusCode: Int): CBErrorDetail
}

data class CBErrorDetail(
    val message: String,
    val type: String? = null,
    val apiErrorCode: String? = null,
    val param: String? = null,
    val httpStatusCode: Int? = null
) : CBError {
    override fun convertCommonError(statusCode: Int): CBErrorDetail {
        return this
    }
}

data class StripeErrorDetailWrapper(
    val error: StripeErrorDetail
) : CBError {
    override fun convertCommonError(statusCode: Int): CBErrorDetail {
        return CBErrorDetail(error.message, error.type, error.code, error.param, statusCode)
    }
}

data class StripeErrorDetail(
    val message: String,
    val code: String? = null,
    val param: String? = null,
    val type: String? = null
)

class CBException(val error: CBError) : RuntimeException() {

}

interface CBResult<T> {
    @Throws(CBException::class)
    fun getData(): T
}

class Success<T>(private val value: T) : CBResult<T> {

    override fun getData(): T {
        return value
    }
}

class Failure<T>(private val error: CBError, private val statusCode: Int = 400) : CBResult<T> {
    override fun getData(): T {
        val commonError = error.convertCommonError(statusCode)
        throw CBException(commonError)
    }
}

fun <T, E : CBError> fromResponse(response: Response<T?>, type: Class<E>): CBResult<T> {
    if (response.isSuccessful) {
        val value = response.body()
        if (value != null)
            return Success(value)
        // TODO: Fix message
        return Failure(CBErrorDetail("Empty response"), 400)
    }
    val errorBody = response.errorBody()
    val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()
    val adapter = gson.getAdapter(type)
    val errorParser = adapter.fromJson(errorBody?.string())
    return Failure(errorParser, response.code())
}
