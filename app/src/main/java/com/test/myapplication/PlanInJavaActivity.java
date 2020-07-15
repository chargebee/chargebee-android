package com.test.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.test.chargebee.CBException;
import com.test.chargebee.PlanHandler;
import com.test.chargebee.models.PlanWrapper;

public class PlanInJavaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_in_java);
        new PlanViewModel().retrievePlan();
        new TokenViewModel().createToken();
        new AddonViewModel().retrieveAddon();
    }

}