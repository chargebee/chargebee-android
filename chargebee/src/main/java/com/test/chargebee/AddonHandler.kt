package com.test.chargebee

import com.test.chargebee.models.Addon
import com.test.chargebee.resources.AddonResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddonHandler {

    fun retrieve(addonId: String, handler: (CBResult<Addon>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val addonResult = AddonResource().retrieve(addonId)
            handler(addonResult)
        }
    }
}