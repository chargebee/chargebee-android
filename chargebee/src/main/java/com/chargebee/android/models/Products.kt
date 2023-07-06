package com.chargebee.android.models

import com.android.billingclient.api.SkuDetails
import com.chargebee.android.billingservice.ProductType

data class CBProduct(
    val productId: String,
    val productTitle: String,
    val productPrice: String,
    var skuDetails: SkuDetails,
    var subStatus: Boolean,
    var productType: ProductType
)