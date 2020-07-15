package com.test.myapplication

import android.util.Log
import androidx.lifecycle.ViewModel
import com.test.chargebee.AddonHandler
import com.test.chargebee.CBException

class AddonViewModel: ViewModel() {
    fun retrieveAddon() {
        val addonHandler = AddonHandler()
        addonHandler.retrieve("cbdemo_setuphelp") { result ->
            try {
                val data = result.getData()
                Log.d("message", "SUCCESS");
                Log.d("message", data.toString())
            } catch (ex: CBException) {
                Log.d("message", "ERROR");
                Log.d("message", ex.error.toString());
            }
        }
    }
}