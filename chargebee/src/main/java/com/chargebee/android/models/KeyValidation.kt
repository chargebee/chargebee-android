package com.chargebee.android.models

import com.chargebee.android.CBResult
import com.chargebee.android.exceptions.InvalidRequestException
import com.chargebee.android.exceptions.OperationFailedException
import com.chargebee.android.loggers.CBLogger
import com.chargebee.android.resources.PurchaseResource

class KeyValidation(val boolean: Boolean) {
    companion object {
        @JvmStatic
        @Throws(InvalidRequestException::class, OperationFailedException::class)
        fun validateSdkKey(sdkKey: String,customerId: String, completion: (CBResult<KeyValidation>) -> Unit) {
            val logger = CBLogger(name = "sdkKey", action = "sdk key validation")
            ResultHandler.safeExecute({ PurchaseResource().validateSDKKey(sdkKey, customerId) }, completion, logger)
        }

    }
}
internal data class KeyValidationWrapper(val validation: KeyValidation)