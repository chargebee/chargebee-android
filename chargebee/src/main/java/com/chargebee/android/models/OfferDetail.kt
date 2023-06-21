package com.chargebee.android.models

data class OfferDetail(
    val introductoryPrice: String,
    val introductoryPriceAmountMicros: Long,
    val introductoryPricePeriod: Int,
    val introductoryOfferType: OfferType
)

enum class OfferType(val value: String) {
    PAY_UP_FRONT("pay_up_front"),
    PAY_AS_YOU_GO("pay_as_you_go");
}
