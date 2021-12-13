package com.chargebee.android.resources

import android.util.Log
import com.chargebee.android.CBResult
import com.chargebee.android.Chargebee
import com.chargebee.android.Success
import com.chargebee.android.repository.LogDetail
import com.chargebee.android.repository.LoggerRepository

internal class LoggerResource: BaseResource(Chargebee.baseUrl) {

    suspend fun log(action: String, type: LogType, errorMessage: String? = null, errorCode: Int? = null, deviceModelName: String? = null,
                    platform: String? = null,
                    osVersion: String? = null,
                    sdkVersion: String? = null): CBResult<String?> {
        var data = logData(action, type, errorMessage, errorCode,deviceModelName,platform, osVersion, sdkVersion)
        val logDetail = LogDetail(data = data)
        apiClient.create(LoggerRepository::class.java).log(logDetail = logDetail)
        return Success(null)
    }

    private fun logData(action: String, type: LogType, errorMessage: String?, errorCode: Int?,deviceModelName: String? = null,
                        platform: String? = null,
                        osVersion: String? = null,
                        sdkVersion: String? = null): MutableMap<String, String?> {
        var data = mutableMapOf(
            "key" to "cb.logging",
            "ref_module" to "cb_android_sdk",
            "site" to Chargebee.site,
            "action" to action,
            "log_data_type" to type.value,
            "device_model" to deviceModelName,
            "platform" to platform,
            "os_version" to osVersion,
            "sdk_version" to sdkVersion
        )
        errorMessage?.let { data["error_message"] = it }
        errorCode?.let { data["error_code"] = "$it" }
        Log.i(javaClass.simpleName, "logData :$data")
        return data
    }
}

enum class LogType(val value: String) {
    ERROR("error"),
    INFO("info")
}
