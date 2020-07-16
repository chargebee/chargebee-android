package com.chargebee.android.models

import com.chargebee.android.CBResult
import com.chargebee.android.ErrorDetail
import com.chargebee.android.Failure
import com.chargebee.android.exceptions.CBException
import com.chargebee.android.exceptions.InvalidRequestException
import com.chargebee.android.exceptions.OperationFailedException
import com.chargebee.android.resources.AddonResource
import com.chargebee.android.resources.PlanResource
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
                try {
                    val result = PlanResource().retrieve(planId)
                    handler(result)
                } catch (ex: CBException) {
                    handler(Failure(ex))
                } catch (ex: Exception) {
                    handler(Failure(error = ErrorDetail("Unknown/Network exception")))
                }
            }
        }
    }
}

internal data class PlanWrapper(val plan: Plan)
