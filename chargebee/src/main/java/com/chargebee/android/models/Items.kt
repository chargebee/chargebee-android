package com.chargebee.android.models

import android.text.TextUtils
import com.chargebee.android.ErrorDetail
import com.chargebee.android.exceptions.CBException
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.exceptions.InvalidRequestException
import com.chargebee.android.exceptions.OperationFailedException
import com.chargebee.android.loggers.CBLogger
import com.chargebee.android.resources.ItemsResource

data class Items(val id: String, val name: String,val status: String, val channel: String){
    companion object{
        @JvmStatic
        @Throws(InvalidRequestException::class, OperationFailedException::class)
        fun retrieveAllItems(params: Array<String>, completion : (ChargebeeResult<Any>) -> Unit) {
            val logger = CBLogger(name = "items", action = "getAllItems")
            if (params.isNullOrEmpty())
                completion(ChargebeeResult.Error(
                    exp = CBException(
                        error = ErrorDetail(message = "Query param is empty", apiErrorCode = "400")
                    )
                ))
            else
               ResultHandler.safeExecuter({ ItemsResource().retrieveAllItems(params) }, completion, logger)
        }
        @JvmStatic
        @Throws(InvalidRequestException::class, OperationFailedException::class)
        fun retrieveItem(itemId: String, completion : (ChargebeeResult<Any>) -> Unit) {
            val logger = CBLogger(name = "item", action = "getItem")
            if (TextUtils.isEmpty(itemId))
                completion(ChargebeeResult.Error(
                    exp = CBException(
                        error = ErrorDetail(message = "Item ID is empty", apiErrorCode = "400")
                    )
                ))
            else
               ResultHandler.safeExecuter({ ItemsResource().retrieveItem(itemId) }, completion, logger)
        }
    }
}

data class ItemsWrapper(val list: ArrayList<ItemWrapper>)

data class ItemWrapper(val item: Items)
