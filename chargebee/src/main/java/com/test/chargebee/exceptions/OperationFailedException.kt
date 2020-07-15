package com.test.chargebee.exceptions

import com.test.chargebee.CBErrorDetail
import com.test.chargebee.CBException

class OperationFailedException(error: CBErrorDetail) : CBException(error) {

}