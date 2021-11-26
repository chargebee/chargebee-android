package com.chargebee.example.items

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

class ItemsActivity : BaseActivity(), ItemsAdapter.ItemClickListener {

    private var viewModel: ItemsViewModel? = null
    var mItemsList = ArrayList<String>()
    var listItemsAdapter: ItemsAdapter? = null
    private var mItemsRecyclerView: RecyclerView? = null
    private var mErrorTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_items)

        this.mItemsRecyclerView = findViewById(R.id.rv_list_items)
        this.mErrorTextView = findViewById(R.id.errorMessage)

        viewModel = ItemsViewModel()
        showProgressDialog()
        val queryParam = arrayOf("8", "Standard", Chargebee.channel)
        viewModel!!.retrieveAllItems(queryParam)

        viewModel?.mItemsResult?.observe(this, Observer {
            hideProgressDialog()
            if (it != null) {
                mItemsList = it
                setItemListAdapter()
                Log.i(javaClass.simpleName, "mItemsList  :  $mItemsList")
            }
        })

        viewModel?.mItemsError?.observe(this, Observer {
            hideProgressDialog()
            mErrorTextView?.setText(
                Gson().fromJson<ErrorDetail>(
                    it,
                    ErrorDetail::class.java
                ).message
            )
        })
    }

    private fun setItemListAdapter(){
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