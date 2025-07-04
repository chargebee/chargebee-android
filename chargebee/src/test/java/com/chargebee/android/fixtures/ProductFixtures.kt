package com.chargebee.android.fixtures

import com.chargebee.android.billingservice.ProductType
import com.chargebee.android.models.CBProduct
import com.chargebee.android.models.PricingPhase
import com.chargebee.android.models.SubscriptionOffer

val subsPricingPhase: PricingPhase = PricingPhase(formattedPrice = "1100.0 INR", amountInMicros = 1100000, currencyCode = "INR")
val subscriptionOffers: List<SubscriptionOffer> = arrayListOf(SubscriptionOffer("basePlanId", "offerId", "offerToken", arrayListOf(subsPricingPhase)))
val oneTimePurchaseOffer: PricingPhase = PricingPhase(formattedPrice = "100.0 INR", amountInMicros = 100000, currencyCode = "INR")
val otpProducts = CBProduct("test.consumable","Example product",
    "Description",ProductType.INAPP,null, oneTimePurchaseOffer)
val subProducts = CBProduct("chargebee.premium.android","Premium Plan",
    "Description",ProductType.SUBS, subscriptionOffers, null)

