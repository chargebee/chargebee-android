package com.chargebee.android.repository

import com.chargebee.android.Chargebee
import com.chargebee.android.network.CBReceiptResponse
import retrofit2.Response
import retrofit2.http.*

interface ReceiptRepository {

    @FormUrlEncoded
    @POST("{version}/in_app_subscriptions/{sdkKey}/process_purchase_command/")
    suspend fun validateReceipt(
        @Header("Authorization") token: String = Chargebee.encodedApiKey,
        @Header("platform") platform: String = Chargebee.platform,
        @Header("sdkVersion") sdkVersion: String = Chargebee.sdkVersion,
        @Path("version") version: String = Chargebee.version,
        @Path("sdkKey") sdkKey: String = Chargebee.sdkKey,
        @FieldMap data: Map<String, String>): Response<CBReceiptResponse?>
}