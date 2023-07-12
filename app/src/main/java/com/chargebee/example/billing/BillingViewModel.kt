package com.chargebee.example.billing

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chargebee.android.Chargebee
import com.chargebee.android.billingservice.*
import com.chargebee.android.exceptions.CBException
import com.chargebee.android.exceptions.CBProductIDResult
import com.chargebee.android.exceptions.ChargebeeResult
import com.chargebee.android.models.*
import com.chargebee.android.network.CBCustomer
import com.chargebee.android.network.ReceiptDetail

class BillingViewModel : ViewModel() {

    private val TAG = "BillingViewModel"
    var productIdsList: MutableLiveData<Array<String>> = MutableLiveData()
    var productPurchaseResult: MutableLiveData<Boolean> = MutableLiveData()
    var cbException: MutableLiveData<CBException?> = MutableLiveData()
    var subscriptionStatus: MutableLiveData<String?> = MutableLiveData()
    var subscriptionList: MutableLiveData<ArrayList<SubscriptionDetailsWrapper>?> = MutableLiveData()
    var error: MutableLiveData<String?> = MutableLiveData()
    var entitlementsResult: MutableLiveData<String?> = MutableLiveData()
    private var subscriptionId: String = ""
    private lateinit var sharedPreference : SharedPreferences
    var restorePurchaseResult: MutableLiveData<List<CBRestoreSubscription?>> = MutableLiveData()

    fun purchaseProduct(context: Context,product: CBProduct, customer: CBCustomer) {
        // Cache the product id in sharedPreferences and retry validating the receipt if in case server is not responding or no internet connection.
        sharedPreference =  context.getSharedPreferences("PREFERENCE_NAME",Context.MODE_PRIVATE)
        CBPurchase.purchaseProduct(product, customer,  object : CBCallback.PurchaseCallback<String>{
            override fun onSuccess(result: ReceiptDetail, status:Boolean) {
                Log.i(TAG, "Subscription ID:  ${result.subscription_id}")
                Log.i(TAG, "Plan ID:  ${result.plan_id}")
                productPurchaseResult.postValue(status)
            }
            override fun onError(error: CBException) {
                try {
                    // Handled server not responding and offline
                    if (error.httpStatusCode!! in 500..599) {
                        storeInLocal(product.productId)
                        validateReceipt(context = context, product = product)
                    } else {
                        cbException.postValue(error)
                    }
                } catch (exp: Exception) {
                    Log.i(TAG, "Exception :${exp.message}")
                }
            }
        })
    }
    fun purchaseProduct(context: Context, product: CBProduct, customerId: String) {
        sharedPreference =  context.getSharedPreferences("PREFERENCE_NAME",Context.MODE_PRIVATE)
        CBPurchase.purchaseProduct(product, customerId,  object : CBCallback.PurchaseCallback<String>{
            override fun onSuccess(result: ReceiptDetail, status:Boolean) {
                Log.i(TAG, "Subscription ID:  ${result.subscription_id}")
                Log.i(TAG, "Plan ID:  ${result.plan_id}")
                productPurchaseResult.postValue(status)
            }
            override fun onError(error: CBException) {
                try {
                    if (error.httpStatusCode!! in 500..599) {
                        storeInLocal(product.productId)
                        validateReceipt(context = context, product = product)
                    } else {
                        cbException.postValue(error)
                    }
                } catch (exp: Exception) {
                    Log.i(TAG, "Exception :${exp.message}")
                }
            }
        })
    }

    private fun validateReceipt(context: Context, product: CBProduct) {
        val customer = CBCustomer(
            id = "sync_receipt_android",
            firstName = "Test",
            lastName = "Purchase",
            email = "testreceipt@gmail.com"
        )
        CBPurchase.validateReceipt(
            context = context,
            product = product,
            customer = customer,
            completionCallback = object : CBCallback.PurchaseCallback<String> {
                override fun onSuccess(result: ReceiptDetail, status: Boolean) {
                    Log.i(TAG, "Subscription ID:  ${result.subscription_id}")
                    Log.i(TAG, "Plan ID:  ${result.plan_id}")
                    // Clear the local cache once receipt validation success
                    val editor = sharedPreference.edit()
                    editor.clear().apply()
                    productPurchaseResult.postValue(status)
                }

                override fun onError(error: CBException) {
                    try {
                        cbException.postValue(error)
                    } catch (exp: Exception) {
                        Log.i(TAG, "Exception :${exp.message}")
                    }
                }
            })
    }

