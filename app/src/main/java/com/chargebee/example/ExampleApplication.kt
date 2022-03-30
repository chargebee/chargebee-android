package com.chargebee.example

import android.app.Application
import com.chargebee.android.Chargebee

class ExampleApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        // Please add site/app details as required
        Chargebee.configure(site = "", publishableApiKey= "",sdkKey= "", packageName = this.packageName)

    }
}