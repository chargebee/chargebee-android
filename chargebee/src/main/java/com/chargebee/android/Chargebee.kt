package com.chargebee.android

import okhttp3.Credentials

object Chargebee {
    var site: String = ""
    var publishableApiKey: String = ""
    var encodedApiKey: String = ""
    var baseUrl: String = ""
    var allowErrorLogging: Boolean = true

    fun configure(site: String, publishableApiKey: String, allowErrorLogging: Boolean = true) {
        this.publishableApiKey = publishableApiKey
        this.site = site
        this.encodedApiKey = Credentials.basic(publishableApiKey, "")
        this.baseUrl = "https://${site}.chargebee.com/api/"
        this.allowErrorLogging = allowErrorLogging
    }
}