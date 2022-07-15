package com.chargebee.example.subscription

import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.chargebee.android.Chargebee
import com.chargebee.example.BaseActivity
import com.chargebee.example.R
import com.chargebee.example.billing.BillingViewModel
import com.google.android.material.textfield.TextInputEditText

class SubscriptionActivity : BaseActivity() {

    private var mBillingViewModel : BillingViewModel? = null
    lateinit var mCustomerIdInput: TextInputEditText
    lateinit var mCustomerIdButton: Button
    lateinit var mSubscriptionIdButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscription)
        mBillingViewModel = BillingViewModel()
        mCustomerIdInput = findViewById(R.id.customerIdInput)
        mCustomerIdButton = findViewById(R.id.customerIdBtn)
        mSubscriptionIdButton = findViewById(R.id.subscriptionIdBtn)

        this.mBillingViewModel!!.error.observeForever {
            hideProgressDialog()
            Log.e(javaClass.simpleName, "Error from server:  $it")
            alertSuccess(it)
        }
        this.mBillingViewModel!!.subscriptionStatus.observeForever {
            hideProgressDialog()
            Log.i(javaClass.simpleName, "subscription status:  $it")
            alertSuccess(it)
        }
        this.mBillingViewModel!!.subscriptionList.observeForever {
            hideProgressDialog()
            Log.i(javaClass.simpleName, "Subscriptions by using queryParams:  $it")
            if(it?.size!! >0) {
                val subscriptionStatus =
                    it?.get(0).cb_subscription.status + "\nPlan Price : " + it?.get(0)?.cb_subscription?.plan_amount;
                alertSuccess(subscriptionStatus)
            }else{
                alertSuccess("Subscriptions not found in Chargebee System")
            }
        }

        mCustomerIdButton.setOnClickListener{
            showProgressDialog()
            val userInput = mCustomerIdInput.text.toString() // Sample QueryParams: "customer_id":"Abc","channel":"play_store","status":"Active"

            val queryParams = userInput.split(",").associate {
                val (left, right) = it.split(":")
                left to right
            }

            mBillingViewModel?.retrieveSubscriptionsByCustomerId(queryParams)
        }

        mSubscriptionIdButton.setOnClickListener{
            showProgressDialog()
            val subscriptionId = mCustomerIdInput.text.toString()
            mBillingViewModel?.retrieveSubscription(subscriptionId)
        }

    }
}