# Chargebee Android
The official Chargebee Android SDK.

After installing and initializing the SDK with the Chargebee site authentication, this SDK can be used for,

1. Integrating with Play Store, processing in-app purchase subscriptions, and tracking them on your Chargebee account for a single source of subscription truth across Web subscriptions & Android IAP. Use this if you are selling digital goods or services, or are REQUIRED to use Google Play's in-app purchases.

2. Tokenizing credit card information while presenting your own UI. Use this if you are selling physical goods or offline services, or are NOT REQUIRED to use Google's in-app purchases.

## Requirements
* Android 5.0 (API level 21) and above
* [Android Gradle Plugin](https://developer.android.com/studio/releases/gradle-plugin) 4.0.0
* [Gradle](https://gradle.org/releases/) 6.1.1+
* [AndroidX](https://developer.android.com/jetpack/androidx/)
* Java 8+ and Kotlin

## Installation
The `Chargebee-Android` SDK can be installed by adding below dependency to the `build.gradle` file:

```kotlin
implementation 'com.chargebee:chargebee-android:0.1.0'
```

## Example project
To run the example project, clone the repo, and run the gradle build first.

## Configuration

### Configuration for using In-App Purchases
To use the Chargebee Android SDK for making and managing in-app purchases, you must initialize the SDK with your Chargebee Site, full access API key and the SDK Key. You can find your API key, or create a new one, in your Chargebee account under Configure Chargebee > API Keys . Once you setup the Google Play Store integration on your Chargebee account, you can find the SDK Key under the name of App ID when you click Set up notifications.

You can initialize the SDK during your app startup by including the following in your app delegate.

```kotlin
import Chargebee

Chargebee.configure(site= "your-site",
                    fullAccessApiKey= "api_key",
                    sdkKey= "sdk_key")
```
### Configuration for using tokenization only
If you want to use the Chargebee Android SDK only for tokenizing credit card details, you can initialize the SDK with your Chargebee Site and Publishable API key alone. You can initialize the SDK during your app startup by including this in Android application class' onCreate method.

```kotlin
import com.chargebee.android.Chargebee

Chargebee.configure(site = "your-site", publishableApiKey = "api_key")

```
## Usage

### Integrating In-App Purchases

### Get all IAP Product Identifiers from Chargebee

Every In-App Purchase subscription product that you configure in your Play Store account, can be configured in Chargebee as a Plan. Start by retrieving the Google IAP Product IDs from your Chargebee account.

```kotlin
CBPurchase.retrieveProductIDs(queryParam) {
    when (it) {
        is CBProductIDResult.ProductIds -> {
            val array = it.IDs.toTypedArray()
        }
        is CBProductIDResult.Error -> {
           Log.e(TAG, " ${it.exp.message}")
            // Handle error here
        }
    }
}
```
For eg. query params above can be "limit": "100".

The above function will determine your product catalog version in Chargebee and hit the relevant APIs automatically, to retrieve the Chargebee Plans that correspond to Google IAP products, along with their Google IAP Product IDs.
### Get IAP Products
You can then convert these to Google IAP Product objects with the following function.

```kotlin
CBPurchase.retrieveProducts(this, productIdList,
      object : CBCallback.ListProductsCallback<ArrayList<Products>> {
               override fun onSuccess(productDetails: ArrayList<Products>) {
                     Log.i(TAG, "List of Products:  $productDetails")
                }
                override fun onError(error: CBException) {
                    Log.e(TAG, "Error:  ${error.message}")
                   // Handle error here
                }
            })
            
```
You can present any of the above products to your users for them to purchase.

### Buy / Subscribe Product
When the user chooses the product to purchase, pass in the product and customer identifiers to the following function.

```kotlin
CBPurchase.purchaseProduct(param, object : CBCallback.PurchaseCallback<PurchaseModel>{
      override fun onSuccess(data: PurchaseModel) {
        Log.i(TAG, "subscription details:  ${data.status}")       
      }
      override fun onError(error: CBException) {
        Log.e(TAG, "Error:  ${error.message}")
        // Handle error here    
      }
})
 ```
The above function will handle the purchase against Google Play Store, and send the IAP token for server-side token verification to your Chargebee account.

### Get Subscription Status
Use the Subscription ID returned by the previous function, to check for Subscription status against Chargebee, and for delivering purchased entitlements.

```kotlin
SubscriptionDetail.retrieveSubscription(subscriptionId) {
       when(it){
             is ChargebeeResult.Success ->{
                  Log.i(TAG, "subscription status:  ${it.status}")
             }
             is ChargebeeResult.Error ->{
                  Log.e(TAG, "Error :  ${it.exp.message}")
                  // Handle error here  
             }
       }
}   
```
### Integrating credit card tokenization
The following section describes how to use the SDK to directly tokenize credit card information if you are NOT REQUIRED to use Google's in-app purchases.

If you are using Product Catalog 2.0 in your Chargebee site, then you can use the following functions to retrieve the product to be presented for users to purchase.

### Get all Items

```kotlin
Items.retrieveAllItems(queryParam) {
       when (it) {
           is ChargebeeResult.Success -> {
                 Log.i(javaClass.simpleName, "list items :  ${it.data}")
           }
           is ChargebeeResult.Error -> {
                 Log.d(javaClass.simpleName, "Error :  ${it.exp.message}")
           }
       }
}  
```
For eg. query params above can be "sort_by[desc]" : "name" OR "limit": "100".

### Get Item Details

```kotlin
Items.retrieveItem(queryParam) {
       when (it) {
           is ChargebeeResult.Success -> {
                 Log.i(javaClass.simpleName, "item details :  ${it.data}")
           }
           is ChargebeeResult.Error -> {
                 Log.d(javaClass.simpleName, "Error :  ${it.exp.message}")
           }
       }
}  
```
If you are using Product Catalog 1.0 in your Chargebee site, then you can use any of the following relevant functions to retrieve the product to be presented for users to purchase.

### Get All Plans

```kotlin
Plan.retrieveAllPlans(queryParam) {
       when (it) {
           is ChargebeeResult.Success -> {
                 Log.i(javaClass.simpleName, "list Plans :  ${it.data}")
           }
           is ChargebeeResult.Error -> {
                 Log.d(javaClass.simpleName, "Error :  ${it.exp.message}")
           }
       }
}  
```
For eg. query params above can be "sort_by[desc]" : "name" OR "limit": "100".

### Get Plan Details

```kotlin
Plan.retrievePlan(queryParam) {
       when (it) {
           is ChargebeeResult.Success -> {
                 Log.i(javaClass.simpleName, "Plan details :  ${it.data}")
           }
           is ChargebeeResult.Error -> {
                 Log.d(javaClass.simpleName, "Error :  ${it.exp.message}")
           }
       }
}  
```

### Get Addon Details

```kotlin
Addon.retrieve("addonId") { addonResult ->
    try {
        val addon = addonResult.getData()
        Log.d("success", addon.toString())
        // Use addon details here
    } catch (ex: CBException) {
        Log.d("error", ex.getMessage());
        // Handle error here
    }
}
```

### Get Payment Token
Once the user selects the product to purchase, and you collect the credit card information, use the following function to tokenize the credit card details against Stripe. You need to have connected your Stripe account to your Chargebee site.

```kotlin

val card = Card(
    number = "4321567890123456",
    expiryMonth = "12",
    expiryYear = "29",
    cvc = "123")
val paymentDetail = PaymentDetail(
    currencyCode = "USD",
    type = PaymentMethodType.CARD,
    card = card)
Token.createTempToken(paymentDetail) { tokenResult ->
    try {
        val cbTempToken = tokenResult.getData()
        Log.d("success", cbTempToken)
        // Use token here
    } catch (ex: PaymentException) {
        Log.d("error payment", ex.toString())
        // Handle PaymentException here
    } catch (ex: InvalidRequestException) {
        Log.d("error invalid", ex.toString())
        // Handle InvalidRequestException here
    } catch (ex: OperationFailedException) {
        Log.d("error operation", ex.toString())
        // Handle OperationFailedException here
    }
}
```
## Use the Chargebee Token
Once your customer’s card data is processed and stored, and a Chargebee token reference is returned to you, you can use the token in subsequent API calls to process transactions. The following are some endpoints that accept Chargebee tokens for processing.

- [Create a Payment Source for the customer](https://apidocs.chargebee.com/docs/api/payment_sources#create_using_chargebee_token)
- [Create a Subscription](https://apidocs.chargebee.com/docs/api/subscriptions#create_a_subscription)
- [Update a Subscription](https://apidocs.chargebee.com/docs/api/subscriptions#update_a_subscription)

Please refer to the [Chargebee API Docs](https://apidocs.chargebee.com/docs/api) for subsequent integration steps.

## License

Chargebee is available under the MIT license. See the LICENSE file for more info.
