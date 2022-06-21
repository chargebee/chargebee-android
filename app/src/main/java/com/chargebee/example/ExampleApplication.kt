package com.chargebee.example

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import com.chargebee.android.Chargebee

class ExampleApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        if (isInternetAvailable(this))
        // Please add site/app details as required
        Chargebee.configure(site = "", publishableApiKey= "",sdkKey= "", packageName = this.packageName)

    }

    private fun isInternetAvailable(context: Context): Boolean {
        val conMgr = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return conMgr.activeNetworkInfo != null
    }
}