package com.chargebee.android.models

internal data class TemporaryToken(val id: String)

internal data class TokenWrapper(val token: TemporaryToken)