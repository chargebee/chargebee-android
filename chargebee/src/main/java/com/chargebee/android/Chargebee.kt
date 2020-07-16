package com.chargebee.android

import okhttp3.Credentials

object Chargebee {
    var site: String = ""
    var publishableApiKey: String = ""
    var encodedApiKey: String = ""
    var baseUrl: String = ""

    fun configure(site: String, publishableApiKey: String) {
        this.publishableApiKey = publishableApiKey
        this.site = site
        this.encodedApiKey = Credentials.basic(publishableApiKey, "")
        this.baseUrl = "https://${site}.chargebee.com/api/"
    }
}