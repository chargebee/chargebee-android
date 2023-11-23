package com.chargebee.android.models

data class PurchaseProductParams(
    val product: CBProduct,
    val offerToken: String? = null
)