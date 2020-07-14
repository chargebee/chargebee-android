package com.test.myapplication

import android.util.Log
import androidx.lifecycle.ViewModel
import com.test.chargebee.AddonHandler

class AddonViewModel: ViewModel() {
    fun retrieveAddon() {

        val addonHandler = AddonHandler()
        addonHandler.retrieve("cbdemo_setuphelp") {
            Log.d("addon", it.toString())
        }
    }
}