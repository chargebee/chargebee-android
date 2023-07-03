package com.chargebee.android.models

import com.google.gson.annotations.SerializedName

data class NonSubscriptionResponse(
    @SerializedName("invoice_id")
    val invoiceId: String,
    @SerializedName("customer_id")
    val customerId: String,
    @SerializedName("charge_id")
    val chargeId: String
)

data class CBNonSubscriptionResponse(
    @SerializedName("non_subscription")
    val nonSubscription: NonSubscriptionResponse
)
