package com.chargebee.android.models

data class CBRestoreSubscription(val subscription_id: String, val plan_id: String, val store_status: StoreStatus)
data class CBRestorePurchases(val in_app_subscriptions: ArrayList<CBRestoreSubscription>)

enum class StoreStatus{
    active,
    in_trial,
    cancelled,
    paused
}