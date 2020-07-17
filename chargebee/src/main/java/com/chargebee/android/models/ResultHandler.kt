package com.chargebee.android.models

import com.chargebee.android.CBResult
import com.chargebee.android.ErrorDetail
import com.chargebee.android.Failure
import com.chargebee.android.exceptions.CBException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class ResultHandler {
    companion object {
        fun <T> safeExecute(
            codeBlock: suspend () -> CBResult<T>,
            completion: (CBResult<T>) -> Unit
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                val result: CBResult<T> = try {
                    codeBlock()
                } catch (ex: CBException) {
                    Failure(ex)
                } catch (ex: Exception) {
                    Failure(error = ErrorDetail("Unknown/Network exception"))
                }
                completion(result)
            }
        }
    }
}