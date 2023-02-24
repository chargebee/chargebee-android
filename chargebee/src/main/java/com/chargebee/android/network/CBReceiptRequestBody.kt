package com.chargebee.android.network

internal class CBReceiptRequestBody(   val receipt: String,
                                       val productId: String,
                                       val customerData: CBCustomer?,
                                       val channel: String) {
    companion object {
        fun fromCBReceiptReqBody(params: Params): CBReceiptRequestBody {
            return CBReceiptRequestBody(
               params.receipt,
                params.productId,
                params.customerData,
                params.channel
            )
        }
    }

    fun toCBReceiptReqBody(): Map<String, String?> {
        return mapOf(
            "receipt" to this.receipt,
            "product[id]" to this.productId,
            "customer[id]" to this.customerData?.id,
            "customer[first_name]" to this.customerData?.firstName,
            "customer[last_name]" to this.customerData?.lastName,
            "customer[email]" to this.customerData?.email,
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
    val customerData: CBCustomer?,
    val channel: String
)
data class CBCustomer(
    val id: String?,
    val firstName: String?,
    val lastName: String?,
    val email: String?
)