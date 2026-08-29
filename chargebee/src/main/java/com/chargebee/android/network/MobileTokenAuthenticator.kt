package com.chargebee.android.network

import com.chargebee.android.Chargebee
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Handles expired/revoked mobile tokens: when a request authenticated with a mobile token comes back
 * 401, a fresh token is fetched from the provider and the request is retried once
 * with it. Requests that do not use a mobile token (no provider configured) are left untouched.
 */
internal class MobileTokenAuthenticator : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (Chargebee.tokenProvider == null) {
            return null
        }
        // Retry only once: a non-null priorResponse means we already refreshed and retried.
        if (response.priorResponse() != null) {
            return null
        }
        val refreshedHeader = refreshTokenBlocking() ?: return null
        return response.request().newBuilder()
            .header("Authorization", refreshedHeader)
            .build()
    }

    // The token provider is asynchronous while the authenticator runs synchronously on OkHttp's
    // background thread, so we bridge the callback with a short-lived latch.
    private fun refreshTokenBlocking(): String? {
        val latch = CountDownLatch(1)
        var refreshedHeader: String? = null
        Chargebee.refreshMobileToken { success ->
            if (success) {
                refreshedHeader = Chargebee.encodedApiKey
            }
            latch.countDown()
        }
        latch.await(TOKEN_REFRESH_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        return refreshedHeader
    }

    private companion object {
        private const val TOKEN_REFRESH_TIMEOUT_SECONDS = 30L
    }
}
