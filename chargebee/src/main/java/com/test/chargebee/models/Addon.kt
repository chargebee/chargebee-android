package com.test.chargebee.models

data class Addon(
    val id: String,
    val name: String,
    val invoiceName: String,
    val description: String,
    val pricingModel: String,
    val chargeType: String,
    val price: Int,
    val periodUnit: String,
    val status: String,
    val enabledInPortal: Boolean,
    val isShippable: Boolean,
    val updatedAt: Long,
    val resourceVersion: Long,
    val `object`: String,
    val currencyCode: String,
    val taxable: Boolean,
    val type: String,
    val showDescriptionInInvoices: Boolean,
    val showDescriptionInQuotes: Boolean
)

internal data class AddonWrapper(val addon: Addon)
