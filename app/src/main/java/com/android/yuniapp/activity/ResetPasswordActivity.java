package com.android.yuniapp.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.android.yuniapp.R;
import com.android.yuniapp.model.ErrorResponse;
import com.android.yuniapp.model.GetMessageModel;
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

public class ResetPasswordActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText edtNewPassword,edtConfirmPassword;
    private Dialog progressDialog;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ImageView imgShowNewPassword,imgShowConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        AppUtils.setFullScreen(this);
        progressDialog = new Dialog(this);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setContentView(R.layout.fragment_loader);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);


        sharedPreferences = getSharedPreferences(ConstantUtils.APP_PREF, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        edtNewPassword=findViewById(R.id.edt_new_pass);
        edtConfirmPassword=findViewById(R.id.edt_confirm_pass);
        findViewById(R.id.btn_reset_pass).setOnClickListener(this);
        findViewById(R.id.img_back).setOnClickListener(this);

        imgShowNewPassword=findViewById(R.id.img_show_new_pass);
        imgShowConfirmPassword=findViewById(R.id.img_show_confirm_pass);
        imgShowNewPassword.setOnClickListener(this);
        imgShowConfirmPassword.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.img_back:
                onBackPressed();
                break;
            case R.id.btn_reset_pass:
                if(edtNewPassword.getText().toString().trim().isEmpty())
                    AndroidUtils.showToast(this,edtNewPassword.getHint().toString()+getResources().getString(R.string.cant_empty));
                else  if(edtConfirmPassword.getText().toString().trim().isEmpty())
                    AndroidUtils.showToast(this,edtConfirmPassword.getHint().toString()+getResources().getString(R.string.cant_empty));
                else if(edtNewPassword.getText().toString().trim().length()<6)
                    AndroidUtils.showToast(this,getResources().getString(R.string.valid_pass));
                else if(!edtNewPassword.getText().toString().trim().equals(edtConfirmPassword.getText().toString().trim()))
                  AndroidUtils.showToast(this,getResources().getString(R.string.password_not_match));
                else {
                    if(ConnectivityController.isNetworkAvailable(this))
                        callResetPasswordApi();
                    else AndroidUtils.showToast(this,getResources().getString(R.string.no_internet));
                }
                break;
            case R.id.img_show_new_pass:
                if(imgShowNewPassword.isSelected())
                {

                    imgShowNewPassword.setSelected(false);
                    edtNewPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }else {
                    imgShowNewPassword.setSelected(true);
                    edtNewPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                break;
            case R.id.img_show_confirm_pass:
                if(imgShowConfirmPassword.isSelected())
                {

                    imgShowConfirmPassword.setSelected(false);
                    edtConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }else {
                    imgShowConfirmPassword.setSelected(true);
                    edtConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                break;
        }
    }

    private void callResetPasswordApi() {
        progressDialog.show();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetMessageModel> resetPassResponseCall=apiService.resetPassword(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN,""),edtNewPassword.getText().toString());
        resetPassResponseCall.enqueue(new Callback<GetMessageModel>() {
            @Override
            public void onResponse(Call<GetMessageModel> call, Response<GetMessageModel> response) {
                progressDialog.dismiss();
                    if(response.code()==200 && response.body()!=null)
                    {
                        if(response.body().getMessage()!=null)
                            AndroidUtils.showToast(ResetPasswordActivity.this,response.body().getMessage());

                        Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else if (response.code() == 400) {
                    Gson gson = new Gson();
                    try {
                        ErrorResponse errorResponse = gson.fromJson(
                                response.errorBody().string(), ErrorResponse.class);
                        if (errorResponse != null && errorResponse.getMessage() != null && !errorResponse.getMessage().isEmpty())
                            AndroidUtils.showToast(ResetPasswordActivity.this, errorResponse.getMessage());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<GetMessageModel> call, Throwable t) {
               progressDialog.dismiss();
               AndroidUtils.showToast(ResetPasswordActivity.this,getResources().getString(R.string.some_went_wrong));
            }
        });
    }
}
