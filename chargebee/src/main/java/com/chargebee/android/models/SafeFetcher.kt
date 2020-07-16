package com.chargebee.android.models

import com.chargebee.android.CBResult
import com.chargebee.android.ErrorDetail
import com.chargebee.android.Failure
import com.chargebee.android.exceptions.CBException

internal class SafeFetcher {
    companion object {
        suspend fun <T>safeExecute(
            handler: suspend () -> CBResult<T>
        ): CBResult<T> {
            return try {
                handler()
            } catch (ex: CBException) {
                Failure(ex)
            } catch (ex: Exception) {
                Failure(error = ErrorDetail("Unknown/Network exception"))
            }
        }
    }
}