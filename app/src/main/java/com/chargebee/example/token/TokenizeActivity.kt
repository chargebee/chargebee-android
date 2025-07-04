package com.chargebee.example.token

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.Observer
import com.chargebee.android.models.Card
import com.chargebee.android.models.PaymentDetail
import com.chargebee.android.models.PaymentMethodType
import com.chargebee.example.BaseActivity
import com.chargebee.example.R

class TokenizeActivity : BaseActivity() {
    private lateinit var viewModel: TokenViewModel
    private lateinit var cardNumber: EditText
    private lateinit var expiryMonth: EditText
    private lateinit var expiryYear: EditText
    private lateinit var cvc: EditText
    private lateinit var result: TextView
    private lateinit var tokenizeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tokenize)

        this.tokenizeButton = findViewById(R.id.tokenize)
        this.cardNumber = findViewById(R.id.number)
        this.expiryMonth = findViewById(R.id.expiryMonth)
        this.expiryYear = findViewById(R.id.expiryYear)
        this.cvc = findViewById(R.id.cvc)
        this.result = findViewById(R.id.token_txt)
        this.viewModel = TokenViewModel()
        this.viewModel.result.observe(this, Observer {
            hideProgressDialog()
            Log.d("calling ", "value")
            Log.d("calling ", it.toString())
            Log.d("calling ", "it.toString()")
            result.text = it.toString()
        })

        this.tokenizeButton.setOnClickListener {
            showProgressDialog()
            this.clearFields()
            val card = Card(cardNumber.text.toString(), expiryMonth.text.toString(), expiryYear.text.toString(), cvc.text.toString())
            val paymentDetail = PaymentDetail("USD", PaymentMethodType.CARD, card)
            this.viewModel.create(paymentDetail)
        }
    }

    private fun clearFields() {
        this.result.text = ""
    }
}