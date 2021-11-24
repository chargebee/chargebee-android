package com.chargebee.android.network

data class ReceiptDetail(val subscription_id: String, val customer_id: String, val plan_id: String){

}

data class CBReceiptResponse(val in_app_subscription: ReceiptDetail)
