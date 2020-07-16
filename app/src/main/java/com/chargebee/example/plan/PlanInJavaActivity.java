package com.chargebee.example.plan;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.chargebee.example.R;

public class PlanInJavaActivity extends AppCompatActivity {

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

        this.viewModel.planResult.observe(this, plan -> {
            planName.setText(plan.getName());
            planPricingText.setText(plan.getPricingModel());
        });

        this.viewModel.planError.observe(this, message -> {
            errorText.setText(message);
        });
        this.planButton.setOnClickListener(view -> {
            this.viewModel.retrievePlan(planIdInput.getText().toString());
        });
    }
}