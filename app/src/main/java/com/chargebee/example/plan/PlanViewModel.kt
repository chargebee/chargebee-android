package com.chargebee.example.plan

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chargebee.android.ErrorDetail
import com.chargebee.android.exceptions.CBException
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.models.Plan
import com.chargebee.android.models.PlanWrapper

class PlanViewModel : ViewModel() {
    var planResult: MutableLiveData<Any?> = MutableLiveData()
    var planError: MutableLiveData<Any?> = MutableLiveData()

    fun retrievePlan(planId: String) {
        Plan.retrievePlan(planId) {
            when(it){
                is ChargebeeResult.Success ->{
                    planResult.postValue((it.data as PlanWrapper?)?.plan)
                }
                is ChargebeeResult.Error ->{
                    planError.postValue(it.exp.message)
                }
            }
        }
    }

}