package com.chargebee.example.plan;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.chargebee.android.ErrorDetail;
import com.chargebee.android.models.Plan;
import com.chargebee.example.BaseActivity;
import com.chargebee.example.R;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class PlanInJavaActivity extends BaseActivity {

    private PlanViewModel viewModel;
    private EditText planIdInput;
    private Button planButton;

    private TextView planName;
    private TextView planPricingText;
    private TextView errorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_in_java);

        planIdInput = findViewById(R.id.planIdInput);
        planButton = findViewById(R.id.planButton);
        planName = findViewById(R.id.planName);
        planPricingText = findViewById(R.id.planPricing);
        errorText = findViewById(R.id.errorText);

        this.viewModel = new PlanViewModel();

        this.viewModel.getPlanResult().observe(this, plan -> {
            hideProgressDialog();
            planName.setText(plan.getName());
            planPricingText.setText(plan.getPricingModel());
        });

        this.viewModel.getPlanError().observe(this, message -> {
            hideProgressDialog();
            errorText.setText(new Gson().fromJson(message, ErrorDetail.class).getMessage());
        });
        this.planButton.setOnClickListener(view -> {
            this.clearFields();
            showProgressDialog();
            this.viewModel.retrievePlan(planIdInput.getText().toString());
        });
    }

    private void clearFields() {
        this.planName.setText("");
        this.planPricingText.setText("");
        this.errorText.setText("");
    }
}