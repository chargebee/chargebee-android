package com.chargebee.android.models

data class Items(val id: String, val name: String,val status: String, val channel: String)

data class ItemsWrapper(val list: ArrayList<ItemWrapper>)

data class ItemWrapper(val item: Items)
