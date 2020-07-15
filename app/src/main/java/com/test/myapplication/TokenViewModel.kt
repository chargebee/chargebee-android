package com.test.myapplication

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.test.chargebee.CBException
import com.test.chargebee.TokenHandler
import com.test.chargebee.models.CBPaymentDetail

class TokenViewModel : ViewModel() {
    val result: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    var temp: String = ""

    fun create(paymentDetail: CBPaymentDetail) {
        val setter = { res: String ->
            Log.d("another", res)
            Log.d("another", "messy here")
            Log.d("another", temp)
//            result.value = res
            Log.d("another", "ronaldo")
        }
        result.value = "Another Value"
//        viewModelScope.launch {
        Log.d("another", "before call")
        TokenHandler().tokenize(paymentDetail) {
            try {
                Log.d("another", "First")
                val cbTempToken = it.getData()
                Log.d("another", cbTempToken)
                temp = cbTempToken
                setter(cbTempToken)
//                    result.value = cbTempToken
            } catch (ex: CBException) {
                Log.d("another", "-=-=-=-")
                Log.d("another", ex.toString())
            }
        }
//        }
    }
}