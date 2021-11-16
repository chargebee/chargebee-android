package com.chargebee.example.billing

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chargebee.android.CBResult
import com.chargebee.android.billingservice.CBCallback
import com.chargebee.android.billingservice.CBPurchase
import com.chargebee.android.billingservice.PurchaseModel
import com.chargebee.android.exceptions.CBException
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.models.KeyValidation
import com.chargebee.android.models.Products
import com.chargebee.android.models.SubscriptionDetail

class BillingViewModel : ViewModel() {

    private val TAG = "BillingViewModel"
    var productPurchaseResult: MutableLiveData<Any?> = MutableLiveData()
    var cbException: MutableLiveData<CBException?> = MutableLiveData()
    var mPurchaseTokenStatus: MutableLiveData<Any?> = MutableLiveData()
    var sdkKeyValidationResult: MutableLiveData<Any?> = MutableLiveData()

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

    fun validateSDKKey(sdkKey: String, customerId: String) {
        KeyValidation.validateSdkKey(sdkKey, customerId) { validateKey: CBResult<KeyValidation> ->
            try {
                val validate = validateKey.getData().boolean
                Log.d(TAG, "validation success $validate")
                sdkKeyValidationResult.postValue(validate)
            } catch (ex: CBException) {
                Log.d(TAG, ex.toString())
                cbException.postValue(ex)
            }
        }
    }
    fun retrieveSubscription(subscriptionId: String) {
        SubscriptionDetail.retrieveSubscription(subscriptionId) {
            when(it){
                is ChargebeeResult.Success ->{
                    Log.i(TAG, "subscription :  ${it.data}")
                }
                is ChargebeeResult.Error ->{
                    Log.d(TAG, "exception :  ${it.exp.message}")
                }
            }
        }
    }
}