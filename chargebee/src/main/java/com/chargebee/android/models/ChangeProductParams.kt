package com.chargebee.android.models

data class ChangeProductParams(
    val purchaseProductParams: PurchaseProductParams,
    val oldProductId: String
)