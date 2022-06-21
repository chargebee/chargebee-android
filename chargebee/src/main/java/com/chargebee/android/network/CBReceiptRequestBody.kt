package com.chargebee.android.network

internal class CBReceiptRequestBody(   val receipt: String,
                                       val productId: String,
                                       val customerId: String,
                                       val channel: String) {
    companion object {
        fun fromCBReceiptReqBody(params: Params): CBReceiptRequestBody {
            return CBReceiptRequestBody(
               params.receipt,
                params.productId,
                params.customerId,
                params.channel
            )
        }
    }

    fun toCBReceiptReqBody(): Map<String, String> {
        return mapOf(
            "receipt" to this.receipt,
            "product[id]" to this.productId,
            "customer[id]" to this.customerId,
            "channel" to this.channel
        )
    }
    fun toMap(): Map<String, String> {
        return mapOf(
            "receipt" to this.receipt,
            "product[id]" to this.productId,
            "channel" to this.channel
        )
    }
}

data class Params(
    val receipt: String,
    val productId: String,
    val customerId: String,
    val channel: String
)