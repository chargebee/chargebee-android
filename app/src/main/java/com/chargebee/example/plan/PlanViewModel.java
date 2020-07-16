package com.chargebee.example.plan;

import android.util.Log;

import androidx.core.util.Consumer;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.chargebee.android.CBResult;
import com.chargebee.android.exceptions.CBException;
import com.chargebee.android.models.Plan;

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