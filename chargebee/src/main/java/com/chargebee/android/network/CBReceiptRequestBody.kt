package com.chargebee.android.network

import com.chargebee.android.models.OfferDetail

internal class CBReceiptRequestBody(   val receipt: String,
                                       val productId: String,
                                       val customer: CBCustomer?,
                                       val channel: String,
                                       val offerDetail: OfferDetail?) {
    companion object {
        fun fromCBReceiptReqBody(params: Params): CBReceiptRequestBody {
            return CBReceiptRequestBody(
               params.receipt,
                params.productId,
                params.customer,
                params.channel,
                params.offerDetail
            )
        }
    }

    fun toCBReceiptReqBody(): Map<String, String?> {
        return mapOf(
            "receipt" to this.receipt,
            "product[id]" to this.productId,
            "customer[id]" to this.customer?.id,
            "channel" to this.channel
        )
    }

    fun toCBReceiptReqCustomerBody(): Map<String, String?> {
        val queryMap = mutableMapOf(
            "receipt" to this.receipt,
            "product[id]" to this.productId,
            "customer[id]" to this.customer?.id,
            "customer[first_name]" to this.customer?.firstName,
            "customer[last_name]" to this.customer?.lastName,
            "customer[email]" to this.customer?.email,
            "channel" to this.channel
        )
        return getOfferParams(queryMap)
    }
    fun toMap(): Map<String, String?> {
        val queryMap = mutableMapOf<String, String?>(
            "receipt" to this.receipt,
            "product[id]" to this.productId,
            "channel" to this.channel
        )
        return getOfferParams(queryMap)
    }

    private fun getOfferParams(queryMap: MutableMap<String, String?>) : Map<String, String?>{
        if (offerDetail != null) {
            queryMap["introductory_offer[price]"] = this.offerDetail.introductoryPriceAmountMicros.toString()
            queryMap["introductory_offer[type]"] = this.offerDetail.introductoryOfferType.value
            queryMap["introductory_offer[period]"] = this.offerDetail.introductoryPricePeriod.toString()
        }
        return queryMap
    }
}

data class Params(
    val receipt: String,
    val productId: String,
    val customer: CBCustomer?,
    val channel: String,
    val offerDetail: OfferDetail?
)
data class CBCustomer(
    val id: String?,
    val firstName: String?,
    val lastName: String?,
    val email: String?
)