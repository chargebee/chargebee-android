package com.test.chargebee.resources

import com.test.chargebee.*
import com.test.chargebee.exceptions.CBException
import com.test.chargebee.models.Plan
import com.test.chargebee.repository.PlanRepository

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