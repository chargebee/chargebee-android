package com.test.chargebee

import com.test.chargebee.models.Plan
import com.test.chargebee.service.PlanService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PlanHandler {

    fun retrieve(planId: String, handler: (Plan?) -> Unit) {
        retrieveInBackground(planId, handler)
    }

    private fun retrieveInBackground(planId: String, handler: (Plan?) -> Unit) {
        val retrofit = Retrofit.Builder()
            .baseUrl(CBEnvironment.baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service: PlanService = retrofit.create(PlanService::class.java)
        CoroutineScope(Dispatchers.IO).launch {
        val planWrapper = service.retrievePlan(planId = planId)
        withContext(Dispatchers.Main) {
            handler(planWrapper?.plan)
        }
    }}
}
