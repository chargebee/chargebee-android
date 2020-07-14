package com.test.myapplication

import android.app.Application
import com.test.chargebee.CBEnvironment

class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        CBEnvironment.configure(site = "test-ashwin1-test", apiKey = "test_1PDU9iynvhEcPMgWAJ0QZw90d2Aw92ah")
    }
}