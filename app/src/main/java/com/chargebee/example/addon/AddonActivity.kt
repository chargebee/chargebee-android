package com.chargebee.example.addon

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.Observer
import com.chargebee.example.BaseActivity
import com.chargebee.example.R

class AddonActivity : BaseActivity() {

    private lateinit var viewModel: AddonViewModel

    private lateinit var addonIdInput: EditText
    private lateinit var addonButton: Button

    private lateinit var addonName: TextView
    private lateinit var addonDescription: TextView
    private lateinit var addonError: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addon)
        addonIdInput = findViewById(R.id.addonIdInput)
        addonButton = findViewById(R.id.addonButton)
        addonName = findViewById(R.id.addonName)
        addonDescription = findViewById(R.id.addonDescription)
        addonError = findViewById(R.id.errorText)

        this.viewModel = AddonViewModel()
        this.viewModel.addonResult.observe(this, Observer {
            hideProgressDialog()
            addonName.text = it.name
            addonDescription.text = it.description
        })

        this.viewModel.addonError.observe(this, Observer {
            hideProgressDialog()
            addonError.text = it
        })

        this.addonButton.setOnClickListener {
            showProgressDialog()
            this.clearFields()
            this.viewModel.retrieveAddon(addonIdInput.text.toString())
        }
    }

    private fun clearFields() {
        this.addonName.text = ""
        this.addonDescription.text = ""
        this.addonError.text = ""
    }
}