package com.chargebee.android.loggers

import com.chargebee.android.models.ResultHandler
import com.chargebee.android.resources.LogType
import com.chargebee.android.resources.LoggerResource

internal class CBLogger(private val name: String,
            private val action: String) {

    fun error(message: String, code: Int? = null) {
        ResultHandler.safeExecute({
            LoggerResource().log(
                action,
                LogType.ERROR,
                message,
                code
            )
        }, {})
    }
}