    fun retrieveProductIdentifers(queryParam: Array<String>){
        CBPurchase.retrieveProductIdentifers(queryParam) {
            when (it) {
                is CBProductIDResult.ProductIds -> {
                    Log.i(TAG, "List of Product Identifiers:  $it")
                    val array = it.IDs.toTypedArray()
                    productIdsList.postValue(array)
                }
                is CBProductIDResult.Error -> {
                    Log.e(javaClass.simpleName, " ${it.exp.message}")
                    cbException.postValue(it.exp)
                }
            }
        }
    }
    fun retrieveSubscription(subscriptionId: String) {
        Chargebee.retrieveSubscription(subscriptionId) {
            when(it){
                is ChargebeeResult.Success -> {
                    Log.i(
                        TAG,
                        "subscription status:  ${(it.data as SubscriptionDetailsWrapper).cb_subscription.status} ,activated_at : ${(it.data as SubscriptionDetailsWrapper).cb_subscription.activated_at}" +
                                " subscription id : ${(it.data as SubscriptionDetailsWrapper).cb_subscription.subscription_id}" +
                                " customer_id : ${(it.data as SubscriptionDetailsWrapper).cb_subscription.customer_id}" +
                                " current_term_start : ${(it.data as SubscriptionDetailsWrapper).cb_subscription.current_term_start} " +
                                " current_term_end : ${(it.data as SubscriptionDetailsWrapper).cb_subscription.current_term_end}"
                    )

                    subscriptionStatus.postValue((it.data as SubscriptionDetailsWrapper).cb_subscription.status)
                }
                is ChargebeeResult.Error ->{
                    Log.e(TAG, "Exception from server- retrieveSubscription() :  ${it.exp.message}")
                    cbException.postValue(it.exp)
                }
            }
        }
    }
    fun retrieveSubscriptionsByCustomerId(queryParams: Map<String, String>) {
        Chargebee.retrieveSubscriptions(queryParams) {
            when(it){
                is ChargebeeResult.Success -> {
                    subscriptionList.postValue((it.data as CBSubscription).list)
                }
                is ChargebeeResult.Error ->{
                    Log.e(TAG, "Exception from server- retrieveSubscriptions() :  ${it.exp.message}")
                    cbException.postValue(it.exp)
                }
            }
        }
    }

    fun retrieveEntitlements(subscriptionId: String) {
        Chargebee.retrieveEntitlements(subscriptionId) {
            when(it){
                is ChargebeeResult.Success -> {
                    Log.i(
                        TAG,
                        "entitlements response:  ${(it.data)}"
                    )
                    entitlementsResult.postValue("${(it.data as CBEntitlements).list.size}")

                }
                is ChargebeeResult.Error ->{
                    Log.e(TAG, "Exception from server- retrieveEntitlements() :  ${it.exp.message}")
                    cbException.postValue(it.exp)
                }
            }
        }
    }
    private fun storeInLocal(productId: String){
        val editor = sharedPreference.edit()
        editor.putString("productId", productId)
        editor.apply()
    }

    fun purchaseNonSubscriptionProduct(context: Context,product: CBProduct, customer: CBCustomer, productType: OneTimeProductType) {
        // Cache the product id in sharedPreferences and retry validating the receipt if in case server is not responding or no internet connection.
        sharedPreference =  context.getSharedPreferences("PREFERENCE_NAME",Context.MODE_PRIVATE)

        CBPurchase.purchaseNonSubscriptionProduct(
            product, customer,
            productType, object : CBCallback.OneTimePurchaseCallback {
            override fun onSuccess(result: NonSubscription, status:Boolean) {
                Log.i(TAG, "invoice ID:  ${result.invoiceId}")
                Log.i(TAG, "charge ID:  ${result.chargeId}")
                productPurchaseResult.postValue(status)
            }
            override fun onError(error: CBException) {
                try {
                    // Handled server not responding and offline
                    if (error.httpStatusCode!! in 500..599) {
                        storeInLocal(product.productId)
                        validateNonSubscriptionReceipt(context = context, product = product, productType = productType)
                    } else {
                        cbException.postValue(error)
                    }
                } catch (exp: Exception) {
                    Log.i(TAG, "Exception :${exp.message}")
                }
            }
        })
    }

    private fun validateNonSubscriptionReceipt(context: Context, product: CBProduct, productType: OneTimeProductType) {
        val customer = CBCustomer(
            id = "sync_receipt_android",
            firstName = "Test",
            lastName = "Purchase",
            email = "testreceipt@gmail.com"
        )
        CBPurchase.validateReceiptForNonSubscriptions(
            context = context,
            product = product,
            customer = customer,
            productType = productType,
            completionCallback = object : CBCallback.OneTimePurchaseCallback {
                override fun onSuccess(result: NonSubscription, status: Boolean) {
                    Log.i(TAG, "Invoice ID:  ${result.invoiceId}")
                    Log.i(TAG, "Plan ID:  ${result.chargeId}")
                    // Clear the local cache once receipt validation success
                    val editor = sharedPreference.edit()
                    editor.clear().apply()
                    productPurchaseResult.postValue(status)
                }

                override fun onError(error: CBException) {
                    try {
                        cbException.postValue(error)
                    } catch (exp: Exception) {
                        Log.i(TAG, "Exception :${exp.message}")
                    }
                }
            })
    }

    fun restorePurchases(context: Context, includeInActivePurchases: Boolean = false) {
        CBPurchase.restorePurchases(
            context = context, includeInActivePurchases = includeInActivePurchases,
            completionCallback = object : CBCallback.RestorePurchaseCallback {
                override fun onSuccess(result: List<CBRestoreSubscription>) {
                    result.forEach {
                        Log.i(javaClass.simpleName, "status : ${it.storeStatus}")
                        Log.i(javaClass.simpleName, "data : $it")
                    }
                    restorePurchaseResult.postValue(result)
                }

                override fun onError(error: CBException) {
                    cbException.postValue(error)
                }
            })
    }
}