package com.test.chargebee.exceptions

import com.test.chargebee.CBError
import com.test.chargebee.CBException

class InvalidRequestException(error: CBError) : CBException(error) {

}