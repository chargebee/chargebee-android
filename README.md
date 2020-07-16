# Chargebee Android

## Requirements

## Installation

## Usage

### Configure
To use the Chargebee Adnroid SDK, you must initialize it with your Chargebee Site Code and API key. You can initialize this during your app startup by including this in Android application class' `onCreate` method.

```kotlin
import com.chargebee.android.Chargebee

Chargebee.configure(site = "site-code", apiKey = "api_key")
```

### Get Plan Details

```java
Plan.retrieve("planCode", planResult -> {
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
Addon.retrieve("addonCode") { addonResult ->
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

## License

Chargebee is available under the MIT license. See the LICENSE file for more info.
