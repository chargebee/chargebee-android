package com.chargebee.android.exceptions

import com.chargebee.android.CBErrorDetail

class OperationFailedException(error: CBErrorDetail) : CBException(error) {

}