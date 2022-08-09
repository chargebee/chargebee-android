package com.chargebee.example.plan

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Observer
import com.chargebee.example.BaseActivity
import com.chargebee.example.R
import com.chargebee.example.billing.BillingActivity
import com.chargebee.example.billing.BillingViewModel
import com.chargebee.example.util.Constants
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class UpgradePlanActivity : BaseActivity() {

    private var mBillingViewModel : BillingViewModel? = null
    private lateinit var mProductIdInput: TextInputEditText
    private lateinit var mPurchaseTokenInput: TextInputEditText
    private lateinit var mBuyProductButton: Button
    private lateinit var mPriceChangeButton: Button
    private lateinit var mPurchaseTokenInputLayout: TextInputLayout
    private lateinit var mTvPriceChange: TextView
    lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upgradeplan)
        context = this
        mBillingViewModel = BillingViewModel()
        mProductIdInput = findViewById(R.id.productIdInput)
        mPurchaseTokenInput = findViewById(R.id.purchaseTokenInput)
        mBuyProductButton = findViewById(R.id.buyBtn)
        mPriceChangeButton = findViewById(R.id.btnpricechange)
        mPurchaseTokenInputLayout = findViewById(R.id.purchaseTokenInputLayout)
        mTvPriceChange = findViewById(R.id.textView)

        val priceChange = intent.getStringExtra(Constants.PRICE_KEY)
        if(priceChange !=null){
            mPurchaseTokenInputLayout.visibility = View.GONE
            mBuyProductButton.visibility = View.GONE
            mTvPriceChange.text = "Plan Price Change"

        }else{
            mPurchaseTokenInputLayout.visibility = View.VISIBLE
            mPriceChangeButton.visibility = View.GONE
            mTvPriceChange.text = "Plan Upgrade/Downgrade"

        }

        this.mBillingViewModel!!.updateProductPurchaseResult.observeForever{
            hideProgressDialog()
            if (!TextUtils.isEmpty(it))
            alertSuccess(it)
        }

        this.mBillingViewModel!!.priceChangeLiveData.observeForever{
            hideProgressDialog()
            if (!TextUtils.isEmpty(it))
                alertSuccess(it)
        }
        this.mBillingViewModel!!.cbException.observeForever{
            if (!TextUtils.isEmpty(it))
                alertSuccess(it)
        }

        mBuyProductButton.setOnClickListener{
            var productId = mProductIdInput.text.toString()
            val purchaseToken = mPurchaseTokenInput.text.toString()

            val array = arrayListOf<String>(productId)
            this.mBillingViewModel!!.updatePurchase(array, purchaseToken, context)
        }

        mPriceChangeButton.setOnClickListener{
            var productId = mProductIdInput.text.toString()
            val productIdList =  ArrayList<String>()
            productIdList.add(productId)
            this.mBillingViewModel!!.priceChangeUpdate(productIdList, context)

        }

    }
}