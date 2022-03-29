package com.chargebee.android.network

internal class CBAuthenticationBody(  val key: String,
                                      val appId: String,
                                      val appName: String,
                                      val channel: String) {
    companion object {
        fun fromCBAuthBody(auth: Auth): CBAuthenticationBody {
            return CBAuthenticationBody(
                auth.sKey,
                auth.applicationId,
                auth.appName,
                auth.channel
            )
        }
    }

    fun toFormBody(): Map<String, String> {
        return mapOf(
            "shared_secret" to this.key,
            "app_id" to this.appId,
            "app_name" to this.appName,
            "channel" to this.channel
        )
    }
}

data class Auth(
    val sKey: String,
    val applicationId: String,
    val appName: String,
    val channel: String
)