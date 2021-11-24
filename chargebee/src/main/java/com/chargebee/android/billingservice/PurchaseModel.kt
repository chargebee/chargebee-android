package com.chargebee.android.billingservice

import com.android.billingclient.api.AccountIdentifiers

data class PurchaseModel(
    val purchaseToken: String,
    val isAck: Boolean,
    val purchaseTime: Long,
    val accountIdentifiers: AccountIdentifiers,
    val orderId: String,
    val isAutoRenew: Boolean,
    val packageName: String
) {
}