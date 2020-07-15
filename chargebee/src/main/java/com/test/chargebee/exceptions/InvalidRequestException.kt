package com.test.chargebee.exceptions

import com.test.chargebee.CBErrorDetail
import com.test.chargebee.CBException

class InvalidRequestException(error: CBErrorDetail) : CBException(error) {

}