package com.chargebee.android.exceptions

import com.chargebee.android.CBErrorDetail

class PaymentException(error: CBErrorDetail) : CBException(error) {

}