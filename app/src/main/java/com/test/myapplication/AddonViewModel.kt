package com.test.myapplication

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.test.chargebee.AddonHandler
import com.test.chargebee.exceptions.CBException
import com.test.chargebee.models.Addon

class AddonViewModel: ViewModel() {
    val addonResult: MutableLiveData<Addon> by lazy {
        MutableLiveData<Addon>()
    }

    val addonError: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    fun retrieveAddon(addonId: String) {
        val addonHandler = AddonHandler()
        addonHandler.retrieve(addonId) { result ->
            try {
                val data = result.getData()
                addonResult.postValue(data)
                Log.d("success", data.toString())
            } catch (ex: CBException) {
                Log.d("error", ex.error.toString());
                addonError.postValue(ex.error.message)
            }
        }
    }
}