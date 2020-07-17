package com.chargebee.android.exceptions

import com.chargebee.android.ErrorDetail

class OperationFailedException(error: ErrorDetail) : CBException(error) {

}