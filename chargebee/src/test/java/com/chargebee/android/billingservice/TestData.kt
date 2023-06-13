package com.chargebee.android.billingservice

import com.chargebee.android.ErrorDetail
import com.chargebee.android.exceptions.CBException
import com.chargebee.android.models.PurchaseTransaction
import com.chargebee.android.network.CBReceiptResponse
import com.chargebee.android.network.ReceiptDetail

object TestData {
    private val list = ArrayList<String>()
    private val storeTransactions = arrayListOf<PurchaseTransaction>()
    internal val response =
        CBReceiptResponse(ReceiptDetail("subscriptionId", "customerId", "planId"))
    internal val error = CBException(
        ErrorDetail(
            message = "The Token data sent is not correct or Google service is temporarily down",
            httpStatusCode = 400
        )
    )

    fun getTransaction(isTestingSuccess: Boolean): ArrayList<PurchaseTransaction> {
        list.add("chargebee.pro.android")
        storeTransactions.clear()
        val result = if (isTestingSuccess)
            PurchaseTransaction(
                productId = list.toList(),
                purchaseTime = 1682666112774,
                purchaseToken = "fajeooclbamgohgapjeehghm.AO-J1OzxVvoEx7y53c9DsypEKwgcfGw2OrisyQsQ-MG6KiXfJ97nT33Yd5VpbQYxd225QnTAEVdPuLP4YSvZE6LBhsv1rzSlizuBxBTjBWghWguSBBtgp2g",
                productType = "subs"
            )
        else
            PurchaseTransaction(
                productId = list.toList(),
                purchaseTime = 1682666112774,
                purchaseToken = "test data",
                productType = "subs"
            )
        storeTransactions.add(result)
        return storeTransactions
    }
}