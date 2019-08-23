package com.android.yuniapp.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.yuniapp.R;
import com.android.yuniapp.application.YuniApplication;
import com.android.yuniapp.model.GetMyQuoteResponseModel;
import com.android.yuniapp.rest.ApiClient;
import com.android.yuniapp.rest.ApiInterface;
import com.android.yuniapp.utils.AndroidUtils;
import com.android.yuniapp.utils.AppUtils;
import com.android.yuniapp.utils.ConnectivityController;
import com.android.yuniapp.utils.ConstantUtils;
import com.google.gson.JsonElement;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.List;

import io.agora.openvcall.model.ConstantApp;
import io.agora.openvcall.ui.CallActivityAgora;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity {
    private final long handlerTime = 3000;
    private Handler mHandler;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private TextView txtQuote;
    private ProgressBar progressBar;
    private RelativeLayout layoutQuote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        AppUtils.setFullScreen(this);

        txtQuote = findViewById(R.id.txt_quote);
        progressBar = findViewById(R.id.progress_bar);
        layoutQuote = findViewById(R.id.layout_quote);

        sharedPreferences = getSharedPreferences(ConstantUtils.APP_PREF, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        mHandler = new Handler();
        if (ConnectivityController.isNetworkAvailable(this))
            callGetMyQuoteApi();
        else AndroidUtils.showToast(this, getResources().getString(R.string.no_internet));


        /*mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (sharedPreferences.getBoolean(ConstantUtils.PREF_IS_LOGIN, false)) {
                    startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                    finish();
                } else if(sharedPreferences.getBoolean(ConstantUtils.IS_FIRST_TIME_LAUNCH,false)){
                    startActivity(new Intent(SplashActivity.this, GetStartedActivity.class));
                    finish();
                }else {
                    startActivity(new Intent(SplashActivity.this, IntroActivity.class));
                    finish();
                }
            }
        }, handlerTime);*/

    }


    private void callGetMyQuoteApi() {
        layoutQuote.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetMyQuoteResponseModel> getMyQuoteResponseModelCall = apiService.getMyQuote(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""));
        getMyQuoteResponseModelCall.enqueue(new Callback<GetMyQuoteResponseModel>() {
            @Override
            public void onResponse(Call<GetMyQuoteResponseModel> call, Response<GetMyQuoteResponseModel> response) {
                progressBar.setVisibility(View.GONE);
                if (response.code() == 200 && response.body() != null) {
                    if (response.body().getQuote_text() != null && !response.body().getQuote_text().isEmpty()) {
                        editor.putString(ConstantUtils.OWN_QUOTE, response.body().getQuote_text()).commit();
                        layoutQuote.setVisibility(View.VISIBLE);
                        txtQuote.setText(response.body().getQuote_text());
                    }

                }

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (getIntent().getBooleanExtra(ConstantApp.FROM_NOTIFICATION, false)) {
                            checkPermissionsForVideoCall();
                        } else if (sharedPreferences.getBoolean(ConstantUtils.PREF_IS_LOGIN, false)) {
                            startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                            finish();
                        } else if (sharedPreferences.getBoolean(ConstantUtils.IS_FIRST_TIME_LAUNCH, false)) {
                            startActivity(new Intent(SplashActivity.this, GetStartedActivity.class));
                            finish();
                        } else {
                            startActivity(new Intent(SplashActivity.this, IntroActivity.class));
                            finish();
                        }
                    }
                }, handlerTime);
            }

            @Override
            public void onFailure(Call<GetMyQuoteResponseModel> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                if (getIntent().getBooleanExtra(ConstantApp.FROM_NOTIFICATION, false)) {
                    checkPermissionsForVideoCall();
                } else if (sharedPreferences.getBoolean(ConstantUtils.PREF_IS_LOGIN, false)) {
                    openHomeActivity();
                } else if (sharedPreferences.getBoolean(ConstantUtils.IS_FIRST_TIME_LAUNCH, false)) {
                    startActivity(new Intent(SplashActivity.this, GetStartedActivity.class));
                    finish();
                } else {
                    startActivity(new Intent(SplashActivity.this, IntroActivity.class));
                    finish();
                }

            }
        });
    }

    private void openHomeActivity() {
        updateDeviceToken();
        startActivity(new Intent(SplashActivity.this, HomeActivity.class));
        finish();
    }

    private void updateDeviceToken() {
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        apiService.updateDeviceDetails(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), "A", sharedPreferences.getString(ConstantUtils.DEVICE_TOKEN, ""))
                .enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {

                    }

                    @Override
                    public void onFailure(Call<JsonElement> call, Throwable t) {

                    }
                });

    }

    private void checkPermissionsForVideoCall() {
        String[] PERMISSIONS;
        ArrayList<String> permissionList = new ArrayList<>();
        permissionList.add(Manifest.permission.CAMERA);
        permissionList.add(Manifest.permission.RECORD_AUDIO);
        permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (AppUtils.checkWriteExternalStoragePermission(this)) {
            permissionList.remove(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (AppUtils.checkCameraPermission(this)) {
            permissionList.remove(Manifest.permission.CAMERA);
        }
        if (AppUtils.checkRecordAudio(this)) {
            permissionList.remove(Manifest.permission.RECORD_AUDIO);
        }
        PERMISSIONS = new String[permissionList.size()];
        PERMISSIONS = permissionList.toArray(PERMISSIONS);
        if (permissionList.size() == 0) {
            openVideoCallActivity();
        } else
            Dexter.withActivity(this)
                    .withPermissions(PERMISSIONS)
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport report) {
                            if (report.areAllPermissionsGranted()) {
                                openVideoCallActivity();
                            } else {
                                if (report.getDeniedPermissionResponses().size() > 0) {
                                    AppUtils.showPermissionDeniedToast(SplashActivity.this, report.getDeniedPermissionResponses().get(0).getPermissionName());
//                                    finish();
                                    openHomeActivity();
                                    return;
                                }
                            }
                            if (report.isAnyPermissionPermanentlyDenied()) {
                                AppUtils.showPermanentPermissionDeniedToast(SplashActivity.this, report.getDeniedPermissionResponses().get(0).getPermissionName());
//                                finish();
                                openHomeActivity();
                            }


                        }


                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    })
                    .onSameThread().check();
    }

    private void openVideoCallActivity() {
        Intent i = new Intent(this, CallActivityAgora.class);
        i.putExtra(ConstantApp.ACTION_KEY_CHANNEL_NAME, getIntent().getStringExtra(ConstantApp.ACTION_KEY_CHANNEL_NAME));
        i.putExtra(ConstantApp.ACTION_KEY_ENCRYPTION_KEY, "");
        i.putExtra(ConstantApp.IS_HOST, false);
        i.putExtra(ConstantApp.ACTION_KEY_ENCRYPTION_MODE,
                getResources()
                        .getStringArray(R.array.encryption_mode_values)[YuniApplication.mVideoSettings.mEncryptionModeIndex]);
        startActivity(i);
        finish();
    }
}
