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
import com.android.yuniapp.model.GetAuthTokenModel;
import com.android.yuniapp.rest.ApiClient;
import com.android.yuniapp.rest.ApiInterface;
import com.android.yuniapp.utils.AndroidUtils;
import com.android.yuniapp.utils.AppUtils;
import com.android.yuniapp.utils.ConnectivityController;
import com.android.yuniapp.utils.ConstantUtils;
import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ValidateCodeActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText edtEnterCode;
    private Dialog progressDialog;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String emailAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validate_code);


        AppUtils.setFullScreen(this);

        progressDialog = new Dialog(this);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setContentView(R.layout.fragment_loader);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);


        sharedPreferences = getSharedPreferences(ConstantUtils.APP_PREF, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        edtEnterCode=findViewById(R.id.edt_enter_code);
        findViewById(R.id.img_back).setOnClickListener(this);

        if (getIntent().getStringExtra(ConstantUtils.USER_EMAIL) != null && !getIntent().getStringExtra(ConstantUtils.USER_EMAIL).isEmpty()) {
            emailAdd = getIntent().getStringExtra(ConstantUtils.USER_EMAIL);
            findViewById(R.id.btn_submit).setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_submit:
                if (edtEnterCode.getText().toString().trim().isEmpty())
                    AndroidUtils.showToast(this, edtEnterCode.getHint().toString()+getResources().getString(R.string.cant_empty));
                else {
                    if (ConnectivityController.isNetworkAvailable(this))
                        callValidateCodeApi();
                    else
                        AndroidUtils.showToast(this, getResources().getString(R.string.no_internet));
                }
                break;
            case R.id.img_back:
                onBackPressed();
                break;
        }
    }

    private void callValidateCodeApi() {
        progressDialog.show();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetAuthTokenModel> validateCodeResponseCall = apiService.validateResetCode(emailAdd,edtEnterCode.getText().toString());
        validateCodeResponseCall.enqueue(new Callback<GetAuthTokenModel>() {
            @Override
            public void onResponse(Call<GetAuthTokenModel> call, Response<GetAuthTokenModel> response) {
                progressDialog.dismiss();

                if(response.code()==200 && response.body()!=null && response.body().getAuth_token()!=null)
                {
                    editor.putString(ConstantUtils.AUTH_TOKEN,response.body().getAuth_token()).commit();
                    startActivity(new Intent(ValidateCodeActivity.this,ResetPasswordActivity.class));

                }else if(response.code()==400)
                {
                    Gson gson = new Gson();
                    try {
                        ErrorResponse errorResponse = gson.fromJson(
                                response.errorBody().string(), ErrorResponse.class);
                        if (errorResponse != null && errorResponse.getMessage() != null && !errorResponse.getMessage().isEmpty())
                            AndroidUtils.showToast(ValidateCodeActivity.this, errorResponse.getMessage());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(Call<GetAuthTokenModel> call, Throwable t) {
               progressDialog.dismiss();
            }
        });
    }
}
