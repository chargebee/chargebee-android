package com.chargebee.android.models

data class Plan(
    val id: String,
    val name: String,
    val invoiceName: String,
    val price: Int,
    val period: Int,
    val periodUnit: String,
    val pricingModel: String,
    val freeQuantity: Int,
    val setup_cost: Int,
    val status: String,
    val enabledInHostedPages: Boolean,
    val enabledInPortal: Boolean,
    val addonApplicability: String,
    val isShippable: Boolean,
    val updatedAt: Long,
    val giftable: Boolean,
    val channel: String,
    val resourceVersion: Long,
    val `object`: String,
    val chargeModel: String,
    val taxable: Boolean,
    val currencyCode: String,
    val showDescriptionInInvoices: Boolean,
    val showDescriptionInQuotes: Boolean
)

data class PlansWrapper(val list: ArrayList<PlanWrapper>)

data class PlanWrapper(val plan: Plan)
