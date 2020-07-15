package com.test.myapplication;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.test.chargebee.exceptions.CBException;
import com.test.chargebee.models.Plan;

class PlanViewModel extends ViewModel {

    MutableLiveData<Plan> planResult = new MutableLiveData();
    MutableLiveData<String> planError = new MutableLiveData();

    void retrievePlan(String planId) {
        Plan.retrieve(planId, plan -> {
            try {
                Plan data = plan.getData();
                Log.d("success", data.toString());
                planResult.postValue(data);
            } catch (CBException ex) {
                Log.d("error", ex.toString());
                planError.postValue(ex.getMessage());
            }
            return null;
        });
    }
}