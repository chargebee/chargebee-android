package com.test.myapplication

import android.util.Log
import androidx.lifecycle.ViewModel
import com.test.chargebee.Chargebee
import com.test.chargebee.models.Plan

class PlanViewModel: ViewModel() {
    fun retrievePlan() {

        Chargebee().retrievePlan("cb-demo-no-trial") { plan: Plan? ->
            Log.d("message", plan.toString())
        }
    }
}