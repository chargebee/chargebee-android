package com.chargebee.android.models

data class PaymentDetail(
    val currencyCode: String,
    val type: PaymentMethodType,
    val card: Card
)

data class Card(
    val number: String,
    val expiryMonth: String,
    val expiryYear: String,
    val cvc: String
)