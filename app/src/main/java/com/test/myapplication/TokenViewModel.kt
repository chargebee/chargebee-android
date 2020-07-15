package com.test.myapplication

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.test.chargebee.CBException
import com.test.chargebee.TokenHandler
import com.test.chargebee.models.CBCard
import com.test.chargebee.models.CBPaymentDetail
import com.test.chargebee.models.CBPaymentMethodType
import kotlinx.coroutines.launch

class TokenViewModel : ViewModel() {
    fun createToken() {
        viewModelScope.launch {
            val card = CBCard("4242424242424242", "09", "29", "123")
            val paymentDetail = CBPaymentDetail("USD", CBPaymentMethodType.CARD, card)
            TokenHandler().tokenize(paymentDetail) {
                try {
                    val cbTempToken = it.getData()
                    Log.d("message", "-=-=-=-")
                    Log.d("message", cbTempToken)
                } catch (ex: CBException) {
                    Log.d("message", "-=-=-=-")
                    Log.d("message", ex.toString())
                    Log.d("message", ex.error.toString())
                }
            }
        }
    }
}