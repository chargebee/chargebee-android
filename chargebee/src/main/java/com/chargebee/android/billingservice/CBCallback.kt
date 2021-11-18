package com.chargebee.android.billingservice

import com.chargebee.android.exceptions.CBException
import com.chargebee.android.models.Products

interface CBCallback {
    interface ListProductIDsCallback<T> {
        fun onSuccess(productIDs: ArrayList<String>)
        fun onError(error: CBException)
    }
    interface ListProductsCallback<T> {
        fun onSuccess(productIDs: ArrayList<Products>)
        fun onError(error: CBException)
    }
    interface PurchaseCallback<T> {
        fun onSuccess(subscriptionId: PurchaseModel)
        fun onError(error: CBException)
    }

}