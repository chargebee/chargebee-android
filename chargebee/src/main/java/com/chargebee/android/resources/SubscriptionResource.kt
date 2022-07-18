package com.chargebee.android.resources

import android.util.Log
import com.chargebee.android.*
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.repository.PurchaseRepository
import com.chargebee.android.responseFromServer

internal class SubscriptionResource : BaseResource(Chargebee.baseUrl) {

    suspend fun retrieveSubscription(subscriptionId: String): ChargebeeResult<Any> {
        val subscriptionResponse = apiClient.create(PurchaseRepository::class.java)
            .retrieveSubscription(subscriptionId = subscriptionId)
        Log.i(javaClass.simpleName, " Response :$subscriptionResponse")
        return responseFromServer(
            subscriptionResponse
        )
    }
    suspend fun retrieveSubscriptions(queryParam: Map<String, String>): ChargebeeResult<Any> {
        var queryParams = HashMap<String,String>()
        val result: (HashMap<String,String>) -> Unit= {
                map: HashMap<String,String> -> queryParams = map
        } //lambda function
        queryParamSanitizer(queryParam,result)

        val subscriptionResponse = apiClient.create(PurchaseRepository::class.java)
            .retrieveSubscriptions(queryParams=queryParams)
        Log.i(javaClass.simpleName, " Response :$subscriptionResponse")
        return responseFromServer(
            subscriptionResponse
        )
    }

    private fun queryParamSanitizer(queryParam: Map<String,String>,result:(HashMap<String,String>) -> Unit){
        val map = HashMap<String,String>()
        if (queryParam.isNotEmpty()) {
            for ((key, value) in queryParam) {
                map[key.replace("\"", "").replace(" ","")] = value.replace("\"", "").replace(" ","")
            }
        }
        result(map)
    }
}