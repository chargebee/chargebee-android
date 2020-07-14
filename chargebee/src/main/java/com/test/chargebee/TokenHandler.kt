package com.test.chargebee

import android.util.Log
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.test.chargebee.models.CBMerchantPaymentConfig
import com.test.chargebee.service.TokenService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

public class TokenHandler(val handler: (CBMerchantPaymentConfig?) -> Unit) {

    public fun retrieveConfig() {
        val gson: Gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(CBEnvironment.baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val service = retrofit.create(TokenService::class.java)
        val retrievePlan = service.retrieveConfig()
        retrievePlan?.enqueue(object : Callback<CBMerchantPaymentConfig?> {
            override fun onFailure(call: Call<CBMerchantPaymentConfig?>, t: Throwable) {
                Log.d("message", "Failure")
                Log.d("message", t.localizedMessage ?: "Some Error")
                TODO("Not yet implemented")
            }

            override fun onResponse(call: Call<CBMerchantPaymentConfig?>, response: Response<CBMerchantPaymentConfig?>) {
                Log.d("message", "Success")
                Log.d("message", response.toString())
                Log.d("message", this.toString())
                val body: CBMerchantPaymentConfig? = response.body()
                handler(body)
            }
        })
    }
}