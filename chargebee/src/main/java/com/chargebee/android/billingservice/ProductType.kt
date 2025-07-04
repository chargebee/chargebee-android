package com.chargebee.android.billingservice

import java.util.*

enum class ProductType(val value: String) {
    SUBS("subs"),
    INAPP("inapp");

    companion object {
        fun getProductType(value: String): ProductType = ProductType.valueOf(value.toUpperCase(Locale.ROOT))
    }
}