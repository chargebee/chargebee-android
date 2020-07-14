package com.test.chargebee

import android.util.Log
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.test.chargebee.models.CBPaymentMethodType
import com.test.chargebee.models.CBTokenWrapper
import com.test.chargebee.service.TokenService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal class TempTokenHandler {

    internal fun createTempToken(
        gatewayToken: String,
        paymentMethod: CBPaymentMethodType,
        gatewayId: String,
        handler: (String?) -> Unit
    ) {
        val gson: Gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(CBEnvironment.baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val service = retrofit.create(TokenService::class.java)
        val createTempToken = service.create(
            gatewayId = gatewayId,
            gatewayToken = gatewayToken,
            paymentMethodType = paymentMethod.displayName
        )
        createTempToken?.enqueue(object : Callback<CBTokenWrapper?> {
            override fun onFailure(call: Call<CBTokenWrapper?>, t: Throwable) {
                Log.d("message", "Failure")
                Log.d("message", t.localizedMessage ?: "Some Error")
            }

            override fun onResponse(
                call: Call<CBTokenWrapper?>,
                response: Response<CBTokenWrapper?>
            ) {
                Log.d("message", "Success")
                Log.d("message", response.toString())
                Log.d("message", this.toString())
                val body: CBTokenWrapper? = response.body()
                handler(body?.token?.id)
            }
        })
    }
}