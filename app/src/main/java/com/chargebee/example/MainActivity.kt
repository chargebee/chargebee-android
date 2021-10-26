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
import com.chargebee.example.util.CBItems
import com.chargebee.example.util.Constants.PRODUCTS_LIST_KEY
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), ListItemsAdapter.ItemClickListener {
    private var mItemsRecyclerView: RecyclerView? = null
    private var list  = arrayListOf<String>()
    var listItemsAdapter: ListItemsAdapter? = null
    var featureList = mutableListOf<CBItems>()
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
        featureList = CBItems.values().toMutableList()
        listItemsAdapter = ListItemsAdapter(featureList, this)
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(applicationContext)
        mItemsRecyclerView?.setLayoutManager(layoutManager)
        mItemsRecyclerView?.setItemAnimator(DefaultItemAnimator())
        mItemsRecyclerView?.setAdapter(listItemsAdapter)
    }

    override fun onItemClick(view: View?, position: Int) {
        when(CBItems.valueOf(featureList.get(position).toString()).value){
            CBItems.Configure.value ->{
                //TODO Implementation yet to be done
            }
            CBItems.ShowPlan.value->{
                val intent = Intent(this, PlanInJavaActivity::class.java)
                startActivity(intent)
            }
            CBItems.ShowAddOn.value ->{
                val intent = Intent(this, AddonActivity::class.java)
                startActivity(intent)
            }
            CBItems.Tokenize.value ->{
                val intent = Intent(this, TokenizeActivity::class.java)
                startActivity(intent)
            }
            CBItems.ProductIDs.value ->{
                CBPurchase.retrieveProductIDs(this, object : CBCallback.ListProductIDsCallback<ArrayList<String>>{
                    override fun onSuccess(productIDs: ArrayList<String>) {
                        list = productIDs
                    }
                    override fun onError(error: CBException) {
                        Log.e(TAG," ${error.message}")
                    }
                })
            }
            else ->{
                val SUBS_SKUS = arrayListOf("gaurav_test", "cb_weekly_premium", "test_123_gaurav", "test_weekly_premium")
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
        }
    }

    private fun launchProductDetailsScreen(productDetails: String){
        val intent = Intent(this, BillingActivity::class.java)
        intent.putExtra(PRODUCTS_LIST_KEY,productDetails)
        this.startActivity(intent)
    }


}