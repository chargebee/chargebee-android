package com.chargebee.android.models

data class ChangeProductParams(
    val newProductParams: PurchaseProductParams,
    var currentProductId: String
)