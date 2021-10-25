package com.chargebee.android.billingservice

data class PurchaseModel(val purchaseToken: String,val isAck:Boolean, val purchaseTime: Long) {
}