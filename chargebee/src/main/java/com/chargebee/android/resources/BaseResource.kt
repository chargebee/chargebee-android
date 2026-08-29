package com.chargebee.android.resources

import com.chargebee.android.network.MobileTokenAuthenticator
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal open class BaseResource(baseUrl: String) {
    var apiClient: Retrofit

    init {

        val gson: Gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()

        // Refreshes and retries once when a mobile-token request is rejected with a 401.
        val httpClient = OkHttpClient.Builder()
            .authenticator(MobileTokenAuthenticator())
            .build()

        apiClient = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}