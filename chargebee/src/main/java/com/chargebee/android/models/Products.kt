package com.chargebee.android.models

import com.chargebee.android.billingservice.ProductType

data class CBProduct(
    val id: String,
    val title: String,
    val description: String,
    val type: ProductType,
    val subscriptionOffers: List<SubscriptionOffer>?,
    val oneTimePurchaseOffer: PricingPhase?,
)

data class SubscriptionOffer(
    val basePlanId: String,
    val offerId: String?,
    val offerToken: String,
    val pricingPhases: List<PricingPhase>
)
data class PricingPhase(
    val formattedPrice: String,
    val amountInMicros: Long,
    val currencyCode: String,
    val billingPeriod: String? = null,
    val billingCycleCount: Int? = null
)
