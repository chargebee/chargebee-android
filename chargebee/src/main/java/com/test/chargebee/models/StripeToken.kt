package com.test.chargebee.models

data class StripeToken(
    val id: String,
    val type: String
)

data class StripeCard(
    val number: String,
    val expMonth: String,
    val expYear: String,
    val cvc: String
)

fun StripeCard.toFormBody(): Map<String, String> {
    return mapOf(
        "card[number]" to this.number,
        "card[exp_month]" to this.expMonth,
        "card[exp_year]" to this.expYear,
        "card[cvc]" to this.cvc
    )
}