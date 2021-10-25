package com.chargebee.example.billing

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chargebee.android.billingservice.CBCallback
import com.chargebee.android.billingservice.CBPurchase
import com.chargebee.android.billingservice.PurchaseModel
import com.chargebee.android.exceptions.CBException
import com.chargebee.android.models.Products

class BillingViewModel : ViewModel() {

    var productPurchaseResult: MutableLiveData<Any?> = MutableLiveData()
    var cbException: MutableLiveData<CBException?> = MutableLiveData()
    var mPurchaseTokenStatus: MutableLiveData<Any?> = MutableLiveData()
    private val TAG = "BillingViewModel"

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

   /* fun validateSDKKey(sdkKey: String?, customerId: String?) {
        validateSdkKey(sdkKey!!, customerId!!,object: CBCallback.ValidateSDKKeyCallback<CBPurchaseFailure>{
            override fun onSuccess(success: ) {
                TODO("Not yet implemented")
            }

            override fun onError(error: CBException) {
                TODO("Not yet implemented")
            }

        })
    }

    fun updatePurchaseToken(purchaseModel: PurchaseModel) {
        KeyValidation.updatePurchaseToken(object: CBCallback.ValidateSDKKeyCallback<CBPurchaseFailure>{
            override fun onSuccess(success: ) {
                TODO("Not yet implemented")
            }

            override fun onError(error: CBException) {
                TODO("Not yet implemented")
            }

        })
    }*/

}