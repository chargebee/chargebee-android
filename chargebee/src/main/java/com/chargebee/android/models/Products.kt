package com.chargebee.android.models

import com.android.billingclient.api.SkuDetails

data class CBProduct(
    val productId: String,
    val productTitle: String,
    val productPrice: String,
    var skuDetails: SkuDetails,
    var subStatus: Boolean,
    var productType: String
)