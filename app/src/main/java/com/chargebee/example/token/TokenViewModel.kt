package com.chargebee.example.token

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chargebee.android.models.Token
import com.chargebee.android.exceptions.InvalidRequestException
import com.chargebee.android.exceptions.OperationFailedException
import com.chargebee.android.exceptions.PaymentException
import com.chargebee.android.models.PaymentDetail

class TokenViewModel : ViewModel() {
    val result: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun create(paymentDetail: PaymentDetail) {
        Token.createTempToken(paymentDetail) {
            try {
                val cbTempToken = it.getData()
                Log.d("success", cbTempToken)
                result.postValue(cbTempToken)
            } catch (ex: PaymentException) {
                Log.d("error payment", ex.toString())
                result.postValue(ex.message)
            } catch (ex: InvalidRequestException) {
                Log.d("error invalid", ex.toString())
                result.postValue(ex.message)
            } catch (ex: OperationFailedException) {
                Log.d("error operation", ex.toString())
                result.postValue(ex.message)
            }
        }
    }
}