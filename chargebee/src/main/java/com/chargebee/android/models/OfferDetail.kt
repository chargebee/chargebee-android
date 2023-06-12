package com.chargebee.android.models

data class OfferDetail(
    val introductoryPrice: String?,
    val introductoryPriceAmountMicros: Long?,
    val introductoryPricePeriod: Int?,
    val introductoryOfferType: String?
)
