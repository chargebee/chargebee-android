package com.chargebee.android.models

import com.chargebee.android.CBResult
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.exceptions.InvalidRequestException
import com.chargebee.android.exceptions.OperationFailedException
import com.chargebee.android.loggers.CBLogger
import com.chargebee.android.resources.PurchaseResource

class SubscriptionDetail(val id: String,val customer_id: String, val status: String) {
    companion object{
        @JvmStatic
        @Throws(InvalidRequestException::class, OperationFailedException::class)
        fun retrieveSubscription(subscriptionId: String, completion: (ChargebeeResult<Any>) -> Unit) {
            val logger = CBLogger(name = "subscription", action = "retrieve_subscription")
            ResultHandler.safeExecuter({ PurchaseResource().retrieveSubscription(subscriptionId) }, completion, logger)
        }
    }
}
internal data class SubscriptionDetailsWrapper(val subscription: SubscriptionDetail)