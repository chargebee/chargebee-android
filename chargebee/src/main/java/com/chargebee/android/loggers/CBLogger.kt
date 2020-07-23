package com.chargebee.android.loggers

import com.chargebee.android.Chargebee
import com.chargebee.android.resources.LogType
import com.chargebee.android.resources.LoggerResource

internal class CBLogger(private val name: String,
            private val action: String) {

    suspend fun error(message: String, code: Int? = null) {
        postLog(LogType.ERROR, message, code)
    }

    suspend fun info() {
        postLog(LogType.INFO)
    }

    private suspend fun postLog(type: LogType, message: String? = null, code: Int? = null) {
        if (Chargebee.allowErrorLogging) {
            LoggerResource().log(
                action,
                type,
                message,
                code
            )
        }
    }
}
