package com.test.chargebee.exceptions

import com.test.chargebee.CBErrorDetail

class InvalidRequestException(error: CBErrorDetail) : CBException(error) {

}