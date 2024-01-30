package com.chargebee.android.models

data class ChangeProductParams(
    val newProductParams: PurchaseProductParams,
    val currentProductId: String
)