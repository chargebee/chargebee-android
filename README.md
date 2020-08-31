# Chargebee Android

## Installation

### Requirements
* Android 5.0 (API level 21) and above
* [Android Gradle Plugin](https://developer.android.com/studio/releases/gradle-plugin) 4.0.0
* [Gradle](https://gradle.org/releases/) 6.1.1+
* [AndroidX](https://developer.android.com/jetpack/androidx/)

### Configuration

Add `chargebee-android` to your `build.gradle` dependencies.

```
implementation 'com.chargebee:chargebee-android:0.1.0'
```

## Usage

### Configure
To use the Chargebee Android SDK, you must initialize it with your Chargebee Site and API key. You can initialize this during your app startup by including this in Android application class' `onCreate` method.

```kotlin
import com.chargebee.android.Chargebee

Chargebee.configure(site = "your-site", publishableApiKey = "api_key")
```

### Get Plan Details

```java
Plan.retrieve("planId", planResult -> {
    try {
        Plan plan = planResult.getData();
        Log.d("success", plan.toString());
        // Use plan details here
    } catch (CBException ex) {
        Log.d("error", ex.getMessage());
        // Handle error here
    }
    return null;
});
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
