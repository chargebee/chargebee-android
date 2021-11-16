package com.chargebee.android.models

import com.android.billingclient.api.SkuDetails

data class Products(val productId: String,val productTitle:String, val productPrice: String, var skuDetails: SkuDetails, var subStatus: Boolean )  {
}