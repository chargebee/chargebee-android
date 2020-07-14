package com.test.chargebee

import android.util.Log
import com.test.chargebee.models.Plan
import com.test.chargebee.models.PlanWrapper
import com.test.chargebee.service.PlanService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PlanHandler(val handler: (Plan?) -> Unit) : Callback<PlanWrapper?>  {

    public fun retrievePlan(planId: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(CBEnvironment.baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service: PlanService = retrofit.create(PlanService::class.java)
        val retrievePlan = service.retrievePlan(planId = planId)
        retrievePlan?.enqueue(this)
    }

    override fun onFailure(call: Call<PlanWrapper?>, t: Throwable) {
        Log.d("message", "Failure")
        Log.d("message", t.localizedMessage ?: "Some Error")
        TODO("Not yet implemented")
    }

    override fun onResponse(call: Call<PlanWrapper?>, response: Response<PlanWrapper?>) {
        Log.d("message", "Success")
        Log.d("message", response.toString())
        val body: PlanWrapper? = response.body()
        this.handler(body?.plan)
    }
}