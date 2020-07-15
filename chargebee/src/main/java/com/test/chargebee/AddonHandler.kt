package com.test.chargebee

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.test.chargebee.models.AddonWrapper
import com.test.chargebee.service.AddonService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AddonHandler {

    fun retrieve(addonId: String, handler: (CBResult<AddonWrapper>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = retrieveAddon(addonId)
            handler(result)
        }
    }

    private suspend fun retrieveAddon(addonId: String): CBResult<AddonWrapper> {
        val gson: Gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(CBEnvironment.baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val service = retrofit.create(AddonService::class.java)
        val addOnResponse = service.retrieveAddon(addonId = addonId)
        return fromResponse(addOnResponse, CBErrorDetail::class.java)
    }
}