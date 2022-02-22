package com.chargebee.android.models

import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.exceptions.InvalidRequestException
import com.chargebee.android.exceptions.OperationFailedException
import com.chargebee.android.loggers.CBLogger
import com.chargebee.android.resources.PurchaseResource
import com.chargebee.android.resources.SubscriptionResource

class SubscriptionDetail(val id: String,val customer_id: String, val status: String, val current_term_start: String, val current_term_end: String,
        val activated_at: String, val plan_amount: String) {
    companion object{
        @JvmStatic
        @Throws(InvalidRequestException::class, OperationFailedException::class)
        fun retrieveSubscription(subscriptionId: String, completion: (ChargebeeResult<Any>) -> Unit) {
            val logger = CBLogger(name = "Subscription", action = "Fetch Subscription")
            ResultHandler.safeExecuter({ SubscriptionResource().retrieveSubscription(subscriptionId) }, completion, logger)
        }
    }
}

data class SubscriptionDetailsWrapper(val cb_subscription: SubscriptionDetail)