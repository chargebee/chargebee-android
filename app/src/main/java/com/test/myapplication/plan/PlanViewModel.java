package com.test.myapplication.plan;

import android.util.Log;

import androidx.core.util.Consumer;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.test.chargebee.CBResult;
import com.test.chargebee.exceptions.CBException;
import com.test.chargebee.models.Plan;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

class PlanViewModel extends ViewModel {

    MutableLiveData<Plan> planResult = new MutableLiveData();
    MutableLiveData<String> planError = new MutableLiveData();

    void retrievePlan(String planId) {
        Plan.retrieve(planId, (Consumer<CBResult<Plan>>) plan -> {
            try {
                Plan data = plan.getData();
                Log.d("success", data.toString());
                planResult.postValue(data);
            } catch (CBException ex) {
                Log.d("error", ex.toString());
                planError.postValue(ex.getMessage());
            }
        });
    }
}