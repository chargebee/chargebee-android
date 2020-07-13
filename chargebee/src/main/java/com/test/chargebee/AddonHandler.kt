package com.test.chargebee

import android.util.Log
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.test.chargebee.models.Addon
import com.test.chargebee.models.AddonWrapper
import com.test.chargebee.service.AddonService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

public class AddonHandler(val handler: (Addon?) -> Unit) {

    public fun retrieveAddon(addonId: String) {
        val gson: Gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://${CBEnvironment.site}.chargebee.com/api/v2/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val service = retrofit.create(AddonService::class.java)
        val retrievePlan = service.retrieveAddon(addonId = addonId)
        retrievePlan?.enqueue(object : Callback<AddonWrapper?>{
            override fun onFailure(call: Call<AddonWrapper?>, t: Throwable) {
                Log.d("message", "Failure")
                Log.d("message", t.localizedMessage ?: "Some Error")
                TODO("Not yet implemented")
            }

            override fun onResponse(call: Call<AddonWrapper?>, response: Response<AddonWrapper?>) {
                Log.d("message", "Success")
                Log.d("message", response.toString())
                Log.d("message", this.toString())
                val body: AddonWrapper? = response.body()
                handler(body?.addon)
            }
        })
    }
}