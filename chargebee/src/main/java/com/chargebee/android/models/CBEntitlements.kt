package com.chargebee.android.models

data class SubscriptionEntitlements(val subscription_id: String, val feature_id: String, val feature_name: String, val feature_description: String,
            val feature_type: String, val value: String, val name: String, val is_overridden: Boolean, val is_enabled: Boolean, val objectName: String)

data class CBEntitlementsWrapper(val subscription_entitlement: SubscriptionEntitlements)

data class CBEntitlements(val list: ArrayList<CBEntitlementsWrapper>)