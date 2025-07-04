package com.chargebee.android.billingservice

import com.chargebee.android.exceptions.CBException
import com.chargebee.android.models.*
import com.chargebee.android.network.ReceiptDetail

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
        fun onSuccess(result: ReceiptDetail, status: Boolean)
        fun onError(error: CBException)
    }

    interface RestorePurchaseCallback {
        fun onSuccess(result: List<CBRestoreSubscription>)
        fun onError(error: CBException)
    }

    interface OneTimePurchaseCallback {
        fun onSuccess(result: NonSubscription, status: Boolean)
        fun onError(error: CBException)
    }
}