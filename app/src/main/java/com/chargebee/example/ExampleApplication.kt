package com.chargebee.example

import android.app.Application
import com.chargebee.android.CBEnvironment

class ExampleApplication: Application() {

    override fun onCreate() {
        super.onCreate()

//      Configure the Chargebee site credentials at the start of the application
        CBEnvironment.configure(site = "test-ashwin1-test", apiKey = "test_1PDU9iynvhEcPMgWAJ0QZw90d2Aw92ah")
    }
}