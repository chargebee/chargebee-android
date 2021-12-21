package com.chargebee.example.items

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.models.ItemWrapper
import com.chargebee.android.models.Items
import com.chargebee.android.models.ItemsWrapper

class ItemsViewModel : ViewModel() {

    var mItemsResult: MutableLiveData<ArrayList<String>?> = MutableLiveData()
    var mItemResult: MutableLiveData<Items?> = MutableLiveData()
    var mItemsError: MutableLiveData<String?> = MutableLiveData()
    var mItemsList = ArrayList<String>()

    fun retrieveAllItems(queryParam: Array<String>) {
        Items.retrieveAllItems(queryParam) {
            when (it) {
                is ChargebeeResult.Success -> {
                    Log.i(javaClass.simpleName, "list items :  ${it.data}")
                    parseResponse(it.data as ItemsWrapper)
                    mItemsResult.postValue(mItemsList)
                }
                is ChargebeeResult.Error -> {
                    Log.d(javaClass.simpleName, "exception :  ${it.exp.message}")
                    mItemsError.postValue(it.exp.message)
                }
            }
        }
    }

    fun retrieveItem(itemId: String) {
        Items.retrieveItem(itemId) {
            when (it) {
                is ChargebeeResult.Success -> {
                    Log.i(javaClass.simpleName, "list items :  ${it.data}")
                    mItemResult.postValue((it.data as ItemWrapper?)?.item)
                }
                is ChargebeeResult.Error -> {
                    Log.d(javaClass.simpleName, "exception :  ${it.exp.message}")
                    mItemsError.postValue(it.exp.message)
                }
            }
        }
    }

    private fun parseResponse(items: ItemsWrapper){
        mItemsList.clear()
        for (item in  items.list){
            mItemsList.add(item.item.id)
        }

    }
}