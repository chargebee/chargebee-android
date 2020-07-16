package com.chargebee.android.resources

import com.chargebee.android.*
import com.chargebee.android.exceptions.CBException
import com.chargebee.android.models.Addon
import com.chargebee.android.repository.AddonRepository

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