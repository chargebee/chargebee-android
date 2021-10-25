package com.chargebee.android.exceptions

import com.chargebee.android.ErrorDetail
import com.chargebee.android.billingservice.PurchaseModel

sealed class CBPurchaseFailure {
    data class Success(val subscriptionId: PurchaseModel) : CBPurchaseFailure()
    /**
     * Product/SKU ID not found in store
     */
    data class ProductIDNotFoundException(val error: ErrorDetail?) : CBPurchaseFailure()

    /**
     * Item not available for purchase
     */
    data class ProductsNotFoundException(val error: ErrorDetail?) : CBPurchaseFailure()

    /**
     * Purchase request failed
     */
    data class SkRequestFailedException(val error: ErrorDetail?) : CBPurchaseFailure()
    
    data class CannotMakePaymentsException(val error: ErrorDetail?) : CBPurchaseFailure()
    data class NoProductToRestoreException(val error: ErrorDetail?) : CBPurchaseFailure()

    data class InvalidSDKKeyException(val error: ErrorDetail?) : CBPurchaseFailure()
    data class InvalidCustomerIdException(val error: ErrorDetail?) : CBPurchaseFailure()
    data class InvalidCatalogVersionException(val error: ErrorDetail?) : CBPurchaseFailure()

}

