package com.test.chargebee.exceptions

import com.test.chargebee.CBErrorDetail

class OperationFailedException(error: CBErrorDetail) : CBException(error) {

}