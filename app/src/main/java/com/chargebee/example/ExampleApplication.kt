package com.chargebee.example

import android.app.Application
import com.chargebee.android.Chargebee

class ExampleApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        // Configure the Chargebee site credentials at the start of the application
        Chargebee.applicationId = BuildConfig.APPLICATION_ID
      //  Chargebee.configure(site = "cb-imay-test", publishableApiKey = "test_EojsGoGFeHoc3VpGPQDOZGAxYy3d0FF3", sdkKey = "cb-j53yhbfmtfhfhkmhow3ramecom")
        Chargebee.configure(site = "cb-imay-test", publishableApiKey = "test_EojsGoGFeHoc3VpGPQDOZGAxYy3d0FF3", sdkKey = "cb-x2wiixyjr5bl5ihugstyp2exbi") // For play store

       // Chargebee.configure(site = "cb-imay-test", publishableApiKey = "test_AVrzSIux7PHMmiMdi7ixAiqoVYE9jHbz", sdkKey = "cb-j53yhbfmtfhfhkmhow3ramecom")

        //  Chargebee.configure(site = "omni3-test", publishableApiKey = "test_Dcdi0TBSqL3KJdvCNKNRBG3cis1HMvFpd", sdkKey = "cb-j53yhbfmtfhfhkmhow3ramecom")

         // Chargebee.configure(site = "omni1-test.integrations", publishableApiKey = "test_rpKneFyplowONFtdHgnlpxh6ccdcQXNUcu", sdkKey = "cb-pte6d5ltebfrnpxcnw4s5kcl2m")

       // Chargebee.configure(site = "omni1-test.integrations", publishableApiKey = "test_rpKneFyplowONFtdHgnlpxh6ccdcQXNUcu", sdkKey = "cb-wgvl2fbeebeglpen2vcal366ry")
       // cb-wgvl2fbeebeglpen2vcal366ry
    }
}