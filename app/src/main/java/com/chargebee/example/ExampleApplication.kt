package com.chargebee.example

import android.app.Application
import com.chargebee.android.Chargebee

class ExampleApplication: Application() {

    override fun onCreate() {
        super.onCreate()

//      Configure the Chargebee site credentials at the start of the application
       // Chargebee.configure(site = "cb-imay-test", publishableApiKey = "test_EojsGoGFeHoc3VpGPQDOZGAxYy3d0FF3", sdkKey = "cb-j53yhbfmtfhfhkmhow3ramecom")

       // Chargebee.configure(site = "omni1-test", publishableApiKey = "test_uM0iFFcuI9cuXboD7Sk5zJngcuuhQj8xB8V", sdkKey = "cb-j53yhbfmtfhfhkmhow3ramecom")

        Chargebee.configure(site = "omni1-test.integrations", publishableApiKey = "test_rpKneFyplowONFtdHgnlpxh6ccdcQXNUcu", sdkKey = "cb-pte6d5ltebfrnpxcnw4s5kcl2m")
    }
}