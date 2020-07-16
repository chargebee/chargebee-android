package com.chargebee.android

import okhttp3.Credentials

object CBEnvironment {
    var site: String = ""
    var apiKey: String = ""
    var encodedApiKey: String = ""
    var baseUrl: String = ""

    fun configure(site: String, apiKey: String) {
        this.apiKey = apiKey
        this.site = site
        this.encodedApiKey = Credentials.basic(apiKey, "")
        this.baseUrl = "https://${site}.chargebee.com/api/"
    }
}