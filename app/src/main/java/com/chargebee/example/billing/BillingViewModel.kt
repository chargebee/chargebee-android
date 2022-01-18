package com.chargebee.example.billing

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chargebee.android.ErrorDetail
import com.chargebee.android.billingservice.CBCallback
import com.chargebee.android.billingservice.CBPurchase
import com.chargebee.android.billingservice.PurchaseModel
import com.chargebee.android.exceptions.CBException
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.models.Products
import com.chargebee.android.models.SubscriptionDetail
import com.chargebee.android.models.SubscriptionDetailsWrapper
import com.chargebee.android.network.CBReceiptResponse
import com.google.gson.Gson

class BillingViewModel : ViewModel() {

    private val TAG = "BillingViewModel"
    var productPurchaseResult: MutableLiveData<PurchaseModel?> = MutableLiveData()
    var cbException: MutableLiveData<String?> = MutableLiveData()
    var subscriptionStatus: MutableLiveData<String?> = MutableLiveData()
    var error: MutableLiveData<String?> = MutableLiveData()
    private var subscriptionId: String = ""

    fun purchaseProduct(param: Products) {
        CBPurchase.purchaseProduct(param, object : CBCallback.PurchaseCallback<PurchaseModel>{
            override fun onSuccess(success: PurchaseModel) {
                productPurchaseResult.postValue(success)
            }
            override fun onError(error: CBException) {
                cbException.postValue(Gson().fromJson<ErrorDetail>(
                    error.message,
                    ErrorDetail::class.java
                ).message)
            }
        })
    }

    fun validateReceipt(purchaseToken: String, products: Products) {
        CBPurchase.validateReceipt(purchaseToken, products){
            when(it){
                is ChargebeeResult.Success ->{
                    Log.i(TAG, "Validate Receipt Response:  ${(it.data as CBReceiptResponse).in_app_subscription}")
                    Log.i(TAG, "Subscription ID :  ${(it.data as CBReceiptResponse).in_app_subscription.subscription_id}")
                    subscriptionId = (it.data as CBReceiptResponse).in_app_subscription.subscription_id
                    retrieveSubscription(subscriptionId)
                }
                is ChargebeeResult.Error ->{
                    Log.e(TAG, "Exception from server - validateReceipt() :  ${it.exp.message}")
                    error.postValue(Gson().fromJson<ErrorDetail>(
                        it.exp.message,
                        ErrorDetail::class.java
                    ).message)
                }
            }
        }
    }
    fun retrieveSubscription(subscriptionId: String) {
        SubscriptionDetail.retrieveSubscription(subscriptionId) {
            when(it){
                is ChargebeeResult.Success -> {
                    Log.i(
                        TAG,
                        "subscription status:  ${(it.data as SubscriptionDetailsWrapper).cb_subscription.status} ,activated_at : ${(it.data as SubscriptionDetailsWrapper).cb_subscription.activated_at}" +
                                " subscription id : ${(it.data as SubscriptionDetailsWrapper).cb_subscription.id}" +
                                " customer_id : ${(it.data as SubscriptionDetailsWrapper).cb_subscription.customer_id}" +
                                " current_term_start : ${(it.data as SubscriptionDetailsWrapper).cb_subscription.current_term_start} " +
                                " current_term_end : ${(it.data as SubscriptionDetailsWrapper).cb_subscription.current_term_end}" +
                                " plan_amount : ${(it.data as SubscriptionDetailsWrapper).cb_subscription.plan_amount}"
                    )

                    subscriptionStatus.postValue((it.data as SubscriptionDetailsWrapper).cb_subscription.status)
                }
                is ChargebeeResult.Error ->{
                    Log.e(TAG, "Exception from server- retrieveSubscription() :  ${it.exp.message}")
                    error.postValue(Gson().fromJson<ErrorDetail>(
                        it.exp.message,
                        ErrorDetail::class.java
                    ).message)
                }
            }
        }
    }
}