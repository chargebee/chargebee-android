package com.test.chargebee.models

import com.test.chargebee.CBResult
import com.test.chargebee.exceptions.InvalidRequestException
import com.test.chargebee.exceptions.OperationFailedException
import com.test.chargebee.exceptions.PaymentException
import com.test.chargebee.resources.AddonResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
) {
    companion object {
        @JvmStatic
        @Throws(InvalidRequestException::class, OperationFailedException::class)
        fun retrieve(addonId: String, handler: (CBResult<Addon>) -> Unit) {
            CoroutineScope(Dispatchers.IO).launch {
                val addonResult = AddonResource().retrieve(addonId)
                handler(addonResult)
            }
        }
    }
}



internal data class AddonWrapper(val addon: Addon)
