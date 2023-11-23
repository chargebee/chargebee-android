package com.chargebee.example.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.chargebee.android.billingservice.ProductType;
import com.chargebee.android.models.CBProduct;
import com.chargebee.android.models.PricingPhase;
import com.chargebee.android.models.SubscriptionOffer;
import com.chargebee.example.R;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ViewHolder> {

    private List<PurchaseProduct> purchaseProducts;
    private ProductListAdapter.ProductClickListener mClickListener;
    private Context mContext = null;
    private PurchaseProduct selectedProduct = null;

    public ProductListAdapter(Context context, List<PurchaseProduct> purchaseProducts, ProductClickListener mClickListener) {
        mContext = context;
        this.purchaseProducts = purchaseProducts;
        this.mClickListener = mClickListener;
    }

    @Override
    public ProductListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_products_item,parent,false);
        return new ProductListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductListAdapter.ViewHolder holder, int position) {
        PurchaseProduct purchaseProduct = purchaseProducts.get(position);
        holder.mTextViewTitle.setText(purchaseProduct.getProductId() + " "+ purchaseProduct.getBasePlanId());
        holder.mTextViewPrice.setText(purchaseProduct.getPrice());
        boolean isSubscriptionProductSelected = selectedProduct != null && selectedProduct.getCbProduct().getType().equals(ProductType.SUBS) && selectedProduct.getOfferToken().equals(purchaseProduct.getOfferToken());
        boolean isOtpProductSelected = selectedProduct != null && selectedProduct.getCbProduct().getType().equals(ProductType.INAPP) && selectedProduct.getProductId().equals(purchaseProduct.getProductId());
        if (isSubscriptionProductSelected || isOtpProductSelected) {
            holder.mTextViewSubscribe.setText(R.string.status_subscribed);
            holder.mTextViewSubscribe.setTextColor(mContext.getResources().getColor(R.color.success_green));
        }else {
            holder.mTextViewSubscribe.setText(R.string.status_subscribe);
            holder.mTextViewSubscribe.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
        }

    }

    @Override
    public int getItemCount() {
        return purchaseProducts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mTextViewTitle,mTextViewPrice, mTextViewSubscribe;

        ViewHolder(View itemView) {
            super(itemView);
            mTextViewTitle = itemView.findViewById(R.id.tv_productTitle);
            mTextViewPrice = itemView.findViewById(R.id.tv_productPrice);
            mTextViewSubscribe = itemView.findViewById(R.id.tv_subscription);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) {
                selectedProduct = purchaseProducts.get(getAdapterPosition());
                mClickListener.onProductClick(view, getAdapterPosition());
            }
        }
    }

    public interface ProductClickListener {
        void onProductClick(View view, int position);
    }
}