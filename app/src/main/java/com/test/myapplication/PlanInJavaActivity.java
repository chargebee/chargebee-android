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
//        new PlanViewModel().retrievePlan();
//        new TokenViewModel().createToken();
//        new AddonViewModel().retrieveAddon();
        PlanHandler planHandler = new PlanHandler();
        planHandler.retrieve("cb-demo-no-trial", (result) -> {
            try {
                PlanWrapper data = result.getData();
                Log.d("message", "SUCCESS");
                Log.d("message", data.toString());
            } catch (CBException ex) {
                Log.d("message", "ERROR");
                Log.d("message", ex.toString());
            }
            return null;
        });
    }

}