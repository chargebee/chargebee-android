package com.test.chargebee

import android.util.Log
import com.test.chargebee.models.Plan
import com.test.chargebee.service.PlanService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


public class Chargebee {
    fun hello(): String {
        return "hello"
    }

    public fun retrievePlan(planId: String, handler: (Plan?) -> Unit) {
        val planHandler = PlanHandler(handler)
        planHandler.retrievePlan(planId)
    }
}