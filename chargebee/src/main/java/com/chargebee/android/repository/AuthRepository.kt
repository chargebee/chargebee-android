package com.chargebee.android.repository

import com.chargebee.android.Chargebee
import com.chargebee.android.network.CBAuthResponse
import retrofit2.Response
import retrofit2.http.*

interface AuthRepository {
    @FormUrlEncoded
    @POST("{version}/in_app_details/{sdkKey}/verify_app_detail/")
    suspend fun authenticateClient(
        @Header("Authorization") token: String = Chargebee.encodedApiKey,
        @Header("platform") platform: String = Chargebee.platform,
        @Header("sdkVersion") sdkVersion: String = Chargebee.sdkVersion,
        @Path("version") version: String = Chargebee.version,
        @Path("sdkKey") sdkKey: String = Chargebee.sdkKey,
        @FieldMap data: Map<String, String>): Response<CBAuthResponse?>
}