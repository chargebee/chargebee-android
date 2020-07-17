package com.chargebee.android.exceptions

import com.chargebee.android.ErrorDetail

class PaymentException(error: ErrorDetail) : CBException(error) {

}