package com.chargebee.android.billingservice

enum class GPErrorCode(val errorMsg: String) {
    UnknownError("Unknown error"),
    PlayStoreError("There was an issue with the Play Store service"),
    BillingUnavailable("The Billing service is unavailable on the device"),
    PurchasePending("Purchase is pending"),
    PurchaseUnspecified("Unspecified state of the purchase"),
    PurchaseInvalid("Failure of purchase"),
    CanceledPurchase("User pressed back or canceled a dialog for purchase"),
    ProductNotOwned("Failure to consume purchase since item is not owned"),
    ProductAlreadyOwned("Failure to purchase since item is already owned"),
    FeatureNotSupported("The requested feature is not supported"),
    ProductUnavailable("Requested product is not available for purchase or its SKU was not found"),
    ParseResponseFailed("A problem occurred when serializing or deserializing data"),
    ProductNotFound("Failure to purchase since the Chargebee product was not found"),
    LaunchError("There was an error on launching Chargebee SDK"),
    SkuDetailsError("Failure to retrieve SkuDetails for the in-app product ID"),
    InvalidCredentials("SDK key is invalid or not set"),
    InvalidClientUid("Client Uid is invalid or not set"),
}