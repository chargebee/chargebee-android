package com.chargebee.example

import android.app.Application
import com.chargebee.android.Chargebee

class ExampleApplication: Application() {

    override fun onCreate() {
        super.onCreate()

//      Configure the Chargebee site credentials at the start of the application
        Chargebee.configure(site = "cb-imay-test", publishableApiKey = "test_EojsGoGFeHoc3VpGPQDOZGAxYy3d0FF3", sdkKey = "cb-j53yhbfmtfhfhkmhow3ramecom1")

    }
}