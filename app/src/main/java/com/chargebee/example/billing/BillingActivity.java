package com.chargebee.example.billing;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.chargebee.android.billingservice.PurchaseModel;
import com.chargebee.android.models.Products;
import com.chargebee.example.BaseActivity;
import com.chargebee.example.R;
import com.chargebee.example.adapter.ProductListAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import static com.chargebee.example.util.Constants.PRODUCTS_LIST_KEY;

public class BillingActivity extends BaseActivity implements ProductListAdapter.ProductClickListener {

    private ArrayList<Products> productList = null;
    private ProductListAdapter productListAdapter = null;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerView mItemsRecyclerView = null;
    private BillingViewModel billingViewModel= new BillingViewModel();
    private static final String TAG = "BillingActivity";
    private int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        mItemsRecyclerView = findViewById(R.id.rv_product_list);
        String productDetails = getIntent().getStringExtra(PRODUCTS_LIST_KEY);

        if(productDetails != null) {
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<Products>>() {}.getType();
            productList = gson.fromJson(productDetails, listType);
        }

        productListAdapter = new ProductListAdapter(this,productList, this);
        linearLayoutManager = new LinearLayoutManager(this);
        mItemsRecyclerView.setLayoutManager(linearLayoutManager);
        mItemsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mItemsRecyclerView.setAdapter(productListAdapter);

        this.billingViewModel.getProductPurchaseResult().observe(this, purchaseModel -> {
            PurchaseModel p = (PurchaseModel) purchaseModel;
            String purchaseToken = p.getPurchaseToken();
            System.out.println("purchaseToken :"+purchaseToken);
            Log.i(TAG, "purchaseToken :"+purchaseToken);
            showPurchaseSuccessDialog(purchaseToken);
            updateSubscribeStatus();
            //showProgressDialog();
            // billingViewModel.updatePurchaseToken(purchaseToken);
        });

        this.billingViewModel.getMPurchaseTokenStatus().observe(this, purchaseModel -> {
           hideProgressDialog();
            Log.i(TAG, "purchaseToken update status :");

        });

        this.billingViewModel.getCbException().observe(this, error -> {
            hideProgressDialog();
            Log.i(TAG, "error :");

        });

    }

    @Override
    public void onProductClick(View view, int position) {
        this.position = position;
        this.billingViewModel.purchaseProduct(productList.get(position));
    }
    private void updateSubscribeStatus(){
        productList.get(position).setSubStatus(true);
        productListAdapter.notifyDataSetChanged();
    }
}
