package com.android.billingclient.api

import kotlin.reflect.KClass

fun KClass<ProductDetails>.create(): ProductDetails {
    val productDetails = ProductDetails("{\n" +
            "\t\"productId\": \"gold\",\n" +
            "\t\"type\": \"subs\",\n" +
            "\t\"title\": \"Gold Plan (com.chargebee.newsample (unreviewed))\",\n" +
            "\t\"name\": \"Gold Plan\",\n" +
            "\t\"localizedIn\": [\"en-US\"],\n" +
            "\t\"skuDetailsToken\": \"AEuhp4Ln65Xw7Do9yIO-o4XIj7rAvn_FD90WWajD79kzt0GiNuNm2ACU15T3q56qZTs=\",\n" +
            "\t\"subscriptionOfferDetails\": [{\n" +
            "\t\t\"offerIdToken\": \"AUj\\/YhiCrZ\\/NvaFihCC8rjAWfXEsLtf\\/qPgutEo1M04GIc8psPY06GcWBpun6qf\\/NhMXcQe3KmD+rbgud2XiLO3ptF41\\/HWcHR7YfYcU7brJ6mM=\",\n" +
            "\t\t\"basePlanId\": \"weekly\",\n" +
            "\t\t\"pricingPhases\": [{\n" +
            "\t\t\t\"priceAmountMicros\": 20000000,\n" +
            "\t\t\t\"priceCurrencyCode\": \"INR\",\n" +
            "\t\t\t\"formattedPrice\": \"₹20.00\",\n" +
            "\t\t\t\"billingPeriod\": \"P1W\",\n" +
            "\t\t\t\"recurrenceMode\": 1\n" +
            "\t\t}],\n" +
            "\t\t\"offerTags\": []\n" +
            "\t}, {\n" +
            "\t\t\"offerIdToken\": \"AUj\\/YhgOwTW\\/BAGR2Po8uAsNJc6G+Z5xSDRBnDU7VJ5GN21yhMvuUjUMFDNCwEu+GtDaN2CzYoLqu7wHu\\/T+37S1KlyLFi0tfSAZcJE5MisuY+hKUuRJ\",\n" +
            "\t\t\"basePlanId\": \"monthly\",\n" +
            "\t\t\"pricingPhases\": [{\n" +
            "\t\t\t\"priceAmountMicros\": 40000000,\n" +
            "\t\t\t\"priceCurrencyCode\": \"INR\",\n" +
            "\t\t\t\"formattedPrice\": \"₹40.00\",\n" +
            "\t\t\t\"billingPeriod\": \"P1M\",\n" +
            "\t\t\t\"recurrenceMode\": 1\n" +
            "\t\t}],\n" +
            "\t\t\"offerTags\": []\n" +
            "\t}]\n" +
            "}")
    return productDetails
}