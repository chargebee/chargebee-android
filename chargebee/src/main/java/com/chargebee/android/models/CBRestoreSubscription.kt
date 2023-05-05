package com.chargebee.android.models

import com.google.gson.annotations.SerializedName

data class CBRestoreSubscription(
    @SerializedName("subscription_id")
    val subscriptionId: String,
    @SerializedName("plan_id")
    val planId: String,
    @SerializedName("store_status")
    val storeStatus: String
)

data class CBRestorePurchases(
    @SerializedName("in_app_subscriptions")
    val inAppSubscriptions: ArrayList<CBRestoreSubscription>
)

enum class StoreStatus(val value: String) {
    Active("active"),
    InTrial("in_trial"),
    Cancelled("cancelled"),
    Paused("paused")
}