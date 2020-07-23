package com.chargebee.android.models

import com.chargebee.android.CBResult
import com.chargebee.android.exceptions.InvalidRequestException
import com.chargebee.android.exceptions.OperationFailedException
import com.chargebee.android.loggers.CBLogger
import com.chargebee.android.resources.AddonResource

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
            val logger = CBLogger(name = "addon", action = "retrieve_addon")
            ResultHandler.safeExecute({ AddonResource().retrieve(addonId) }, handler, logger)
        }
    }
}

internal data class AddonWrapper(val addon: Addon)
