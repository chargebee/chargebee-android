package com.chargebee.example.billing

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chargebee.android.billingservice.CBCallback
import com.chargebee.android.billingservice.CBPurchase
import com.chargebee.android.billingservice.PurchaseModel
import com.chargebee.android.exceptions.CBException
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.models.Products
import com.chargebee.android.models.SubscriptionDetail
import com.chargebee.android.models.SubscriptionDetailsWrapper
import com.chargebee.android.network.CBReceiptResponse

class BillingViewModel : ViewModel() {

    private val TAG = "BillingViewModel"
    var productPurchaseResult: MutableLiveData<PurchaseModel?> = MutableLiveData()
    var cbException: MutableLiveData<CBException?> = MutableLiveData()
    var subscriptionStatus: MutableLiveData<String?> = MutableLiveData()
    var error: MutableLiveData<String?> = MutableLiveData()
    private var subscriptionId: String = ""

    fun purchaseProduct(param: Products) {
        CBPurchase.purchaseProduct(param, object : CBCallback.PurchaseCallback<PurchaseModel>{
            override fun onSuccess(success: PurchaseModel) {
                productPurchaseResult.postValue(success)
            }
            override fun onError(error: CBException) {
                cbException.postValue(error)
            }
        })
    }

    fun validateReceipt(purchaseToken: String, products: Products) {
        CBPurchase.validateReceipt(purchaseToken, products){
            when(it){
                is ChargebeeResult.Success ->{
                    subscriptionId = (it.data as CBReceiptResponse).in_app_subscription.subscription_id
                    retrieveSubscription(subscriptionId)
                }
                is ChargebeeResult.Error ->{
                    Log.d(TAG, "Exception from server :  ${it.exp.message}")
                    error.postValue(it.exp.message)
                }
            }
        }
    }
    fun retrieveSubscription(subscriptionId: String) {
        SubscriptionDetail.retrieveSubscription(subscriptionId) {
            when(it){
                is ChargebeeResult.Success ->{
                    Log.i(TAG, "subscription :  ${it.data}")
                    subscriptionStatus.postValue((it.data as SubscriptionDetailsWrapper).subscription.status)
                }
                is ChargebeeResult.Error ->{
                    Log.d(TAG, "Exception from server :  ${it.exp.message}")
                    error.postValue(it.exp.message)
                }
            }
        }
    }
}