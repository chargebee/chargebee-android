package com.chargebee.android.repository

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

internal interface LoggerRepository {

    @POST("internal/track_info_error")
    suspend fun log(@Body logDetail: LogDetail): Response<Void>
}

internal data class LogDetail(val data: Map<String, String?>,
                            val type: String = "kvl")