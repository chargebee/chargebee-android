package com.chargebee.android.models

import com.android.billingclient.api.ProductDetails
import com.chargebee.android.billingservice.ProductType

data class CBProduct(
    val productId: String,
    val productTitle: String,
    val productBasePlanId: String?,
    val productPrice: String,
    var productDetails: ProductDetails,
    var offerToken: String?,
    var subStatus: Boolean,
    var productType: ProductType
)