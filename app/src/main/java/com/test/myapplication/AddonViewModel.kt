package com.test.myapplication

import android.util.Log
import androidx.lifecycle.ViewModel
import com.test.chargebee.AddonHandler
import com.test.chargebee.models.Addon

class AddonViewModel: ViewModel() {
    fun retrieveAddon() {

        val completionHandler: (Addon?) -> Unit = { addon: Addon? ->
            Log.d("message", addon.toString())
        }
        val addonHandler = AddonHandler(completionHandler)
        addonHandler.retrieveAddon("cbdemo_setuphelp")
    }
}