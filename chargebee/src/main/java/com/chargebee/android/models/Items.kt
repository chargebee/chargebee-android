package com.chargebee.android.models

data class Items(
    val id: String,
    val name: String,
    val status: String,
    val channel: String,
    val externalName: String,
    val resourceVersion: String,
    val updatedAt: Long,
    val itemFamilyId: String,
    val type: String,
    val isShippable: Boolean,
    val isGiftable: String,
    val enabledForCheckouts: Boolean,
    val enabledInPortal: Boolean,
    val itemApplicability: String,
    val metered: Boolean,
    val `object`: String
)

data class ItemsWrapper(val list: ArrayList<ItemWrapper>)

data class ItemWrapper(val item: Items)
