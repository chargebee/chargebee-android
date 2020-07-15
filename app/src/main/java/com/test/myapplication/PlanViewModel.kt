package com.test.myapplication

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.test.chargebee.CBException
import com.test.chargebee.PlanHandler
import com.test.chargebee.models.Plan

class PlanViewModel: ViewModel() {

    val planResult: MutableLiveData<Plan> by lazy {
        MutableLiveData<Plan>()
    }

    val planError: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun retrievePlan(planId: String) {
        PlanHandler().retrieve(planId) { result ->
            try {
                val data = result.getData()
                planResult.postValue(data)
                Log.d("message", "SUCCESS");
                Log.d("message", data.toString())
            } catch (ex: CBException) {
                Log.d("message", "ERROR");
                Log.d("message", ex.toString());
                Log.d("message", ex.error.toString());
                planError.postValue(ex.error.message)
            }
        }
    }
}