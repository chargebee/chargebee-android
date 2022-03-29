package com.chargebee.android.billingservice

import com.chargebee.android.exceptions.CBException
import com.chargebee.android.models.CBProduct

interface CBCallback {
    interface ListProductIDsCallback<T> {
        fun onSuccess(productIDs: ArrayList<String>)
        fun onError(error: CBException)
    }
    interface ListProductsCallback<T> {
        fun onSuccess(productIDs: ArrayList<CBProduct>)
        fun onError(error: CBException)
    }
    interface PurchaseCallback<T> {
        fun onSuccess(subscriptionID: String, status: Boolean)
        fun onError(error: CBException)
    }

}