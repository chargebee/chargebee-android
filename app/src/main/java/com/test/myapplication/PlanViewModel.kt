package com.test.myapplication

import android.util.Log
import androidx.lifecycle.ViewModel
import com.test.chargebee.CBException
import com.test.chargebee.PlanHandler

class PlanViewModel: ViewModel() {
    fun retrievePlan() {
        PlanHandler().retrieve("cb-demo-no-trial") { result ->
            try {
                val data = result.getData()
                Log.d("message", "SUCCESS");
                Log.d("message", data.toString())
            } catch (ex: CBException) {
                Log.d("message", "ERROR");
                Log.d("message", ex.error.toString());
            }
        }
    }
}