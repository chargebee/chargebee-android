# Migration Guide

## From Billing Library 4 to Billing Library 5

### SDK version <tag>v1.2.1</tag> to <tag>v2.0.0</tag>

With the introduction of Google Play Billing Library 5, Google has significantly revamped the structure used to define subscription products.

## Why this migration:

- Starting from August 2, 2023, no new apps will be accepted on GBL4.
- Effective November 1, 2023, existing apps in GBL4 will be unable to release updates.

<!-- ## Subscription Structure
![Subscription structure with multiple plans and offers](https://mychargebee.atlassian.net/37e1a015-df7b-434c-a172-d18b221fdbed#media-blob-url=true&id=c5fe33a8-c007-468d-846f-7edb56b935de&collection=contentId-3302425431&contextId=3302425431&width=684&height=380&alt=") -->

## Changes in CBProduct class:

- A single subscription has the capability to include multiple base plans, each of which can have multiple offers.
- It is also possible for different base plans to have same frequency.
- The “Product ID” is required for fetching the subscription offer details configured for it.
- The user would be able to subscribe to any of the multiple subscription offers for that product ID.
- When the user tries to purchase any other base plan + offer under the same product, the user is prompted with a “Change Subscription” flow.

### Currenct version

    data class CBProduct(
        val productId: String,
        val productTitle: String,
        val productPrice: String,
        var skuDetails: SkuDetails,
        var subStatus: Boolean,
        var productType: ProductType
    )

> - **_SkuDetails:_** This is merchant convenience. Not needed if we are to return all required details.
> - **_subStatus:_** This is not needed. This has been added for the example app.

### New version

### Option 1: Flatten out the CB product based on basePlanID and offer combination

    data class CBProduct(
        val id: String,
        val title: String,
        val description: String,
        var type: ProductType,
        val price: Price,
        val offer: Offer?,
        val productDetails: ProductDetails
    )

    data class Price(val formattedPrice: String, val amountInMicros: Long, val currencyCode: String)

    data class Offer(val basePlanId: String, val id: String?, val token: String)

> - **_ProductDetails:_** This is only for the merchant convenience. Not needed if we can return all required details.

### Option 2: Follow Google Play Product Detail structure

    data class CBProduct(
        val id: String,
        val title: String,
        val description: String,
        val type: ProductType,
        val subscriptionOffers: List<SubscriptionOffer>?,
        val oneTimePurchaseOffer: PricingPhase?,
    )

    data class SubscriptionOffer(
        val basePlanId: String,
        val offerId: String?,
        val offerToken: String,
        val pricingPhases: List<PricingPhase>
    )
    data class PricingPhase(
        val formattedPrice: String,
        val amountInMicros: Long,
        val currencyCode: String,
        val billingPeriod: String? = null,
        val billingCycleCount: Int? = null
    )

### Option 3: Return Google Product as is

[ProductDetails | Android Developers](https://developer.android.com/reference/com/android/billingclient/api/ProductDetails)

## Changes in Purchase product:

### Currenct version

    fun purchaseProduct(
        product: CBProduct,
        customer: CBCustomer? = null,
        callback: CBCallback.PurchaseCallback<String>
    )

    fun purchaseNonProduct(
        product: CBProduct,
        productType: ProductType,
        customer: CBCustomer? = null,
        callback: CBCallback.PurchaseCallback<String>
    )

### New version

### Option 1: Using same signature

- We can use the same signature if the CBProduct is flattened

### Option 2: If CBProduct follows the ProductDetails, or if ProductDetails is returned directly:

> - **_purchaseProduct:_** This uses PurchaseProductParams(wrapper for CBProduct).
> - **_purchaseNonProduct:_** This still uses CBProduct.

    fun purchaseProduct(
        purchaseProductParams: PurchaseProductParams,
        customer: CBCustomer? = null,
        callback: CBCallback.PurchaseCallback<String>
    )

    class PurchaseProductParams(
        val product: CBProduct,
        val offerToken: String? = null
    )

### Deprecations

- ***fun purchaseProduct(product: CBProduct, customer: CBCustomer, callback: CBCallback.PurchaseCallback<String>)*** will be removed in upcoming release.
- Please use this API instead ***fun purchaseProduct(purchaseProductParams: PurchaseProductParams,customer: CBCustomer? = null, callback: CBCallback.PurchaseCallback<String>)***
