package com.chargebee.android.resources

import android.util.Log
import com.chargebee.android.CBResult
import com.chargebee.android.Chargebee
import com.chargebee.android.Success
import com.chargebee.android.repository.LogDetail
import com.chargebee.android.repository.LoggerRepository

internal class LoggerResource: BaseResource(Chargebee.baseUrl) {

    suspend fun log(action: String, type: LogType, error_message: String, error_code: Int?): CBResult<String?> {
        val data = mapOf(
            "key" to "cb.logging",
            "ref_module" to "cb_android_sdk",
            "site" to Chargebee.site,
            "action" to action,
            "log_data_type" to type.value,
            "error_message" to error_message,
            "error_code" to "$error_code"
        )
        val logDetail = LogDetail(data = data)
        val response = apiClient.create(LoggerRepository::class.java).log(logDetail = logDetail)
        return Success(null)
    }
}

enum class LogType(val value: String) {
    ERROR("error")
}
