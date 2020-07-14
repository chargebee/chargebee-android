package com.test.myapplication

import android.util.Log
import androidx.lifecycle.ViewModel
import com.test.chargebee.PlanHandler
import com.test.chargebee.models.Plan

class PlanViewModel: ViewModel() {
    fun retrievePlan() {

        PlanHandler().retrieve("cb-demo-no-trial") { plan: Plan? ->
            Log.d("message", plan.toString())
        }
    }
}