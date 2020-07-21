package com.chargebee.android.repository

import com.chargebee.android.Chargebee
import com.chargebee.android.models.AddonWrapper
import retrofit2.Response
import retrofit2.http.*

internal interface LoggerRepository {

    @POST("/internal/track_info_error")
    suspend fun log(@Body logDetail: LogDetail): Response<String?>
}

internal data class LogDetail(val data: Map<String, String>,
                            val type: String = "kvl")