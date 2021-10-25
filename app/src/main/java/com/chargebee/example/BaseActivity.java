package com.chargebee.example;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
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

}
