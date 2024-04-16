# Chargebee Android

> [!NOTE]  
> #### Updates for Billing Library 5
> - SDK Version 2.0: This version uses Google Billing Library 5.2.1 APIs to fetch product information from the Google Play Console and make purchases. If you’re integrating Chargebee’s SDK for the first time, then use this version, and if you’re migrating from the older version of SDK to this version, follow the migration steps in this [document](https://www.chargebee.com/docs/2.0/mobile-playstore-billing-library-5.html).
> - SDK Version 1.2.0: This [version](https://github.com/chargebee/chargebee-android/tree/1.x.x) includes Billing Library 5.2.1 but still uses Billing Library 4.0 APIs to fetch product information from the Google Play Console and make purchases. This will enable you to list or update your Android app on the store without any warnings from Google and give you enough time to migrate to version 2.0.
> - SDK Version 1.1.0: This and less than this version of SDKs use billing library 4.0 APIs that are deprecated by Google. Therefore, it is highly recommended that you upgrade your app and integrate it with SDK version 1.2.0 and above.


This is Chargebee’s Android Software Development Kit (SDK). This SDK makes it efficient and comfortable to build a seamless subscription experience in your Android app. 

Post-installation, initialization, and authentication with the Chargebee site, this SDK will support the following process.

1. **Sync In-App Subscriptions with Chargebee:** [Integrate](https://www.chargebee.com/docs/2.0/mobile-playstore-connect.html#chargebee-configuration) Chargebee with Google Play Store to process in-app purchase subscriptions, and track them on your Chargebee account for a single source of truth for subscriptions across the Web and Google Play Store. Use this if you are selling digital goods or services, or are REQUIRED to use Google Play's in-app purchases. 
**For SDK methods to work, ensure that [prerequisites](https://www.chargebee.com/docs/2.0/mobile-playstore-connect.html#prerequisites-configuration) are configured in Chargebee.** 

2. **Tokenisation of credit card:** Tokenize credit card information while presenting your user interface. Use this if you are selling physical goods or offline services or are NOT REQUIRED to use Google's in-app purchases.

## Requirements
The following requirements must be set up before installing Chargebee’s Android SDK.

* Android Target API Level 31 and above
* [Android Gradle Plugin](https://developer.android.com/studio/releases/gradle-plugin) 4.2.2
* [Gradle](https://gradle.org/releases/) 6.1.1+
* [AndroidX](https://developer.android.com/jetpack/androidx/)
* Java 8+ and Kotlin

## Installation
The `Chargebee-Android` SDK can be installed by adding below dependency to the `build.gradle` file:

```kotlin
implementation 'com.chargebee:chargebee-android:2.0.0-beta-2'
```

## Example project
This is an optional step that helps you verify the SDK implementation using this example project. You can download or clone the example project via GitHub.

To run the example project, follow these steps.

1. Clone the repo - https://github.com/chargebee/chargebee-android.

2. Run build.gradle from the Example directory.

## Configuration
There are two types of configuration.

* Configuration for In-App Purchases

* Configuration for credit card using tokenization

### Configuration for In-App Purchases
To configure the Chargebee Android SDK for completing and managing In-App Purchases, follow these steps.

1. [Integrate](https://www.chargebee.com/docs/2.0/mobile-playstore-connect.html) Google Play Store with your [Chargebee site](https://app.chargebee.com/sites/select).

2. On the **Sync Overview** page of the web app, click **Set up notifications** and use the generated [App ID](https://www.chargebee.com/docs/1.0/mobile-playstore-notifications.html#app-id) value as **SDK Key**.

3. On the Chargebee site, navigate to **Settings** > **Configure Chargebee** > [API Keys](https://www.chargebee.com/docs/2.0/api_keys.html#create-an-api-key) to create a new [Publishable API Key](https://www.chargebee.com/docs/2.0/api_keys.html#types-of-api-keys_publishable-key) or use an existing Publishable API Key. 
**Note:** During the publishable API key creation you must allow **read-only** access to plans/items otherwise this key will not work in the following step. Read [more](https://www.chargebee.com/docs/2.0/api_keys.html#types-of-api-keys_publishable-key).

4. Initialize the SDK with your Chargebee site, **Publishable API Key**, and SDK Key by including the following snippets in your app delegate during app startup.

```kotlin
import com.chargebee.android.Chargebee

Chargebee.configure(
  site = "your-site",
  publishableApiKey = "api-key",
  sdkKey = "sdk-key",
  packageName = "your-package"
) {
  when (it) {
    is ChargebeeResult.Success -> {
      // Success
    }
    is ChargebeeResult.Error -> {
      // Error
    }
  }
}
```
### Configuration for credit card using tokenization
To configure SDK only for tokenizing credit card details, follow these steps.

1. Initialize the SDK with your Chargebee Site and Publishable/Full Access Key.

2. Initialize the SDK during your app startup by including this in the Android application class' [onCreate](https://developer.android.com/reference/android/app/Activity#onCreate(android.os.Bundle)) method.

```kotlin
import com.chargebee.android.Chargebee

Chargebee.configure(site = "your-site", publishableApiKey = "api-key")

```
## Integration
This section describes the SDK integration processes.

* Integrating In-App Purchases

* Integrating credit card tokenizationConfigure

### Integrating In-App Purchases
The following section describes how to use the SDK to integrate In-App Purchase information. For details on In-App Purchase, read more [here](https://www.chargebee.com/docs/2.0/mobile-in-app-purchases.html).

### Get all IAP Product Identifiers from Chargebee
Every In-App Purchase subscription product that you configure in your Play Store account, can be configured in Chargebee as a Plan. Start by retrieving the Google IAP Product IDs from your Chargebee account.

```kotlin
CBPurchase.retrieveProductIdentifiers(queryParam) {
    when (it) {
        is CBProductIDResult.ProductIds -> {
          Log.i(TAG, "List of Product Identifiers:  $it")
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
Retrieve the Google IAP Product using the following function.

```kotlin
CBPurchase.retrieveProducts(activity, productIdList= ["Product ID's from Google Play Console"],
      object : CBCallback.ListProductsCallback<ArrayList<CBProduct>> {
               override fun onSuccess(productDetails: ArrayList<CBProduct>) {
                     Log.i(TAG, "List of Products:  $productDetails")
                }
                override fun onError(error: CBException) {
                    Log.e(TAG, "Error:  ${error.message}")
                }
            })
```
You can present any of the above products to your users for them to purchase.

### Buy or Subscribe Product
Pass the `PurchaseProductParams`, `CBCustomer` and `OfferToken` to the following function when the user chooses the product to purchase.

`CBCustomer` - **Optional object**. Although this is an optional object, we recommend passing the necessary customer details, such as `customerId`, `firstName`, `lastName`, and `email` if it is available before the user subscribes to your App. This ensures that the customer details in your database match the customer details in Chargebee. If the `customerId` is not passed in the customer's details, then the value of `customerId` will be the same as the `SubscriptionId` created in Chargebee.

**Note**: The `customer` parameter in the below code snippet is an instance of `CBCustomer` class that contains the details of the customer who wants to subscribe or buy the product.

```kotlin
val purchaseParams = PurchaseProductParams(selectedCBProduct, "selectedOfferToken")
val cbCustomer = CBCustomer("customerId","firstName","lastName","email")
CBPurchase.purchaseProduct(purchaseProductParams = purchaseProductParams, customer = cbCustomer, object : CBCallback.PurchaseCallback<String>{
      override fun onSuccess(result: ReceiptDetail, status:Boolean) {
        Log.i(TAG, "$status")
        Log.i(TAG, "${result.subscription_id}")
        Log.i(TAG, "${result.plan_id}")   
      }
      override fun onError(error: CBException) {
        Log.e(TAG, "Error:  ${error.message}")
      }
})
 ```
The above function will handle the purchase against Google Play Store and send the IAP token for server-side token verification to your Chargebee account. Use the Subscription ID returned by the above function, to check for Subscription status on Chargebee and confirm the access - granted or denied.

### Invoke Manage Subscriptions in your App
The `showManageSubscriptionsSettings()` function is designed to invoke the Manage Subscriptions in your app using Chargebee's Android SDKs. `Chargebee.showManageSubscriptionsSettings()`, opens the Play Store App subscriptions settings page.

### One-Time Purchases
The `purchaseNonSubscriptionProduct` function handles the one-time purchase against Google Play Store and sends the IAP receipt for server-side receipt verification to your Chargebee account. Post verification a Charge corresponding to this one-time purchase will be created in Chargebee. There are two types of one-time purchases `consumable` and `non_consumable`.

```kotlin
CBPurchase.purchaseNonSubscriptionProduct(product = CBProduct, customer = CBCustomer, productType = OneTimeProductType.CONSUMABLE, callback = object : CBCallback.OneTimePurchaseCallback{
      override fun onSuccess(result: NonSubscription, status:Boolean) {
        Log.i(TAG, "invoice ID:  ${result.invoiceId}")
        Log.i(TAG, "charge ID:  ${result.chargeId}")
        Log.i(TAG, "customer ID:  ${result.customerId}")
      }
      override fun onError(error: CBException) {
        Log.e(TAG, "Error:  ${error.message}")
      }
})
 ```

The given code defines a function named `purchaseNonSubscriptionProduct` in the CBPurchase class, which takes four input parameters:

- `product`: An instance of `CBProduct` class,  representing the product to be purchased from the Google Play Store.
- `customer`: Optional. An instance of `CBCustomer` class, initialized with the customer's details such as `customerId`, `firstName`, `lastName`, and `email`.
- `productType`: An enum instance of `productType` type, indicating the type of product to be purchased. It can be either .`consumable`, or `non_consumable`.
- `callback`:  The `OneTimePurchaseCallback` listener will be invoked when product purchase completes.

The function is called asynchronously, and it returns a `Result` object with a `success` or `failure` case, which can be handled in the listener.
- If the purchase is successful, the listener will be called with the `success` case, it returns `NonSubscriptionResponse` object. which includes the `customerId`, `chargeId`, and `invoiceId` associated with the purchase.
- If there is any failure during the purchase, the listener will be called with the `error` case, it returns `CBException`. which includes an error object that can be used to handle the error.

### Restore Purchase

The `restorePurchases()` function helps to recover your app user's previous purchases without making them pay again. Sometimes, your app user may want to restore their previous purchases after switching to a new device or reinstalling your app. You can use the `restorePurchases()` function to allow your app user to easily restore their previous purchases.

To retrieve **inactive** purchases along with the **active** purchases for your app user, you can call the `restorePurchases()` function with the `includeInActiveProducts` parameter set to `true`. If you only want to restore active subscriptions, set the parameter to `false`. Here is an example of how to use the `restorePurchases()` function in your code with the `includeInActiveProducts` parameter set to `true`.

`CBCustomer` - **Optional object**. Although this is an optional object, we recommend passing the necessary customer details, such as `customerId`, `firstName`, `lastName`, and `email` if it is available before the user subscribes to your App. This ensures that the customer details in your database match the customer details in Chargebee. If the `customerId` is not passed in the customer's details, then the value of `customerId` will be the same as the `subscriptionId` created in Chargebee. Also, the restored subscriptions will not be associate with existing customerId.

```kotlin
CBPurchase.restorePurchases(context = current activity context, customer = CBCustomer, includeInActivePurchases = false, object : CBCallback.RestorePurchaseCallback{
      override fun onSuccess(result: List<CBRestoreSubscription>) {
        result.forEach {
          Log.i(javaClass.simpleName, "Successfully restored purchases")
        }  
      }
      override fun onError(error: CBException) {
        Log.e(TAG, "Error:  ${error.message}")
      }
})
 ```

##### Return Subscriptions Object 
The `restorePurchases()` function returns an array of subscription objects and each object holds three attributes `subscription_id`, `plan_id`, and `store_status`. The value of `store_status` can be used to verify the subscription status such as `Active`, `InTrial`, `Cancelled` and `Paused`.

##### Error Handling 
In the event of any failures while finding associated subscriptions for the restored items, The SDK will return an error, as mentioned in the following table.

These are the possible error codes and their descriptions:
| Error Code                        | Description                                                                                                                 |
|-----------------------------------|-----------------------------------------------------------------------------------------------------------------------------|
| `BillingErrorCode.SERVICE_TIMEOUT`            | The request has reached the maximum timeout before Google Play responds.       |
| `BillingErrorCode.FEATURE_NOT_SUPPORTED` | The requested feature is not supported by the Play Store on the current device.                                             |
| `BillingErrorCode.SERVICE_UNAVAILABLE`        | The service is currently unavailable.         |
| `BillingErrorCode.DEVELOPER_ERROR`  | Error resulting from incorrect usage of the API.                                                          |
| `BillingErrorCode.ERROR`         | Fatal error during the API action.                             |
| `BillingErrorCode.SERVICE_DISCONNECTED`         | The app is not connected to the Play Store service via the Google Play Billing Library.                             |
| `BillingErrorCode.UNKNOWN`         | Unknown error occurred.                             |

##### Synchronization of Google Play Store Purchases with Chargebee through Receipt Validation
Receipt validation is crucial to ensure that the purchases made by your users are synced with Chargebee. In rare cases, when a purchase is made at the Google Play Store, and the network connection goes off or the server not responding, the purchase details may not be updated in Chargebee. In such cases, you can use a retry mechanism by following these steps:

* Add a network listener, as shown in the example project.
* Save the product identifier in the cache once the purchase is initiated and clear the cache once the purchase is successful.
* When the network connectivity is lost after the purchase is completed at Google Play Store but not synced with Chargebee, retrieve the product from the cache once the network connection is back and initiate `validateReceipt() / validateReceiptForNonSubscriptions()` by passing activity `Context`, `CBProduct` and `CBCustomer(optional)` as input. This will validate the receipt and sync the purchase in Chargebee as a subscription or one-time purchase. For subscriptions, use the function to `validateReceipt()`;for one-time purchases, use the function `validateReceiptForNonSubscriptions()`.

Use the function available for the retry mechanism.
##### Function for validating the Subscriptions receipt

```kotlin
CBPurchase.validateReceipt(context = current activity context, product = CBProduct, customer = CBCustomer, object : CBCallback.PurchaseCallback<String> {
      override fun onSuccess(result: ReceiptDetail, status: Boolean) {
        Log.i(TAG, "$status")
        Log.i(TAG, "${result.subscription_id}")
        Log.i(TAG, "${result.plan_id}")
      }
      override fun onError(error: CBException) {
        Log.e(TAG, "Error:  ${error.message}")
      }
})
 ```

##### Function for validating the One-Time Purchases receipt

```kotlin
CBPurchase.validateReceiptForNonSubscriptions(context = current activity context, product = CBProduct, customer = CBCustomer, productType = OneTimeProductType.CONSUMABLE, object : CBCallback.OneTimePurchaseCallback {
      override fun onSuccess(result: NonSubscription, status: Boolean) {
        Log.i(TAG, "invoice ID:  ${result.invoiceId}")
        Log.i(TAG, "charge ID:  ${result.chargeId}")
        Log.i(TAG, "customer ID:  ${result.customerId}")
      }
      override fun onError(error: CBException) {
        Log.e(TAG, "Error:  ${error.message}")
      }
})
 ```

### Get Subscription Status for Existing Subscribers
The following are methods for checking the subscription status of a subscriber who already purchased the product.

### Get Subscription Status for Existing Subscribers using Query Parameters
Use query parameters - Subscription ID, Customer ID, or Status for checking the Subscription status on Chargebee and confirm the access - granted or denied.

```kotlin
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
For example, query parameters can be passed as **"customer_id" : "id", "subscription_id": "id", or "status": "active"**.

### Get Subscription Status for Existing Subscribers using Subscription ID

Use only Subscription ID for checking the Subscription status on Chargebee and confirm the access - granted or denied.

```kotlin
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
### Retrieve Entitlements of a Subscription

Use the Subscription ID for fetching the list of [entitlements](https://www.chargebee.com/docs/2.0/entitlements.html) associated with the subscription.

```kotlin
Chargebee.retrieveEntitlements(subscriptionId) {
      when(it){
            is ChargebeeResult.Success -> {
                Log.i(TAG, "Response:  ${(it.data)}") 
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

### Product Catalog 2.0
If you are using Product Catalog 2.0 in your Chargebee site, then you can use the following functions to retrieve the product to be presented for users to purchase.

### Get all Items
You can retrieve the list of items by using the following function.

```kotlin
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
For example, query parameters can be passed as **"sort_by[desc]" : "name" OR "limit": "100"**.

### Get Item Details
You can retrieve specific item details by using the following function. Use the Item ID you received from the previous function - Get all items.

```kotlin
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

### Product Catalog 1.0
If you are using Product Catalog 1.0 in your Chargebee site, then you can use any of the following relevant functions to retrieve the product to be presented for users to purchase.

### Get All Plans
You can retrieve the list of plans by using the following function.

```kotlin
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
For example, query parameters can be passed as **"sort_by[desc]" : "name" OR "limit": "100"**.

### Get Plan Details
You can retrieve specific plan details by passing plan ID in the following function.

```kotlin
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

### Get Addon Details
You can retrieve specific addon details by passing addon ID in the following function.

```kotlin
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
## Use the Chargebee Token
Once your customer’s card data is processed and stored, and a Chargebee token reference is returned to you, you can use the token in subsequent API calls to process transactions. The following are some endpoints that accept Chargebee tokens for processing.

- [Create a Payment Source for the customer](https://apidocs.chargebee.com/docs/api/payment_sources#create_using_chargebee_token)
- [Create a Subscription](https://apidocs.chargebee.com/docs/api/subscriptions#create_a_subscription)
- [Update a Subscription](https://apidocs.chargebee.com/docs/api/subscriptions#update_a_subscription)

Please refer to the [Chargebee API Docs](https://apidocs.chargebee.com/docs/api) for subsequent integration steps.


## License

Chargebee is available under the [MIT license](https://opensource.org/licenses/MIT). See the LICENSE file for more info.

## Document in Markdown

<details>
  <summary>Click here to expand...</summary>
  
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
  implementation 'com.chargebee:chargebee-android:1.0.25'
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
  
  ```kotlin
  import com.chargebee.android.Chargebee
  
  Chargebee.configure(site= "your-site",
                      publishableApiKey= "api_key",
                      sdkKey= "sdk_key",packageName = "packageName")
  ```

  ### Configuration for credit card using tokenization
  
  To configure SDK only for tokenizing credit card details, follow these steps.
  
  1.  Initialize the SDK with your Chargebee Site and Publishable/Full Access Key.
  2.  Initialize the SDK during your app startup by including this in the Android application class' [onCreate](https://developer.android.com/reference/android/app/Activity#onCreate(android.os.Bundle)) method.
  
  ```kotlin
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

### Get all IAP Product Identifiers from Chargebee
Every In-App Purchase subscription product that you configure in your Play Store account, can be configured in Chargebee as a Plan. Start by retrieving the Google IAP Product IDs from your Chargebee account.

```kotlin
CBPurchase.retrieveProductIdentifers(queryParam) {
    when (it) {
        is CBProductIDResult.ProductIds -> {
          Log.i(TAG, "List of Product Identifiers:  $it")
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

  #### Get IAP Products
  
  Retrieve the Google IAP Product using the following function.
  
  ```kotlin
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

  Pass the `CBProduct` and  `CBCustomer` objects to the following function when the user chooses the product to purchase.
  
  `CBCustomer` - **Optional object**. Although this is an optional object, we recommend passing the necessary customer details, such as `customerId`, `firstName`, `lastName`, and `email` if it is available before the user subscribes to your App. This ensures that the customer details in your database match the customer details in Chargebee. If the `customerId` is not passed in the customer's details, then the value of `customerId` will be the same as the `SubscriptionId` created in Chargebee.
  
  **Note**: The `customer` parameter in the below code snippet is an instance of `CBCustomer` class that contains the details of the customer who wants to subscribe or buy the product.

  ```kotlin
    CBPurchase.purchaseProduct(product=CBProduct, customer=CBCustomer, object : CBCallback.PurchaseCallback<PurchaseModel>{
        override fun onSuccess(result: ReceiptDetail, status:Boolean) {
            Log.i(TAG, "$status") 
            Log.i(TAG, "${result.subscription_id}")
            Log.i(TAG, "${result.plan_id}")
        }
        override fun onError(error: CBException) {
            Log.e(TAG, "Error:  ${error.message}")
            // Handle error here    
        }
        })
  ```
  
  The above function will handle the purchase against Google Play Store and send the IAP token for server-side token verification to your Chargebee account. Use the Subscription ID returned by the above function, to check for Subscription status on Chargebee and confirm the access - granted or denied.

  This function also returns the plan ID associated with a subscription. You can associate JSON metadata with the Google Play Store plans in Chargebee and retrieve the same by passing plan ID to the SDK method - [retrievePlan](https://github.com/chargebee/chargebee-android#get-plan-details)(PC 1.0) or [retrieveItem](https://github.com/chargebee/chargebee-android#get-item-details)(PC 2.0).
  
  #### Get Subscription Status for Existing Subscribers
  
  The following are methods for checking the subscription status of a subscriber who already purchased the product.
  
  ##### Get Subscription Status for Existing Subscribers using Query Parameters
  
  Use query parameters - Subscription ID, Customer ID, or Status for checking the Subscription status on Chargebee and confirm the access - granted or denied.
  
  ```kotlin
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
  
  ```kotlin
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

  ##### Returns Plan Object

  These functions return the plan ID associated with a subscription. You can associate JSON metadata with the Google Play Store plans in Chargebee and retrieve the same by passing plan ID to the SDK method - [retrievePlan](https://github.com/chargebee/chargebee-android#get-plan-details)(PC 1.0) or [retrieveItem](https://github.com/chargebee/chargebee-android#get-item-details)(PC 2.0).

  #### Retrieve Entitlements of a Subscription

  Use the Subscription ID for fetching the list of [entitlements](https://www.chargebee.com/docs/2.0/entitlements.html) associated with the subscription.

  ```kotlin
  Chargebee.retrieveEntitlements(subscriptionId) {
      when(it){
            is ChargebeeResult.Success -> {
                Log.i(TAG, "Response:  ${(it.data)}") 
            }
            is ChargebeeResult.Error ->{
                Log.e(TAG, "Error :  ${it.exp.message}")
                // Handle error here
           }
     }
  }  
  ```

  **Note**: Entitlements feature is available only if your Chargebee site is on [Product Catalog 2.0](https://www.chargebee.com/docs/2.0/product-catalog.html).

  ### Integrating credit card tokenization
  
  The following section describes how to use the SDK to directly tokenize credit card information if you are NOT REQUIRED to use Google's in-app purchases.
  
  #### Product Catalog 2.0
  
  If you are using Product Catalog 2.0 on your Chargebee site, then use the following functions to retrieve the product to be presented for users to purchase.
  
  ##### Get all Items
  
  Retrieve the list of items by using the following function.
  
  ```kotlin
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
  
  ```kotlin
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
  
  ```kotlin
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
  
  ```kotlin
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
  
  ```kotlin
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
  
</details>
