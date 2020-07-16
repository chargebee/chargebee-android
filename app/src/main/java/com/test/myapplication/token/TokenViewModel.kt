package com.test.myapplication.token

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.test.chargebee.models.Token
import com.test.chargebee.exceptions.InvalidRequestException
import com.test.chargebee.exceptions.OperationFailedException
import com.test.chargebee.exceptions.PaymentException
import com.test.chargebee.models.CBPaymentDetail

class TokenViewModel : ViewModel() {
    val result: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun create(paymentDetail: CBPaymentDetail) {
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