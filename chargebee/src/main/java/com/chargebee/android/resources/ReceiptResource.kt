package com.chargebee.android.resources

import android.util.Log
import com.chargebee.android.Chargebee
import com.chargebee.android.ErrorDetail
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.network.*
import com.chargebee.android.network.CBReceiptRequestBody
import com.chargebee.android.repository.ReceiptRepository
import com.chargebee.android.responseFromServer

internal class ReceiptResource : BaseResource(baseUrl = Chargebee.baseUrl){

    internal suspend fun validateReceipt(params: Params): ChargebeeResult<Any> {
        val paramDetail = CBReceiptRequestBody.fromCBReceiptReqBody(params)
        val response = apiClient.create(ReceiptRepository::class.java)
            .validateReceipt(data = paramDetail.toCBReceiptReqBody())

        Log.i(javaClass.simpleName, " validateReceipt Response :$response")
        return responseFromServer(
            response,
            ErrorDetail::class.java
        )
    }

}