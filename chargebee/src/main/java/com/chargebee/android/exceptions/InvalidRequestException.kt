package com.chargebee.android.exceptions

import com.chargebee.android.CBErrorDetail

class InvalidRequestException(error: CBErrorDetail) : CBException(error) {

}