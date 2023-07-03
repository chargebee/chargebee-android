package com.chargebee.android.billingservice

enum class OneTimeProductType(val value: String) {
    UNKNOWN(""),
    CONSUMABLE("consumable"),
    NON_CONSUMABLE("non_consumable")
}