package com.chargebee.android.models

import com.chargebee.android.CBResult
import com.chargebee.android.ErrorDetail
import com.chargebee.android.Failure
import com.chargebee.android.exceptions.CBException
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.loggers.CBLogger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.UnknownHostException

internal class ResultHandler {
    companion object {
        fun <T> safeExecute(
            codeBlock: suspend () -> CBResult<T>,
            completion: (CBResult<T>) -> Unit,
            logger: CBLogger? = null
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                val result: CBResult<T> = try {
                    logger?.info()
                    codeBlock()
                } catch (ex: CBException) {
                    logger?.error(ex.message ?: "failed", ex.httpStatusCode)
                    Failure(ex)
                } catch (ex: Exception) {
                    logger?.error(ex.message ?: "failed")
                    Failure(error = ErrorDetail("Unknown/Network exception"))
                }
                completion(result)
            }
        }
        fun <T> safeExecuter(
            codeBlock: suspend () -> ChargebeeResult<T>,
            completion: (ChargebeeResult<T>) -> Unit,
            logger: CBLogger? = null
        ) {
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    val result: ChargebeeResult<T> = try {
                        logger?.info()
                        codeBlock()
                    } catch (ex: CBException) {
                        logger?.error(ex.message ?: "failed", ex.httpStatusCode)
                        ChargebeeResult.Error(ex)
                    } catch (ex: UnknownHostException) {
                        print("failed: ${ex.message}")
                        ChargebeeResult.Error(exp = CBException(ErrorDetail(ex.message)))
                    } catch (ex: Exception) {
                        logger?.error(ex.message ?: "failed")
                        ChargebeeResult.Error(exp = CBException(ErrorDetail(ex.message)))
                    }
                    completion(result)
                }
            }catch (exp: Exception){
                print("failed: ${exp.message}")
                ChargebeeResult.Error(exp = CBException(ErrorDetail(exp.message)))
            }

        }
//        private fun coroutineExceptionHandler() : CoroutineExceptionHandler {
//            val coroutineExceptionHandler = CoroutineExceptionHandler{_, throwable ->
//                print("CoroutineExceptionHandler: ${throwable.message}")
//            }
//            return coroutineExceptionHandler
//        }
    }
}