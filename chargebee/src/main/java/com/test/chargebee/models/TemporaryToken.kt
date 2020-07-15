package com.test.chargebee.models

internal data class TemporaryToken(val id: String)

internal data class TokenWrapper(val token: TemporaryToken)