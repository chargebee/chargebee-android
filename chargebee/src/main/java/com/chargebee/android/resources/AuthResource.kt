package com.chargebee.android.resources

import android.util.Log
import com.chargebee.android.Chargebee
import com.chargebee.android.ErrorDetail
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.network.Auth
import com.chargebee.android.network.CBAuthenticationBody
import com.chargebee.android.repository.AuthRepository
import com.chargebee.android.responseFromServer

internal class AuthResource : BaseResource(Chargebee.baseUrl) {

    internal suspend fun authenticate(auth: Auth): ChargebeeResult<Any> {
        val authDetail = CBAuthenticationBody.fromCBAuthBody(auth)
        val response = apiClient.create(AuthRepository::class.java)
            .authenticateClient(data = authDetail.toFormBody())

        Log.i(javaClass.simpleName, " Response :$response")
        return responseFromServer(
            response
        )
    }

}
