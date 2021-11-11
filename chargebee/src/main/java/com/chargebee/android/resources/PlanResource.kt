package com.chargebee.android.resources

import com.chargebee.android.*
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.models.Plan
import com.chargebee.android.repository.ItemsRepository
import com.chargebee.android.repository.PlanRepository

internal class PlanResource: BaseResource(Chargebee.baseUrl) {

  /*  suspend fun retrieve(planId: String): CBResult<Plan> {
        val planResponse = apiClient.create(PlanRepository::class.java).retrievePlan(planId = planId)
        val result = fromResponse(
            planResponse,
            ErrorDetail::class.java
        )
        return Success(result.getData().plan)
    }*/

    suspend fun retrievePlan(planId: String): ChargebeeResult<Any> {
        val planResponse = apiClient.create(PlanRepository::class.java).retrievePlan(planId = planId)

        return responseFromServer(
            planResponse,
        ErrorDetail::class.java)
    }

    suspend fun retrieveAllPlans(params: Array<String>): ChargebeeResult<Any> {
        val itemsResponse = apiClient.create(PlanRepository::class.java).retrieveAllPlans(sort = params.get(0), channel = params.get(1))

        return responseFromServer(
            itemsResponse,
            ErrorDetail::class.java)
    }


}