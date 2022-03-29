package com.chargebee.example.items

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.Observer
import com.chargebee.android.ErrorDetail
import com.chargebee.example.BaseActivity
import com.chargebee.example.R
import com.google.gson.Gson

class ItemActivity: BaseActivity() {

    var viewModel: ItemsViewModel? = null

    private var itemIdInput: EditText? = null
    private var itemButton: Button? = null

    private var itemName: TextView? = null
    private var itemStatus: TextView? = null
    private var errorText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)

        itemIdInput = findViewById(R.id.itemIdInput)
        itemButton = findViewById(R.id.itemButton)
        itemName = findViewById(R.id.itemName)
        itemStatus = findViewById(R.id.itemStatus)
        errorText = findViewById(R.id.errorText)

        this.viewModel = ItemsViewModel()

        this.itemButton?.setOnClickListener(View.OnClickListener { view: View? ->
            showProgressDialog()
            viewModel!!.retrieveItem(itemIdInput?.getText().toString())
        })

        viewModel?.mItemResult?.observe(this, Observer {
            hideProgressDialog()
            if (it != null) {
                itemName?.setText(it.name)
                itemStatus?.setText(it.status)
            }
        })

        viewModel?.mItemsError?.observe(this, Observer {
            hideProgressDialog()
            if (it != null) {
                errorText?.setText(
                    Gson().fromJson<ErrorDetail>(
                        it,
                        ErrorDetail::class.java
                    ).message
                )
            }
        })
    }
}