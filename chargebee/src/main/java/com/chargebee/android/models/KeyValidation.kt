package com.chargebee.android.models

import com.chargebee.android.exceptions.InvalidRequestException
import com.chargebee.android.exceptions.OperationFailedException
import com.chargebee.android.resources.PurchaseResource

class KeyValidation(val boolean: Boolean) {
    companion object {
        @JvmStatic
        @Throws(InvalidRequestException::class, OperationFailedException::class)
        suspend fun validateSdkKey(sdkKey: String, customerId: String) {
            PurchaseResource().validateSDKKey(sdkKey, customerId)
        }

    }
}
internal data class KeyValidationWrapper(val validation: KeyValidation)