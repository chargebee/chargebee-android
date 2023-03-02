package com.chargebee.example.billing

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chargebee.android.Chargebee
import com.chargebee.android.ErrorDetail
import com.chargebee.android.billingservice.CBCallback
import com.chargebee.android.billingservice.CBPurchase
import com.chargebee.android.exceptions.CBException
import com.chargebee.android.exceptions.CBProductIDResult
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.models.*
import com.chargebee.android.network.CBCustomer
import com.chargebee.android.network.ReceiptDetail
import com.google.gson.Gson

class BillingViewModel : ViewModel() {

    private val TAG = "BillingViewModel"
    var productIdsList: MutableLiveData<Array<String>> = MutableLiveData()
    var productPurchaseResult: MutableLiveData<Boolean> = MutableLiveData()
    var cbException: MutableLiveData<CBException?> = MutableLiveData()
    var subscriptionStatus: MutableLiveData<String?> = MutableLiveData()
    var subscriptionList: MutableLiveData<ArrayList<SubscriptionDetailsWrapper>?> = MutableLiveData()
    var error: MutableLiveData<String?> = MutableLiveData()
    var entitlementsResult: MutableLiveData<String?> = MutableLiveData()
    private var subscriptionId: String = ""

    fun purchaseProduct(product: CBProduct, customer: CBCustomer) {

        CBPurchase.purchaseProduct(product, customer,  object : CBCallback.PurchaseCallback<String>{
            override fun onSuccess(result: ReceiptDetail, status:Boolean) {
                Log.i(TAG, "Subscription ID:  ${result.subscription_id}")
                Log.i(TAG, "Plan ID:  ${result.plan_id}")
                productPurchaseResult.postValue(status)
            }
            override fun onError(error: CBException) {
                try {
                    cbException.postValue(error)
                }catch (exp: Exception){
                    Log.i(TAG, "Exception :${exp.message}")
                }
            }
        })
    }
    fun purchaseProduct(product: CBProduct, customerId: String) {

        CBPurchase.purchaseProduct(product, customerId,  object : CBCallback.PurchaseCallback<String>{
            override fun onSuccess(result: ReceiptDetail, status:Boolean) {
                Log.i(TAG, "Subscription ID:  ${result.subscription_id}")
                Log.i(TAG, "Plan ID:  ${result.plan_id}")
                productPurchaseResult.postValue(status)
            }
            override fun onError(error: CBException) {
                try {
                    cbException.postValue(error)
                }catch (exp: Exception){
                    Log.i(TAG, "Exception :${exp.message}")
                }
            }
        })
    }

    fun retrieveProductIdentifers(queryParam: Array<String>){
        CBPurchase.retrieveProductIdentifers(queryParam) {
            when (it) {
                is CBProductIDResult.ProductIds -> {
                    Log.i(TAG, "List of Product Identifiers:  $it")
                    val array = it.IDs.toTypedArray()
                    productIdsList.postValue(array)
                }
                is CBProductIDResult.Error -> {
                    Log.e(javaClass.simpleName, " ${it.exp.message}")
                    cbException.postValue(it.exp)
                }
            }
        }
    }
    fun retrieveSubscription(subscriptionId: String) {
        Chargebee.retrieveSubscription(subscriptionId) {
            when(it){
                is ChargebeeResult.Success -> {
                    Log.i(
                        TAG,
                        "subscription status:  ${(it.data as SubscriptionDetailsWrapper).cb_subscription.status} ,activated_at : ${(it.data as SubscriptionDetailsWrapper).cb_subscription.activated_at}" +
                                " subscription id : ${(it.data as SubscriptionDetailsWrapper).cb_subscription.subscription_id}" +
                                " customer_id : ${(it.data as SubscriptionDetailsWrapper).cb_subscription.customer_id}" +
                                " current_term_start : ${(it.data as SubscriptionDetailsWrapper).cb_subscription.current_term_start} " +
                                " current_term_end : ${(it.data as SubscriptionDetailsWrapper).cb_subscription.current_term_end}"
                    )

                    subscriptionStatus.postValue((it.data as SubscriptionDetailsWrapper).cb_subscription.status)
                }
                is ChargebeeResult.Error ->{
                    Log.e(TAG, "Exception from server- retrieveSubscription() :  ${it.exp.message}")
                    cbException.postValue(it.exp)
                }
            }
        }
    }
    fun retrieveSubscriptionsByCustomerId(queryParams: Map<String, String>) {
        Chargebee.retrieveSubscriptions(queryParams) {
            when(it){
                is ChargebeeResult.Success -> {
                    subscriptionList.postValue((it.data as CBSubscription).list)
                }
                is ChargebeeResult.Error ->{
                    Log.e(TAG, "Exception from server- retrieveSubscriptions() :  ${it.exp.message}")
                    cbException.postValue(it.exp)
                }
            }
        }
    }

    fun retrieveEntitlements(subscriptionId: String) {
        Chargebee.retrieveEntitlements(subscriptionId) {
            when(it){
                is ChargebeeResult.Success -> {
                    Log.i(
                        TAG,
                        "entitlements response:  ${(it.data)}"
                    )
                    entitlementsResult.postValue("${(it.data as CBEntitlements).list.size}")

                }
                is ChargebeeResult.Error ->{
                    Log.e(TAG, "Exception from server- retrieveEntitlements() :  ${it.exp.message}")
                    cbException.postValue(it.exp)
                }
            }
        }
    }

}