package com.test.chargebee

import android.util.Log
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.test.chargebee.models.StripeCard
import com.test.chargebee.models.StripeToken
import com.test.chargebee.models.toFormBody
import com.test.chargebee.service.StripeService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

public class StripeHandler(val handler: (StripeToken?) -> Unit) {

    public fun createToken() {
        val gson: Gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.stripe.com/v1/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val service = retrofit.create(StripeService::class.java)
        val card = StripeCard("4242424242424242",
        "09",
        "29",
        "123")
        val merchantKey = "Bearer pk_test_F97JzyrPUIFE5KSlCxBj8Eq000LP8an6pJ"
        val retrievePlan = service.createToken(merchantKey, card.toFormBody())
        retrievePlan?.enqueue(object : Callback<StripeToken?> {
            override fun onFailure(call: Call<StripeToken?>, t: Throwable) {
                Log.d("message", "Failure")
                Log.d("message", t.localizedMessage ?: "Some Error")
                TODO("Not yet implemented")
            }

            override fun onResponse(call: Call<StripeToken?>, response: Response<StripeToken?>) {
                Log.d("message", "Success")
                Log.d("message", response.toString())
                Log.d("message", this.toString())
                val body: StripeToken? = response.body()
                handler(body)
            }
        })
    }
}