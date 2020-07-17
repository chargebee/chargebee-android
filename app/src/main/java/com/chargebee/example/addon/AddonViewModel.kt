package com.chargebee.example.addon

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chargebee.android.exceptions.CBException
import com.chargebee.android.models.Addon

class AddonViewModel: ViewModel() {
    val addonResult: MutableLiveData<Addon> by lazy {
        MutableLiveData<Addon>()
    }

    val addonError: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    fun retrieveAddon(addonId: String) {
        Addon.retrieve(addonId) { result ->
            try {
                val data = result.getData()
                addonResult.postValue(data)
                Log.d("success", data.toString())
            } catch (ex: CBException) {
                Log.d("error", ex.toString());
                addonError.postValue(ex.message)
            }
        }
    }
}