package com.chargebee.example.plan

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chargebee.android.Chargebee
import com.chargebee.android.ErrorDetail
import com.chargebee.example.BaseActivity
import com.chargebee.example.R
import com.chargebee.example.adapter.ItemsAdapter
import com.google.gson.Gson

class PlansActivity : BaseActivity(), ItemsAdapter.ItemClickListener {

    private var viewModel: PlanViewModel? = null
    var mItemsList = ArrayList<String>()
    var listItemsAdapter: ItemsAdapter? = null
    private var mItemsRecyclerView: RecyclerView? = null
    private var mErrorTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plans)

        this.mItemsRecyclerView = findViewById(R.id.rv_list_plans)

        this.mErrorTextView = findViewById(R.id.errorMessage)
        viewModel = PlanViewModel()
        showProgressDialog()
        val queryParam = arrayOf("Standard", Chargebee.channel)
        viewModel!!.retrieveAllPlans(queryParam)

        viewModel?.mPlansResult?.observe(this, Observer {
            hideProgressDialog()
            if (it != null) {
                mItemsList = it
                setPlansAdapter()
                Log.i(javaClass.simpleName, "mPlansList  :  $mItemsList")
            }
        })

        viewModel?.planError?.observe(this, Observer {
            hideProgressDialog()
            mErrorTextView?.setText(
                Gson().fromJson<ErrorDetail>(
                    it,
                    ErrorDetail::class.java
                ).message
            )
        })
    }

    private fun setPlansAdapter(){
        listItemsAdapter = ItemsAdapter(mItemsList, this)
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(applicationContext)
        mItemsRecyclerView?.setLayoutManager(layoutManager)
        mItemsRecyclerView?.setItemAnimator(DefaultItemAnimator())
        mItemsRecyclerView?.setAdapter(listItemsAdapter)
    }

    override fun onItemClick(view: View?, position: Int) {
        TODO("Not yet implemented")
    }
}