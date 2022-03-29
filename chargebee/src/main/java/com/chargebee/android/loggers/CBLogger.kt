package com.chargebee.android.loggers

import com.chargebee.android.Chargebee
import com.chargebee.android.resources.LogType
import com.chargebee.android.resources.LoggerResource

class CBLogger(private val name: String,
            private val action: String) {

    suspend fun error(message: String, code: Int? = null) {
        postLog(LogType.ERROR, message, code)
    }

    suspend fun info() {
        postLog(LogType.INFO)
    }

    private suspend fun postLog(
        type: LogType,
        message: String? = null,
        code: Int? = null,
        deviceModelName: String? = null,
        platform: String? = null,
        osVersion: String? = null,
        sdkVersion: String? = null
    ) {
        if (Chargebee.allowErrorLogging) {
            LoggerResource().log(
                action,
                type,
                message,
                code,
                deviceModelName,
                platform,
                osVersion,
                sdkVersion
            )
        }
    }
}
