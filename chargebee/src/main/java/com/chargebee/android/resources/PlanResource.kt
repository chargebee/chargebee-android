package com.chargebee.android.resources

import com.chargebee.android.*
import com.chargebee.android.exceptions.CBException
import com.chargebee.android.models.Plan
import com.chargebee.android.repository.PlanRepository

internal class PlanResource: BaseResource(CBEnvironment.baseUrl) {

    suspend fun retrieve(planId: String): CBResult<Plan> {
        val planResponse = apiClient.create(PlanRepository::class.java).retrievePlan(planId = planId)
        val result = fromResponse(
            planResponse,
            CBErrorDetail::class.java
        )
        return try {
            Success(result.getData().plan)
        } catch (ex: CBException) {
            Failure(ex)
        }
    }
}