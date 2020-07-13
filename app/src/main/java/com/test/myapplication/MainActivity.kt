package com.test.myapplication

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.test.chargebee.CBEnvironment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("message", "Another message")
        CBEnvironment.configure(site = "test-ashwin1-test", apiKey = "test_1PDU9iynvhEcPMgWAJ0QZw90d2Aw92ah")
        PlanViewModel().retrievePlan()
        AddonViewModel().retrieveAddon()
        AddonViewModel().retrieveAddon()
    }
}