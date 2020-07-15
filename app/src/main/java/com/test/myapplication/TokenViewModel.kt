package com.test.myapplication

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.test.chargebee.TokenHandler
import com.test.chargebee.exceptions.CBException
import com.test.chargebee.models.CBPaymentDetail

class TokenViewModel : ViewModel() {
    val result: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun create(paymentDetail: CBPaymentDetail) {
        TokenHandler().tokenize(paymentDetail) {
            try {
                val cbTempToken = it.getData()
                Log.d("success", cbTempToken)
                result.postValue(cbTempToken)
            } catch (ex: CBException) {
                Log.d("error", ex.error.toString())
                result.postValue(ex.error.message)
            }
        }
    }
}