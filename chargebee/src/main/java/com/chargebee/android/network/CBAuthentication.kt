package com.chargebee.android.network

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
        fun authenticate(sdkKey: Auth, completion : (ChargebeeResult<Any>) -> Unit) {
            val logger = CBLogger(name = "Authentication", action = "Authenticate SDK Key")
            ResultHandler.safeExecuter({  AuthResource().authenticate(sdkKey) }, completion, logger)
        }
        @Throws(InvalidRequestException::class, OperationFailedException::class)
        fun isSDKKeyValid(sdkKey: String, completion : (ChargebeeResult<Any>) -> Unit) {
            val logger = CBLogger(name = "configure", action = "configure")
            ResultHandler.safeExecuter({  AuthResource().authenticate(sdkKey) }, completion, logger)
        }
    }
}
data class CBAuthResponse(val in_app_detail: CBAuthentication)
