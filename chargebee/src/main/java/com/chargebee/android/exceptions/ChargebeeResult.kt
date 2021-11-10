package com.chargebee.android.exceptions

sealed class ChargebeeResult<out S> {
    data class Success<out T>(val data: T) : ChargebeeResult<T>()
    data class Error(val exp: CBException): ChargebeeResult<Nothing>()
}