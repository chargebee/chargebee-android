package com.test.myapplication

import android.util.Log
import androidx.lifecycle.ViewModel
import com.test.chargebee.StripeHandler
import com.test.chargebee.models.StripeToken

class TokenViewModel : ViewModel() {
    fun createToken() {
//        val handler: (CBMerchantPaymentConfig?) -> Unit =
//            { res ->
//                Log.d("message", "final response")
//                Log.d("message", res.toString())
//                Log.d("message", "------------res?.getConfig()")
//                Log.d("message", res?.getPaymentProviderConfig("USD", CBPaymentType.CARD).toString())
//            }
//
//        TokenHandler(handler).retrieveConfig()
        val handler: (StripeToken?) -> Unit =
            { res ->
                Log.d("message", "final response")
                Log.d("message", res.toString())
            }

        StripeHandler(handler).createToken()
    }
}