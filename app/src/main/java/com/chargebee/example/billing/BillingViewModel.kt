package com.chargebee.example.billing

import android.content.Context
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
import com.chargebee.android.models.CBProduct
import com.chargebee.android.models.CBSubscription
import com.chargebee.android.models.SubscriptionDetailsWrapper
import com.google.gson.Gson

class BillingViewModel : ViewModel() {

    private val TAG = "BillingViewModel"
    var productIdsList: MutableLiveData<Array<String>> = MutableLiveData()
    var productPurchaseResult: MutableLiveData<Boolean> = MutableLiveData()
    var cbException: MutableLiveData<String?> = MutableLiveData()
    var subscriptionStatus: MutableLiveData<String?> = MutableLiveData()
    var subscriptionList: MutableLiveData<ArrayList<SubscriptionDetailsWrapper>?> = MutableLiveData()
    var error: MutableLiveData<String?> = MutableLiveData()
    private var subscriptionId: String = ""
    var updateProductPurchaseResult: MutableLiveData<String> = MutableLiveData()
    var priceChangeLiveData: MutableLiveData<String> = MutableLiveData()

    fun purchaseProduct(product: CBProduct, customerID: String) {

        CBPurchase.purchaseProduct(product, customerID,  object : CBCallback.PurchaseCallback<String>{
            override fun onSuccess(subscriptionID: String, status:Boolean) {
                Log.i(TAG, "Subscription ID:  $subscriptionID")
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
                    error.postValue(Gson().fromJson<ErrorDetail>(
                        it.exp.message,
                        ErrorDetail::class.java
                    ).message)
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
                    error.postValue(Gson().fromJson<ErrorDetail>(
                        it.exp.message,
                        ErrorDetail::class.java
                    ).message)
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
                    error.postValue(Gson().fromJson<ErrorDetail>(
                        it.exp.message,
                        ErrorDetail::class.java
                    ).message)
                }
            }
        }
    }

    fun updatePurchase(productIdList: ArrayList<String>, oldPurchaseToken: String, context: Context){
        CBPurchase.retrieveProducts(context,
            productIdList,object : CBCallback.ListProductsCallback<ArrayList<CBProduct>>{
                override fun onSuccess(productIDs: ArrayList<CBProduct>) {
                    if (productIDs.size > 0) {
                        CBPurchase.updateProduct(context, productIDs.first(),oldPurchaseToken, object : CBCallback.PurchaseCallback<String>{
                            override fun onSuccess(purchaseToken: String, status:Boolean) {
                                Log.i(TAG, "purchaseToken:  $purchaseToken")
                                updateProductPurchaseResult.postValue(purchaseToken)
                            }
                            override fun onError(error: CBException) {
                                try {
                                    cbException.postValue(error.message)
                                }catch (exp: Exception){
                                    Log.i(TAG, "Exception :${exp.message}")
                                }
                            }

                        })
                    } else {
                        Log.i(javaClass.simpleName,"Product id not found in Google Play")
                        cbException.postValue("Product id not found in Google Play")
                    }
                }

                override fun onError(error: CBException) {
                    Log.e(javaClass.simpleName, "Error:  ${error.message}")
                    try {
                        cbException.postValue(error.message)
                    }catch (exp: Exception){
                        Log.e(TAG, "Exception :${exp.message}")
                    }
                }

            })

    }

    fun priceChangeUpdate(productIdList: ArrayList<String>, context: Context){
        CBPurchase.retrieveProducts(
            context,
            productIdList,
            object : CBCallback.ListProductsCallback<ArrayList<CBProduct>> {
                override fun onSuccess(productIDs: ArrayList<CBProduct>) {
                        if (productIDs.size > 0) {
                            CBPurchase.priceChangeConfirmation(productIDs.first(), object: CBCallback.PriceChangeCallback<String>{
                                override fun onSuccess(response: String) {
                                    priceChangeLiveData.postValue(response)
                                }

                                override fun onError(error: CBException) {
                                    Log.e(javaClass.simpleName,"Error in Price Change: ${error.message}")
                                    try {
                                        cbException.postValue(error.message)
                                    }catch (exp: Exception){
                                        Log.i(TAG, "Exception :${exp.message}")
                                    }
                                }

                            })
                        } else {
                            Log.i(javaClass.simpleName,"Product id not found in Google Play")
                            cbException.postValue("Product id not found in Google Play")
                        }
                }
                override fun onError(error: CBException) {
                    Log.e(javaClass.simpleName, "Error:  ${error.message}")
                    try {
                        cbException.postValue(error.message)
                    }catch (exp: Exception){
                        Log.e(TAG, "Exception :${exp.message}")
                    }
                }
            })
    }
}