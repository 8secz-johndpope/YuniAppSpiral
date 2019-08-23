package com.android.yuniapp.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import com.android.yuniapp.R;
import com.android.yuniapp.model.ErrorResponse;
import com.android.yuniapp.model.GetMessageModel;
import com.android.yuniapp.rest.ApiClient;
import com.android.yuniapp.rest.ApiInterface;
import com.android.yuniapp.utils.AndroidUtils;
import com.android.yuniapp.utils.AppUtils;
import com.android.yuniapp.utils.ConnectivityController;
import com.android.yuniapp.utils.ConstantUtils;
import com.cunoraz.gifview.library.GifView;
import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText edtEmailAdd;
    private Dialog progressDialog;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        AppUtils.setFullScreen(this);

        progressDialog = new Dialog(this);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setContentView(R.layout.fragment_loader);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);


        sharedPreferences = getSharedPreferences(ConstantUtils.APP_PREF, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        initView();
        setOnClick();
    }


    private void initView() {
        edtEmailAdd = findViewById(R.id.edt_email_add);
    }

    private void setOnClick() {
        findViewById(R.id.img_back).setOnClickListener(this);
        findViewById(R.id.btn_submit).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_back:
                onBackPressed();
                break;
            case R.id.btn_submit:
                if (edtEmailAdd.getText().toString().trim().isEmpty())
                    AndroidUtils.showToast(this, edtEmailAdd.getHint().toString() + getResources().getString(R.string.cant_empty));
                else if (!AndroidUtils.isValidEmailAddress(edtEmailAdd.getText().toString().trim()))
                    AndroidUtils.showToast(this, getResources().getString(R.string.valid_email));
                else {
                    if (ConnectivityController.isNetworkAvailable(this))
                        callSendResatePasswordCode();
                    else
                        AndroidUtils.showToast(this, getResources().getString(R.string.no_internet));
                }
                break;
        }
    }

    private void callSendResatePasswordCode() {
        progressDialog.show();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetMessageModel> resetPasswordCodeResponseCall = apiService.sendResetCode(edtEmailAdd.getText().toString().trim());
        resetPasswordCodeResponseCall.enqueue(new Callback<GetMessageModel>() {
            @Override
            public void onResponse(Call<GetMessageModel> call, Response<GetMessageModel> response) {
                progressDialog.dismiss();

                if (response.code() == 200) {
                    Intent intent = new Intent(ForgotPasswordActivity.this, ValidateCodeActivity.class);
                    intent.putExtra(ConstantUtils.USER_EMAIL, edtEmailAdd.getText().toString());
                    startActivity(intent);
                } else if (response.code() == 400) {
                    Gson gson = new Gson();
                    try {
                        ErrorResponse errorResponse = gson.fromJson(
                                response.errorBody().string(), ErrorResponse.class);
                        if (errorResponse != null && errorResponse.getMessage() != null && !errorResponse.getMessage().isEmpty())
                            AndroidUtils.showToast(ForgotPasswordActivity.this, errorResponse.getMessage());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<GetMessageModel> call, Throwable t) {
                progressDialog.dismiss();
            }
        });

    }
}
