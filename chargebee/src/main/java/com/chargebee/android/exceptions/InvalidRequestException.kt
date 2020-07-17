package com.chargebee.android.exceptions

import com.chargebee.android.ErrorDetail

class InvalidRequestException(error: ErrorDetail) : CBException(error) {

}