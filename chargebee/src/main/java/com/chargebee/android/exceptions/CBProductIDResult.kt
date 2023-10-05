package com.chargebee.android.exceptions

import java.util.ArrayList

sealed class CBProductIDResult<out S> {
    data class ProductIds(val IDs: MutableSet<String>) : CBProductIDResult<MutableSet<String>>()
    data class Error(val exp: CBException): CBProductIDResult<Nothing>()
}
