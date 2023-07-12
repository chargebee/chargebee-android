package com.chargebee.android.loggers

import android.os.Build
import com.chargebee.android.Chargebee
import com.chargebee.android.resources.LogType
import com.chargebee.android.resources.LoggerResource

class CBLogger(private val name: String,
               private val action: String,
               private val additionalInfo: Map<String, String>? = null) {

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
        deviceModelName: String? = Build.MODEL,
        platform: String? = Chargebee.platform,
        osVersion: String? = Build.VERSION.SDK_INT.toString(),
        sdkVersion: String? = Chargebee.sdkVersion
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
                sdkVersion,
                additionalInfo
            )
        }
    }
}
