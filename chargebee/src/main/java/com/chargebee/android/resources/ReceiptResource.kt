package com.chargebee.android.resources

import android.text.TextUtils
import android.util.Log
import com.chargebee.android.Chargebee
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.network.*
import com.chargebee.android.network.CBReceiptRequestBody
import com.chargebee.android.repository.ReceiptRepository
import com.chargebee.android.responseFromServer

internal class ReceiptResource : BaseResource(baseUrl = Chargebee.baseUrl){

    internal suspend fun validateReceipt(params: Params): ChargebeeResult<Any> {
        val paramDetail = CBReceiptRequestBody.fromCBReceiptReqBody(params)
        val dataMap = if (params.customer != null && !(TextUtils.isEmpty(params.customer.id))) {
            paramDetail.toCBReceiptReqCustomerBody()
        } else{
            paramDetail.toMap()
        }
        val response = apiClient.create(ReceiptRepository::class.java)
            .validateReceipt(data = dataMap)

        Log.i(javaClass.simpleName, " validateReceipt Response :$response")
        return responseFromServer(
            response
        )
    }

    internal suspend fun validateReceiptForNonSubscription(params: Params): ChargebeeResult<Any> {
        val paramDetail = CBReceiptRequestBody.fromCBReceiptReqBody(params)
        val dataMap = if (params.customer != null && !(TextUtils.isEmpty(params.customer.id))) {
            paramDetail.toCBNonSubscriptionReqCustomerBody()
        } else{
            paramDetail.toMapNonSubscription()
        }
        val response = apiClient.create(ReceiptRepository::class.java)
            .validateReceiptForNonSubscription(data = dataMap)

        Log.i(javaClass.simpleName, " validateReceiptForNonSubscription Response :$response")
        return responseFromServer(
            response
        )
    }
}