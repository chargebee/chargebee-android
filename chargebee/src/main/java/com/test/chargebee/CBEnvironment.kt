package com.test.chargebee

object CBEnvironment {
    var site: String = ""
    var apiKey: String = ""

    fun configure(site: String, apiKey: String) {
        this.apiKey = apiKey
        this.site = site
    }
}