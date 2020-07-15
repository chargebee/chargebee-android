package com.test.chargebee.models

import com.test.chargebee.CBResult
import com.test.chargebee.exceptions.InvalidRequestException
import com.test.chargebee.exceptions.OperationFailedException
import com.test.chargebee.resources.PlanResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
    val resourceVersion: Long,
    val `object`: String,
    val chargeModel: String,
    val taxable: Boolean,
    val currencyCode: String,
    val showDescriptionInInvoices: Boolean,
    val showDescriptionInQuotes: Boolean
) {
    companion object {
        @JvmStatic
        @Throws(InvalidRequestException::class, OperationFailedException::class)
        fun retrieve(planId: String, handler: (CBResult<Plan>) -> Unit) {
            CoroutineScope(Dispatchers.IO).launch {
                val planResult = PlanResource().retrieve(planId)
                handler(planResult)
            }
        }
    }
}

internal data class PlanWrapper(val plan: Plan)
