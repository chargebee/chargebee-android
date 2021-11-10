package com.chargebee.android.models

import com.chargebee.android.CBResult
import com.chargebee.android.exceptions.InvalidRequestException
import com.chargebee.android.exceptions.OperationFailedException
import com.chargebee.android.loggers.CBLogger
import com.chargebee.android.resources.PurchaseResource

class SubscriptionDetail(val subscriptionId: String) {
    companion object{
        @JvmStatic
        @Throws(InvalidRequestException::class, OperationFailedException::class)
        fun updatePurchaseToken(purchaseToken: String, completion: (CBResult<SubscriptionDetail>) -> Unit) {
            val logger = CBLogger(name = "plan", action = "retrieve_plan")
            ResultHandler.safeExecute({ PurchaseResource().updatePurchaseToken(purchaseToken) }, completion, logger)
        }
    }
}
internal data class SubscriptionDetailsWrapper(val validation: SubscriptionDetail)