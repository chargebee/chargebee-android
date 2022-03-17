package com.chargebee.example.billing

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chargebee.android.Chargebee
import com.chargebee.android.ErrorDetail
import com.chargebee.android.billingservice.CBCallback
import com.chargebee.android.billingservice.CBPurchase
import com.chargebee.android.exceptions.CBException
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.models.CBProduct
import com.chargebee.android.models.SubscriptionDetailsWrapper
import com.google.gson.Gson

class BillingViewModel : ViewModel() {

    private val TAG = "BillingViewModel"
    var productPurchaseResult: MutableLiveData<String?> = MutableLiveData()
    var cbException: MutableLiveData<String?> = MutableLiveData()
    var subscriptionStatus: MutableLiveData<String?> = MutableLiveData()
    var error: MutableLiveData<String?> = MutableLiveData()
    private var subscriptionId: String = ""

    fun purchaseProduct(product: CBProduct, customerID: String) {

        CBPurchase.purchaseProduct(product, customerID,  object : CBCallback.PurchaseCallback<String>{
            override fun onSuccess(status: String) {
                productPurchaseResult.postValue(status)
            }
            override fun onError(error: CBException) {
                try {
                    cbException.postValue(error.message)
                }catch (exp: Exception){
                    Log.i(TAG, "Exception :${exp.message}")
                }
            }
        })
    }

//    fun validateReceipt(purchaseToken: String, products: CBProduct) {
//        CBPurchase.validateReceipt(purchaseToken, products){
//            when(it){
//                is ChargebeeResult.Success ->{
//                    Log.i(TAG, "Validate Receipt Response:  ${(it.data as CBReceiptResponse).in_app_subscription}")
//                    subscriptionId = (it.data as CBReceiptResponse).in_app_subscription.subscription_id
//                    retrieveSubscription(subscriptionId)
//                }
//                is ChargebeeResult.Error ->{
//                    Log.e(TAG, "Exception from server - validateReceipt() :  ${it.exp.message}")
//                    error.postValue(Gson().fromJson<ErrorDetail>(
//                        it.exp.message,
//                        ErrorDetail::class.java
//                    ).message)
//                }
//            }
//        }
//    }
    fun retrieveSubscription(subscriptionId: String) {
        Chargebee.retrieveSubscription(subscriptionId) {
            when(it){
                is ChargebeeResult.Success -> {
                    Log.i(
                        TAG,
                        "subscription status:  ${(it.data as SubscriptionDetailsWrapper).subscription.status} ,activated_at : ${(it.data as SubscriptionDetailsWrapper).subscription.activated_at}" +
                                " subscription id : ${(it.data as SubscriptionDetailsWrapper).subscription.id}" +
                                " customer_id : ${(it.data as SubscriptionDetailsWrapper).subscription.customer_id}" +
                                " current_term_start : ${(it.data as SubscriptionDetailsWrapper).subscription.current_term_start} " +
                                " current_term_end : ${(it.data as SubscriptionDetailsWrapper).subscription.current_term_end}"
                    )

                    subscriptionStatus.postValue((it.data as SubscriptionDetailsWrapper).subscription.status)
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