package com.chargebee.android.billingservice

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.chargebee.android.ErrorDetail
import com.chargebee.android.exceptions.CBException

enum class GPErrorCode(val errorMsg: String) {
    BillingUnavailable("The Billing API version is not supported"),
    PurchasePending("Purchase is in pending state"),
    PurchaseUnspecified("Unspecified state of the purchase"),
    PurchaseInvalid("Failure of purchase"),
    PurchaseReceiptNotFound("Receipt not found"),
    CanceledPurchase("User pressed back or canceled a dialog"),
    ProductNotOwned("Failure to consume purchase since item is not owned"),
    ProductAlreadyOwned("Failure to purchase since item is already owned"),
    FeatureNotSupported("The requested feature is not supported by Play Store on the current device"),
    ProductUnavailable("Requested product is not available for purchase"),
    PlayServiceTimeOut("The request has reached the maximum timeout before Google Play responds"),
    PlayServiceUnavailable("Network connection is down"),
    LaunchBillingFlowError("Failed to launch billing flow, try again later"),
    UnknownError("Fatal error during the API action"),
    DeveloperError("Invalid arguments provided to the API"),
    BillingClientNotReady("Play services not available"),
    SDKKeyNotAvailable("SDK key not available to proceed purchase"),
    InvalidPurchaseToken("The Token data sent is not correct or Google service is temporarily down"),
    BillingServiceDisconnected("The app is not connected to Google play via Billing library")
}

internal enum class BillingErrorCode(val code: Int) {
    UNKNOWN(-4),
    SERVICE_TIMEOUT(-3),
    FEATURE_NOT_SUPPORTED(-2),
    USER_CANCELED(1),
    SERVICE_UNAVAILABLE(2),
    BILLING_UNAVAILABLE(3),
    ITEM_UNAVAILABLE(4),
    DEVELOPER_ERROR(5),
    ERROR(6),
    ITEM_NOT_OWNED(8),
    SERVICE_DISCONNECTED(-1),
    ITEM_ALREADY_OWNED(7);

    companion object {
        private fun billingResponseCode(responseCode: Int): BillingErrorCode =
            when (responseCode) {
                BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> SERVICE_TIMEOUT
                BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> FEATURE_NOT_SUPPORTED
                BillingClient.BillingResponseCode.USER_CANCELED -> USER_CANCELED
                BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> SERVICE_UNAVAILABLE
                BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> BILLING_UNAVAILABLE
                BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> ITEM_UNAVAILABLE
                BillingClient.BillingResponseCode.DEVELOPER_ERROR -> DEVELOPER_ERROR
                BillingClient.BillingResponseCode.ERROR -> ERROR
                BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> ITEM_NOT_OWNED
                BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> SERVICE_DISCONNECTED
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> ITEM_ALREADY_OWNED
                else -> {
                    UNKNOWN
                }
            }

        internal fun billingDebugMessage(responseCode: Int): String =
            when (responseCode) {
                BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> GPErrorCode.PlayServiceTimeOut.errorMsg
                BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> GPErrorCode.FeatureNotSupported.errorMsg
                BillingClient.BillingResponseCode.USER_CANCELED -> GPErrorCode.CanceledPurchase.errorMsg
                BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> GPErrorCode.PlayServiceUnavailable.errorMsg
                BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> GPErrorCode.BillingUnavailable.errorMsg
                BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> GPErrorCode.ProductUnavailable.errorMsg
                BillingClient.BillingResponseCode.DEVELOPER_ERROR -> GPErrorCode.DeveloperError.errorMsg
                BillingClient.BillingResponseCode.ERROR -> GPErrorCode.UnknownError.errorMsg
                BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> GPErrorCode.ProductNotOwned.errorMsg
                BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> GPErrorCode.BillingServiceDisconnected.errorMsg
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> GPErrorCode.ProductAlreadyOwned.errorMsg
                else -> {
                    GPErrorCode.UnknownError.errorMsg
                }
            }

        internal fun throwCBException(billingResult: BillingResult): CBException =
            CBException(
                ErrorDetail(
                    httpStatusCode = billingResponseCode(billingResult.responseCode).code,
                    message = billingDebugMessage(billingResult.responseCode)
                )
            )
    }
}