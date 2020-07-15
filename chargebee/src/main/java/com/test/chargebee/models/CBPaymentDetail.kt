package com.test.chargebee.models

data class CBPaymentDetail(
    val currencyCode: String,
    val type: CBPaymentMethodType,
    val card: CBCard
)

data class CBCard(
    val number: String,
    val expiryMonth: String,
    val expiryYear: String,
    val cvc: String
)