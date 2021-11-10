package com.chargebee.android

import android.util.Log
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.network.Auth
import com.chargebee.android.network.CBAuthResponse
import com.chargebee.android.network.CBAuthentication
import com.chargebee.android.resources.CatalogVersion
import okhttp3.Credentials

object Chargebee {
    var site: String = ""
    var publishableApiKey: String = ""
    var encodedApiKey: String = ""
    var sdkKey: String = ""
    var baseUrl: String = ""
    var allowErrorLogging: Boolean = true
    var version: String = CatalogVersion.Unknown.value
    const val applicationId: String = "com.chargebee.sdk"
    const val channel: String = "app_store"
    const val appName: String = "Chargebee"

    fun configure(site: String, publishableApiKey: String, allowErrorLogging: Boolean = true, sdkKey: String ) {
        this.publishableApiKey = publishableApiKey
        this.site = site
        this.encodedApiKey = Credentials.basic(publishableApiKey, "")
        this.baseUrl = "https://${site}.chargebee.com/api/"
        this.allowErrorLogging = allowErrorLogging
        this.sdkKey = sdkKey
        val auth = Auth(sdkKey,applicationId,appName, channel)

        CBAuthentication.authenticate(auth) {
            when(it){
                is ChargebeeResult.Success ->{
                    Log.i(javaClass.simpleName, " Response :${it.data}")
                    val response = it.data as CBAuthResponse
                     this.version = response.in_app_detail.product_catalog_version
                }
                is ChargebeeResult.Error ->{
                    Log.i(javaClass.simpleName, "Exception from server :${it.exp.message}")
                }
            }
        }
    }
}