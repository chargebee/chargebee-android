package com.test.chargebee.exceptions

import com.test.chargebee.CBErrorDetail

open class CBException(val error: CBErrorDetail) : RuntimeException()