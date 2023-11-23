package com.chargebee.example

import android.app.Application
import android.content.Context
import com.chargebee.android.Chargebee
import android.content.SharedPreferences
import android.util.Log
import com.chargebee.android.billingservice.CBCallback
import com.chargebee.android.billingservice.CBPurchase
import com.chargebee.android.billingservice.OneTimeProductType
import com.chargebee.android.billingservice.ProductType
import com.chargebee.android.exceptions.CBException
import com.chargebee.android.models.CBProduct
import com.chargebee.android.models.NonSubscription
import com.chargebee.android.network.CBCustomer
import com.chargebee.android.network.ReceiptDetail
import com.chargebee.example.util.NetworkUtil

class ExampleApplication : Application(), NetworkUtil.NetworkListener {
    private lateinit var networkUtil: NetworkUtil
    private var sharedPreference: SharedPreferences? = null
    lateinit var mContext: Context
    private val customer = CBCustomer(
        id = "sync_receipt_android",
        firstName = "Test",
        lastName = "Purchase",
        email = "testreceipt@gmail.com"
    )

    override fun onCreate() {
        super.onCreate()
        mContext = this
        networkUtil = NetworkUtil(mContext, this)
        networkUtil.registerCallbackEvents()
        sharedPreference = mContext.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
    }

    override fun onNetworkConnectionAvailable() {
        val productId = sharedPreference?.getString("productId", "")
        if (productId?.isNotEmpty() == true) {
            val productList = ArrayList<String>()
            productList.add(productId)
            retrieveProducts(productList)
        }
    }

    override fun onNetworkConnectionLost() {
        Log.e(javaClass.simpleName, "Network connectivity not available")
    }

    private fun retrieveProducts(productIdList: ArrayList<String>) {
        CBPurchase.retrieveProducts(
            this,
            productIdList,
            object : CBCallback.ListProductsCallback<ArrayList<CBProduct>> {
                override fun onSuccess(productIDs: ArrayList<CBProduct>) {
                    if (productIDs.first().type == ProductType.SUBS)
                        validateReceipt(mContext, productIDs.first())
                    else
                        validateNonSubscriptionReceipt(mContext, productIDs.first())
                }

                override fun onError(error: CBException) {
                    Log.e(javaClass.simpleName, "Exception:  $error")
                }
            })
    }

    private fun validateReceipt(context: Context, product: CBProduct) {

        CBPurchase.validateReceipt(
            context = context,
            product = product,
            customer = customer,
            completionCallback = object : CBCallback.PurchaseCallback<String> {
                override fun onSuccess(result: ReceiptDetail, status: Boolean) {
                    // Clear the local cache once receipt validation success
                    val editor = sharedPreference?.edit()
                    editor?.clear()?.apply()
                    Log.i(javaClass.simpleName, "Subscription ID:  ${result.subscription_id}")
                    Log.i(javaClass.simpleName, "Plan ID:  ${result.plan_id}")
                    Log.i(javaClass.simpleName, "Customer ID:  ${result.customer_id}")
                    Log.i(javaClass.simpleName, "Status:  $status")
                }

                override fun onError(error: CBException) {
                    Log.e(javaClass.simpleName, "Exception :$error")
                }
            })
    }

    private fun validateNonSubscriptionReceipt(context: Context, product: CBProduct) {
        CBPurchase.validateReceiptForNonSubscriptions(
            context = context,
            product = product,
            customer = customer,
            productType = OneTimeProductType.CONSUMABLE,
            completionCallback = object : CBCallback.OneTimePurchaseCallback {
                override fun onSuccess(result: NonSubscription, status: Boolean) {
                    // Clear the local cache once receipt validation success
                    val editor = sharedPreference?.edit()
                    editor?.clear()?.apply()
                    Log.i(javaClass.simpleName, "Subscription ID:  ${result.invoiceId}")
                    Log.i(javaClass.simpleName, "Plan ID:  ${result.chargeId}")
                    Log.i(javaClass.simpleName, "Customer ID:  ${result.customerId}")
                    Log.i(javaClass.simpleName, "Status:  $status")
                }

                override fun onError(error: CBException) {
                    Log.e(javaClass.simpleName, "Exception :$error")
                }
            })
    }
}