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
}

internal enum class BillingErrorCode(val code: Int, val message: String) {
    UNKNOWN(-4, "Unknown error occurred"),
    SERVICE_TIMEOUT(-3, "The request has reached the maximum timeout before Google Play responds"),
    FEATURE_NOT_SUPPORTED(
        -2,
        "The requested feature is not supported by the Play Store on the current device"
    ),
    USER_CANCELED(1, "Transaction was canceled by the user"),
    SERVICE_UNAVAILABLE(2, "The service is currently unavailable"),
    BILLING_UNAVAILABLE(3, "A user billing error occurred during processing"),
    ITEM_UNAVAILABLE(4, "The requested product is not available for purchase"),
    DEVELOPER_ERROR(5, "Error resulting from incorrect usage of the API"),
    ERROR(6, "Fatal error during the API action"),
    ITEM_NOT_OWNED(8, "Requested action on the item failed since it is not owned by the user"),
    SERVICE_DISCONNECTED(
        -1,
        "The app is not connected to the Play Store service via the Google Play Billing Library"
    ),
    ITEM_ALREADY_OWNED(7, "The purchase failed because the item is already owned");

    companion object {
        private fun errorDetail(responseCode: Int): ErrorDetail =
            when (responseCode) {
                BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> ErrorDetail(
                    message = SERVICE_TIMEOUT.message,
                    httpStatusCode = SERVICE_TIMEOUT.code
                )
                BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> ErrorDetail(
                    message = FEATURE_NOT_SUPPORTED.message,
                    httpStatusCode = FEATURE_NOT_SUPPORTED.code
                )
                BillingClient.BillingResponseCode.USER_CANCELED -> ErrorDetail(
                    message = USER_CANCELED.message,
                    httpStatusCode = USER_CANCELED.code
                )
                BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> ErrorDetail(
                    message = SERVICE_UNAVAILABLE.message,
                    httpStatusCode = SERVICE_UNAVAILABLE.code
                )
                BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> ErrorDetail(
                    message = BILLING_UNAVAILABLE.message,
                    httpStatusCode = BILLING_UNAVAILABLE.code
                )
                BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> ErrorDetail(
                    message = ITEM_UNAVAILABLE.message,
                    httpStatusCode = ITEM_UNAVAILABLE.code
                )
                BillingClient.BillingResponseCode.DEVELOPER_ERROR -> ErrorDetail(
                    message = DEVELOPER_ERROR.message,
                    httpStatusCode = DEVELOPER_ERROR.code
                )
                BillingClient.BillingResponseCode.ERROR -> ErrorDetail(
                    message = ERROR.message,
                    httpStatusCode = ERROR.code
                )
                BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> ErrorDetail(
                    message = ITEM_NOT_OWNED.message,
                    httpStatusCode = ITEM_NOT_OWNED.code
                )
                BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> ErrorDetail(
                    message = SERVICE_DISCONNECTED.message,
                    httpStatusCode = SERVICE_DISCONNECTED.code
                )
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> ErrorDetail(
                    message = ITEM_ALREADY_OWNED.message,
                    httpStatusCode = ITEM_ALREADY_OWNED.code
                )
                else -> {
                    ErrorDetail(message = UNKNOWN.message, httpStatusCode = UNKNOWN.code)
                }
            }

        internal fun throwCBException(billingResult: BillingResult): CBException =
            CBException(
                errorDetail(billingResult.responseCode)
            )
    }
}