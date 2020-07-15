package com.test.chargebee.resources

import com.test.chargebee.*
import com.test.chargebee.exceptions.CBException
import com.test.chargebee.models.Addon
import com.test.chargebee.repository.AddonRepository

internal class AddonResource: BaseResource(CBEnvironment.baseUrl) {

    suspend fun retrieve(addonId: String): CBResult<Addon> {
        val planResponse = apiClient.create(AddonRepository::class.java).retrieveAddon(addonId = addonId)
        val result = fromResponse(
            planResponse,
            CBErrorDetail::class.java
        )
        return try {
            Success(result.getData().addon)
        } catch (ex: CBException) {
            Failure(ex)
        }
    }

}