package com.chargebee.example.plan

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.models.*


class PlanViewModel : ViewModel() {
    var mPlansResult: MutableLiveData<ArrayList<String>?> = MutableLiveData()
    var planResult: MutableLiveData<Plan?> = MutableLiveData()
    var planError: MutableLiveData<String?> = MutableLiveData()
    var mPlansList = ArrayList<String>()

    fun retrievePlan(planId: String) {
        Plan.retrievePlan(planId) {
            when(it){
                is ChargebeeResult.Success -> {
                    planResult.postValue((it.data as PlanWrapper?)?.plan)
                }
                is ChargebeeResult.Error -> {
                    planError.postValue(it.exp.message)
                }
            }
        }
    }


    fun retrieveAllPlans(queryParam: Array<String>) {
        Plan.retrieveAllPlans(queryParam) {
            when (it) {
                is ChargebeeResult.Success -> {
                    Log.i(javaClass.simpleName, "list plans :  ${it.data}")
                    mPlansList.clear()
                    for (item in  (it.data as PlansWrapper).list){
                        Log.i(javaClass.simpleName, "Plan id :  ${item.plan.id}")
                        mPlansList.add(item.plan.name)
                    }
                    mPlansResult.postValue((mPlansList))
                }
                is ChargebeeResult.Error -> {
                    Log.d(javaClass.simpleName, "exception :  ${it.exp.message}")
                    planError.postValue(it.exp.message)
                }
            }
        }
    }

}