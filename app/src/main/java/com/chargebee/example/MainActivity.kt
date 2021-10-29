package com.chargebee.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chargebee.android.billingservice.CBCallback
import com.chargebee.android.billingservice.CBPurchase
import com.chargebee.android.exceptions.CBException
import com.chargebee.android.models.Products
import com.chargebee.example.adapter.ListItemsAdapter
import com.chargebee.example.addon.AddonActivity
import com.chargebee.example.billing.BillingActivity
import com.chargebee.example.plan.PlanInJavaActivity
import com.chargebee.example.token.TokenizeActivity
import com.chargebee.example.util.CBMenu
import com.chargebee.example.util.Constants.PRODUCTS_LIST_KEY
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), ListItemsAdapter.ItemClickListener {
    private var mItemsRecyclerView: RecyclerView? = null
    private var list  = arrayListOf<String>()
    var listItemsAdapter: ListItemsAdapter? = null
    var featureList = mutableListOf<CBMenu>()
    var mContext: Context? = null
    private val TAG = "MainActivity"
    private val gson = Gson()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mContext = this
        this.mItemsRecyclerView = findViewById(R.id.rv_list_feature)
        //initializeListeners()
        setListAdapter()
    }

    private fun setListAdapter(){
        featureList = CBMenu.values().toMutableList()
        listItemsAdapter = ListItemsAdapter(featureList, this)
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(applicationContext)
        mItemsRecyclerView?.setLayoutManager(layoutManager)
        mItemsRecyclerView?.setItemAnimator(DefaultItemAnimator())
        mItemsRecyclerView?.setAdapter(listItemsAdapter)
    }

    override fun onItemClick(view: View?, position: Int) {
        when(CBMenu.valueOf(featureList.get(position).toString()).value){
            CBMenu.Configure.value ->{
                //TODO Implementation yet to be done
            }
            CBMenu.GetPlan.value->{
                val intent = Intent(this, PlanInJavaActivity::class.java)
                startActivity(intent)
            }
            CBMenu.GetAddOn.value ->{
                val intent = Intent(this, AddonActivity::class.java)
                startActivity(intent)
            }
            CBMenu.Tokenize.value ->{
                val intent = Intent(this, TokenizeActivity::class.java)
                startActivity(intent)
            }
            CBMenu.ProductIDs.value ->{
                CBPurchase.retrieveProductIDs(this, object : CBCallback.ListProductIDsCallback<ArrayList<String>>{
                    override fun onSuccess(productIDs: ArrayList<String>) {
                        list = productIDs
                    }
                    override fun onError(error: CBException) {
                        Log.e(TAG," ${error.message}")
                    }
                })
            }
            CBMenu.GetProducts.value -> {
                val SUBS_SKUS = arrayListOf("merchant.premium.android", "merchant.pro.android")
                CBPurchase.retrieveProducts(this,SUBS_SKUS, object : CBCallback.ListProductsCallback<ArrayList<Products>>{
                    override fun onSuccess(productDetails: ArrayList<Products>) {
                        GlobalScope.launch {
                            launchProductDetailsScreen(gson.toJson(productDetails))
                        }
                    }
                    override fun onError(error: CBException) {
                        Log.e(TAG," ${error.message}")
                    }
                })

            }
            CBMenu.GetPlans.value -> {

            }
            else ->{
                Log.e(TAG,"Not configured")
            }
        }
    }

    private fun launchProductDetailsScreen(productDetails: String){
        val intent = Intent(this, BillingActivity::class.java)
        intent.putExtra(PRODUCTS_LIST_KEY,productDetails)
        this.startActivity(intent)
    }


}