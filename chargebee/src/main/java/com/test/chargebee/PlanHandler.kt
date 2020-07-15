package com.test.chargebee

import com.test.chargebee.models.Plan
import com.test.chargebee.resources.PlanResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlanHandler {

    fun retrieve(planId: String, handler: (CBResult<Plan>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val planResult = PlanResource().retrieve(planId)
            handler(planResult)
        }
    }
}
