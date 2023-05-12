package com.chargebee.android.models

data class PurchaseTransaction(
    val productId: List<String>,
    val purchaseTime: Long,
    val purchaseToken: String,
    val productType: String
)
