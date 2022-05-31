package com.chargebee.android.models

import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.SkuDetails

//data class CBProduct(val productId: String,val productTitle:String, val productPrice: String, var skuDetails: SkuDetails, var subStatus: Boolean )  {
//}

data class CBProduct(val productId: String,val productTitle:String, val productPrice: String,val productDetails: ProductDetails)