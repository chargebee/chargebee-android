package com.chargebee.android.network

import android.text.TextUtils
import com.chargebee.android.Chargebee
import com.chargebee.android.ErrorDetail
import com.chargebee.android.exceptions.CBException
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.exceptions.InvalidRequestException
import com.chargebee.android.exceptions.OperationFailedException
import com.chargebee.android.loggers.CBLogger
import com.chargebee.android.models.ResultHandler
import com.chargebee.android.resources.AuthResource

data class CBAuthentication(val resource_id: String, val app_id: String,
                            val app_name: String, val webhook_url: String,
                            val status: String, val product_catalog_version: String) {
    companion object {
        @JvmStatic
        @Throws(InvalidRequestException::class, OperationFailedException::class)
        fun authenticate(auth: Auth, completion : (ChargebeeResult<Any>) -> Unit) {
            val logger = CBLogger(name = "Authentication", action = "Authenticate SDK Key")
            verifyAppDetails(auth, logger,completion)
        }
        @Throws(InvalidRequestException::class, OperationFailedException::class)
        fun isSDKKeyValid(sdkKey: String, completion : (ChargebeeResult<Any>) -> Unit) {
            val logger = CBLogger(name = "Authentication", action = "Authenticate SDK Key")
            val auth = Auth(sdkKey, Chargebee.applicationId, Chargebee.appName, Chargebee.channel)
            verifyAppDetails(auth, logger,completion)
        }

        private fun verifyAppDetails(auth: Auth, logger: CBLogger?=null, completion : (ChargebeeResult<Any>) -> Unit) {
            if (TextUtils.isEmpty(Chargebee.appName))
                completion(ChargebeeResult.Error(
                    exp = CBException(
                        error = ErrorDetail(message = "App Name is empty", apiErrorCode = "400")
                    )))
            else if (TextUtils.isEmpty(Chargebee.sdkKey))
                completion(ChargebeeResult.Error(
                    exp = CBException(
                        error = ErrorDetail(message = "SDK Key is empty", apiErrorCode = "400")
                    )))
            else if (TextUtils.isEmpty(Chargebee.applicationId))
                completion(ChargebeeResult.Error(
                    exp = CBException(
                        error = ErrorDetail(message = "Application ID is empty", apiErrorCode = "400")
                    )))
            else
                ResultHandler.safeExecuter({  AuthResource().authenticate(auth) }, completion, logger)
        }
    }
}
data class CBAuthResponse(val in_app_detail: CBAuthentication)
