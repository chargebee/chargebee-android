package com.chargebee.android

import android.util.Log
import com.android.billingclient.BuildConfig
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
    var version: String = CatalogVersion.V2.value
    var applicationId: String = ""
    const val channel: String = "play_store"
    var appName: String = "Chargebee"
    const val platform: String = "Android"
    const val sdkVersion: String = BuildConfig.VERSION_NAME

    fun configure(site: String, publishableApiKey: String, allowErrorLogging: Boolean = true, sdkKey: String ) {
        this.publishableApiKey = publishableApiKey
        this.site = site
        this.encodedApiKey = Credentials.basic(publishableApiKey, "")
        this.baseUrl = "https://${site}.chargebee.com/api/"
       //  this.baseUrl = "https://${site}.predev37.in/api/"  //check Plans API
       //  this.baseUrl = "https://${site}.predev51.in/api/"  //Process purchase API
        this.allowErrorLogging = allowErrorLogging
        this.sdkKey = sdkKey
        val auth = Auth(sdkKey,applicationId,appName, channel)

        Log.i(javaClass.simpleName, " encodedApiKey :"+encodedApiKey)

        CBAuthentication.authenticate(auth) {
            when(it){
                is ChargebeeResult.Success ->{
                    Log.i(javaClass.simpleName, " Response :${it.data}")
                    val response = it.data as CBAuthResponse
                    this.version = response.in_app_detail.product_catalog_version
                    this.applicationId = response.in_app_detail.app_id
                    this.appName = response.in_app_detail.app_name
                }
                is ChargebeeResult.Error ->{
                    Log.i(javaClass.simpleName, "Exception from server :${it.exp.message}")
                }
            }
        }
    }
}