package com.chargebee.android.billingservice

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
}