package com.chargebee.example;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public void showProgressDialog(){
        Log.i("BaseActivity","show loader");
        try {
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(this, R.style.ProgressDialogTheme);
                progressDialog.setCancelable(false);
            }
            progressDialog.show();
            progressDialog.setContentView(R.layout.progress_dialog);
        }
        catch (Exception e){
            Log.e("BaseActivity",e.getLocalizedMessage());
        }
    }
    public void hideProgressDialog(){
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (IllegalArgumentException e) {
            Log.e("BaseActivity",e.getLocalizedMessage());
        }
        progressDialog = null;
    }

    protected void showPurchaseSuccessDialog(String purchaseToken){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_layout);
        TextView textViewMessage = (TextView) dialog.findViewById(R.id.tv_message);
        String msg = getResources().getString(R.string.dialog_message)+" "+purchaseToken;
        textViewMessage.setText(msg);
        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
