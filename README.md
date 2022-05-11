Chargebee Android
=================

This is Chargebee's Android Software Development Kit (SDK). This SDK makes it efficient and comfortable to build a seamless subscription experience in your Android app.

Post-installation, initialization, and authentication with the Chargebee site, this SDK will support the following process.

-   **Sync In-App Subscriptions with Chargebee**: [Integrate](https://www.chargebee.com/docs/2.0/mobile-playstore-connect.html#chargebee-configuration) Chargebee with Google Play Store to process in-app purchase subscriptions, and track them on your Chargebee account for a single source of truth for subscriptions across the Web and Google Play Store. Use this if you are selling digital goods or services, or are REQUIRED to use Google Play's in-app purchases as per their [app review guidelines](https://support.google.com/googleplay/android-developer/answer/9858738).
    **For SDK methods to work, ensure that **[**prerequisites**](https://www.chargebee.com/docs/2.0/mobile-playstore-connect.html#prerequisites-configuration)**  are configured in Chargebee.**

-   **Tokenisation of credit card**: Tokenize credit card information while presenting your user interface. Use this if you are selling physical goods or offline services or are NOT REQUIRED to use Google's in-app purchases as per their [app review guidelines](https://support.google.com/googleplay/android-developer/answer/9858738).

Requirements
------------

The following requirements must be set up before installing Chargebee's Android SDK.

-   [Android 5.0 (API level 21)](https://developer.android.com/studio/releases/platforms#5.0) and above
-   [Android Gradle Plugin](https://developer.android.com/studio/releases/gradle-plugin) 4.0.0
-   [Gradle](https://gradle.org/releases/) 6.1.1+
-   [AndroidX](https://developer.android.com/jetpack/androidx/)
-   [Java 8+](https://www.oracle.com/java/technologies/downloads/#java8) and [Kotlin](https://kotlinlang.org/)

Installation
------------

To install Chargebee's Android SDK, add the following dependency to the build.gradle file.

```
implementation 'com.chargebee:chargebee-android:1.0.2'
```

Example project
---------------

This is an optional step that helps you  verify the SDK implementation using this example project. You can download or clone the example project via GitHub.

To run the example project, follow these steps.

1.  Clone the repo - https://github.com/chargebee/chargebee-android.
2.  Run build.gradle from the Example directory.

Configuration
-------------

There are two types of configuration.
-   Configuration for In-App Purchases
-   Configuration for credit card using tokenization

### Configuration for In-App Purchases

To configure the Chargebee Android SDK for completing and managing In-App Purchases, follow these steps.

1.  [Integrate](https://www.chargebee.com/docs/2.0/mobile-playstore-connect.html)  Google Play Store with your [Chargebee site](https://app.chargebee.com/login).
2.  On the**Sync Overview** pageof theweb app, click**Set up notifications**and use the generated[**App ID**](https://www.chargebee.com/docs/1.0/mobile-playstore-notifications.html#app-id)valueas **SDK Key.**
3.  On the Chargebee site, navigate to **Settings** > **Configure Chargebee** *>* [**API Keys**](https://www.chargebee.com/docs/2.0/api_keys.html#create-an-api-key) to create a new **Publishable API Key** or use an existing [**Publishable API Key**](https://www.chargebee.com/docs/2.0/api_keys.html#types-of-api-keys_publishable-key).
    **Note:** During the publishable API key creation you must allow **read-only** access to plans/items otherwise this key will not work in the following step. Read [more](https://www.chargebee.com/docs/2.0/api_keys.html#types-of-api-keys_publishable-key).
4.  Initialize the SDK with your Chargebee site, Publishable API Key, and SDK Key by including the following snippets in your app delegate during app startup.

```
import Chargebee

Chargebee.configure(site= "your-site",
                    publishableApiKey= "api_key",
                    sdkKey= "sdk_key",packageName = "packageName")
```

### Configuration for credit card using tokenization

To configure SDK only for tokenizing credit card details, follow these steps.

1.  Initialize the SDK with your Chargebee Site and Publishable/Full Access Key.
2.  Initialize the SDK during your app startup by including this in the Android application class' [onCreate](https://developer.android.com/reference/android/app/Activity#onCreate(android.os.Bundle)) method.

```
import com.chargebee.android.Chargebee

Chargebee.configure(site = "your-site", publishableApiKey = "api_key")
```

Integration
-----------

This section describes the SDK integration processes.

-   Integrating In-App Purchases
-   Integrating credit card tokenizationConfigure

### Integrating In-App Purchases

The following section describes how to use the SDK to integrate In-App Purchase information. For details on In-App Purchase, read more [here](https://www.chargebee.com/docs/2.0/mobile-in-app-purchases.html).

#### Get all IAP Product Identifiers from Chargebee

Every In-App Purchase subscription product that you configure in your Play Store account, can be configured in Chargebee as a Plan. Start by retrieving the Google IAP Product IDs from your Chargebee account.
**Note**: Use `retrieveProductIDs` when the Play Store plans already exist in Chargebee. Currently, we do not support uploading Google products to Chargebee. Therefore, to enlist a product in Chargebee you must create a subscription. For displaying products to the end-user, it is recommended to retrieve them from Google.

```
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
For example, query parameters can be passed as **"limit": "100"**.

The above function will automatically determine your product catalog version in Chargebee and call the relevant APIs to retrieve the Chargebee Plans that correspond to Google IAP products and their Google IAP Product IDs.

#### Get IAP Products

Retrieve the Google IAP Product using the following function.

```
CBPurchase.retrieveProducts(this, productIdList= "[Product ID's from Google Play Console]",
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

You can present any of the above products to your users for purchase.

#### Buy or Subscribe Product

Pass the product and customer identifiers to the following function when the user chooses the product to purchase.
`customerId` - Optional parameter. We need the unique ID of your customer as customerId. If your unique list of customers is maintained in your database or a third-party system, send us the unique ID from that source.

```
CBPurchase.purchaseProduct(product="CBProduct", customerID="customerID", object : CBCallback.PurchaseCallback<PurchaseModel>{
      override fun onSuccess(subscriptionID: String, status:Boolean) {
        Log.i(TAG, "${status}")
        Log.i(TAG, "${subscriptionID}")
      }
      override fun onError(error: CBException) {
        Log.e(TAG, "Error:  ${error.message}")
        // Handle error here
      }
})
```

The above function will handle the purchase against Google Play Store and send the IAP token for server-side token verification to your Chargebee account. Use the Subscription ID returned by the above function, to check for Subscription status on Chargebee and confirm the access - granted or denied.

#### Get Subscription Status for Existing Subscribers

The following are methods for checking the subscription status of a subscriber who already purchased the product.

##### Get Subscription Status for Existing Subscribers using Query Parameters

Use query parameters - Subscription ID, Customer ID, or Status for checking the Subscription status on Chargebee and confirm the access - granted or denied.

```
Chargebee.retrieveSubscriptions(queryParam= "[Array of String]") {
       when(it){
             is ChargebeeResult.Success ->{
                  Log.i(TAG, "subscription list in array:  ${it}")
                  Log.i(TAG, "subscription status:  ${it?.get(0)?.cb_subscription?.status}")
             }
             is ChargebeeResult.Error ->{
                  Log.e(TAG, "Error :  ${it.exp.message}")
                  // Handle error here
             }
       }
}
```

For example, query parameters can be passed as ***"customer_id" : "id"***, ***"subscription_id": "id"***, or ***"status": "active"***.

##### Get Subscription Status for Existing Subscribers using Subscription ID

Use only Subscription ID for checking the Subscription status on Chargebee and confirm the access - granted or denied.

```
Chargebee.retrieveSubscription(subscriptionId) {
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

#### Product Catalog 2.0

If you are using Product Catalog 2.0 on your Chargebee site, then use the following functions to retrieve the product to be presented for users to purchase.

##### Get all Items

Retrieve the list of items by using the following function.

```
Chargebee.retrieveAllItems(queryParam) {
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

For example, query parameters can be passed as **"sort_by[desc]" : "name"** or **"limit": "100"**.

##### Get Item Details

Retrieve specific item details by using the following function. Use the Item ID you received from the previous function - Get all items.

```
Chargebee.retrieveItem(queryParam) {
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

#### Product Catalog 1.0

If you are using Product Catalog 1.0 on your Chargebee site, then use any of the following relevant functions to retrieve the product to be presented for users to purchase.

##### Get All Plans

Retrieve the list of plans by using the following function.

```
Chargebee.retrieveAllPlans(queryParam) {
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

For example, query parameters can be passed as **"sort_by[desc]" : "name"** or **"limit": "100"**.

##### Get Plan Details

Retrieve specific plan details by passing plan ID in the following function.

```
Chargebee.retrievePlan(queryParam) {
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

##### Get Addon Details

You can retrieve specific addon details by passing addon ID in the following function.

```
Chargebee.retrieve("addonId") { addonResult ->
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

```
val card = Card(
    number = "4321567890123456",
    expiryMonth = "12",
    expiryYear = "29",
    cvc = "123")
val paymentDetail = PaymentDetail(
    currencyCode = "USD",
    type = PaymentMethodType.CARD,
    card = card)
Chargebee.createTempToken(paymentDetail) { tokenResult ->
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

### Use the Chargebee Token

Once your customer's card data is processed and stored and a Chargebee token reference is returned to you, use the token in subsequent API calls to process transactions.

The following are some endpoints that accept Chargebee tokens for processing.

-   [Create a Payment Source for the customer](https://apidocs.chargebee.com/docs/api/payment_sources#create_using_chargebee_token)
-   [Create a Subscription](https://apidocs.chargebee.com/docs/api/subscriptions#create_a_subscription)
-   [Update a Subscription](https://apidocs.chargebee.com/docs/api/subscriptions#update_a_subscription)

Please refer to the [Chargebee API Docs](https://apidocs.chargebee.com/docs/api) for subsequent integration steps.

License
-------

Chargebee is available under the [MIT license](https://opensource.org/licenses/MIT). For more information, see the LICENSE file.
