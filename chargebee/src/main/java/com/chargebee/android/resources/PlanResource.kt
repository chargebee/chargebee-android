package com.chargebee.android.resources

import android.util.Log
import com.chargebee.android.*
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.models.Plan
import com.chargebee.android.repository.ItemsRepository
import com.chargebee.android.repository.PlanRepository

internal class PlanResource: BaseResource(Chargebee.baseUrl) {

    suspend fun retrievePlan(planId: String): ChargebeeResult<Any> {
        val planResponse = apiClient.create(PlanRepository::class.java).retrievePlan(planId = planId)

        Log.i(javaClass.simpleName, " Response :$planResponse")
        return responseFromServer(
            planResponse)
    }

    suspend fun retrieveAllPlans(params: Array<String>): ChargebeeResult<Any> {
        val itemsResponse = apiClient.create(PlanRepository::class.java).retrieveAllPlans(limit = params[0])

        Log.i(javaClass.simpleName, " Response :$itemsResponse")
        return responseFromServer(
            itemsResponse)
    }
}