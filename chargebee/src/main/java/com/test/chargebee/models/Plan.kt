package com.test.chargebee.models

public data class Plan(
    val id: String,
    val name: String,
    val invoice_name: String,
    val price: Int,
    val period: Int,
    val period_unit: String,
    val pricing_model: String,
    val free_quantity: Int,
    val setup_cost: Int,
    val status: String,
    val enabled_in_hosted_pages: Boolean,
    val enabled_in_portal: Boolean,
    val addon_applicability: String,
    val is_shippable: Boolean,
    val updated_at: Long,
    val giftable: Boolean,
    val resource_version: Long,
    val `object`: String,
    val charge_model: String,
    val taxable: Boolean,
    val currency_code: String,
    val show_description_in_invoices: Boolean,
    val show_description_in_quotes: Boolean
)

data class PlanWrapper(val plan: Plan)
