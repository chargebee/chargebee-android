package com.chargebee.android.models

class SubscriptionDetail(val id: String,val customer_id: String, val status: String, val current_term_start: String, val current_term_end: String,
        val activated_at: String, val plan_amount: String)

data class SubscriptionDetailsWrapper(val cb_subscription: SubscriptionDetail)

data class CBSubscription(val list: ArrayList<SubscriptionDetailsWrapper>)