package com.chargebee.example.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.chargebee.android.models.Products;
import com.chargebee.example.R;
import java.util.List;

public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ViewHolder> {

    private List<Products> mProductsList;
    private ProductListAdapter.ProductClickListener mClickListener;
    private Context mContext = null;

    public ProductListAdapter(Context context, List<Products> mProductsList, ProductClickListener mClickListener) {
        mContext = context;
        this.mProductsList = mProductsList;
        this.mClickListener = mClickListener;
    }

    @Override
    public ProductListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_products_item,parent,false);
        return new ProductListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductListAdapter.ViewHolder holder, int position) {
        Products products = mProductsList.get(position);
        holder.mTextViewTitle.setText(products.getProductId());
        holder.mTextViewPrice.setText(products.getProductPrice());
        if (products.getSubStatus()) {
            holder.mTextViewSubscribe.setText(R.string.status_subscribed);
            holder.mTextViewSubscribe.setTextColor(mContext.getResources().getColor(R.color.success_green));
        }else {
            holder.mTextViewSubscribe.setText(R.string.status_subscribe);
            holder.mTextViewSubscribe.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
        }

    }

    @Override
    public int getItemCount() {
        return mProductsList.size();
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
                mClickListener.onProductClick(view, getAdapterPosition());
            }
        }
    }

    public interface ProductClickListener {
        void onProductClick(View view, int position);
    }
}