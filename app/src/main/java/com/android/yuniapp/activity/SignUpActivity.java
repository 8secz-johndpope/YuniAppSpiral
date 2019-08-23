package com.android.yuniapp.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.yuniapp.R;
import com.android.yuniapp.model.ErrorResponse;
import com.android.yuniapp.model.GetLoginResponse;
import com.android.yuniapp.rest.ApiClient;
import com.android.yuniapp.rest.ApiInterface;
import com.android.yuniapp.utils.AndroidUtils;
import com.android.yuniapp.utils.AppUtils;
import com.android.yuniapp.utils.ConnectivityController;
import com.android.yuniapp.utils.ConstantUtils;
import com.android.yuniapp.utils.MyClickableSpan;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView imgBottomMoon, imgCheckbox,imgShowPassword;
    private Button btnHowItWorks, btnTutorialVideo, btnLogin;
    private EditText edtUserName, edtEmail, edtAddress, edtPassword;
    private TextView txtAgreement;
    private Dialog progressDialog;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private CallbackManager callbackManager;
    private String userName = "", socialId = "", userPicUrl = "", userEmail;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 007;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        progressDialog = new Dialog(this);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setContentView(R.layout.fragment_loader);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        sharedPreferences = getSharedPreferences(ConstantUtils.APP_PREF, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        AppUtils.setFullScreen(this);
        initViews();
        setOnClick();


        AppUtils.rotateAnimation(imgBottomMoon);
        logInWithFb();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


    }

    private void logInWithFb() {
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {

                        onFacebookLoginSuccessful(loginResult);
                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(FacebookException error) {

                    }

                }

        );

    }

    private void initViews() {
        imgShowPassword=findViewById(R.id.img_show_pass);
        imgBottomMoon = findViewById(R.id.img_bottom_moon);
        edtUserName = findViewById(R.id.edt_user_name);
        edtEmail = findViewById(R.id.edt_email_add);
        edtAddress = findViewById(R.id.edt_address);
        edtPassword = findViewById(R.id.edt_password);
        txtAgreement = findViewById(R.id.txt_agreement);
        customTextView(txtAgreement);
        imgCheckbox = findViewById(R.id.img_checkbox);
      imgCheckbox.setSelected(true);

    }

    private void setOnClick() {

        findViewById(R.id.img_fb).setOnClickListener(this);
        findViewById(R.id.img_google).setOnClickListener(this);
        findViewById(R.id.btn_signup).setOnClickListener(this);
       // findViewById(R.id.btn_how_it_works).setOnClickListener(this);
        //findViewById(R.id.btn_tutorial_video).setOnClickListener(this);
        findViewById(R.id.txt_login).setOnClickListener(this);
        imgCheckbox.setOnClickListener(this);
        imgShowPassword.setOnClickListener(this);
    }

    private void customTextView(TextView view) {

        SpannableStringBuilder spanTxt = new SpannableStringBuilder(
                getResources().getString(R.string.agreement_text));
        spanTxt.append(" \n ");
        spanTxt.append(getResources().getString(R.string.tos));
        spanTxt.setSpan(new MyClickableSpan() {
            @Override
            public void onClick(View widget) {

                if (ConnectivityController.isNetworkAvailable(SignUpActivity.this)) {

                    Intent intent = new Intent(SignUpActivity.this, WebViewActivity.class);
                    intent.putExtra(ConstantUtils.SOURCE, getResources().getString(R.string.tos));
                    startActivity(intent);

                } else {
                    AndroidUtils.showToast(SignUpActivity.this, getResources().getString(R.string.no_internet));
                }

            }

        }, spanTxt.length() - getResources().getString(R.string.tos).length(), spanTxt.length(), 0);

        spanTxt.append(" and ");
        spanTxt.append(getResources().getString(R.string.pp));
        spanTxt.setSpan(new MyClickableSpan() {
            @Override
            public void onClick(View widget) {

                if (ConnectivityController.isNetworkAvailable(SignUpActivity.this)) {

                    Intent intent = new Intent(SignUpActivity.this, WebViewActivity.class);
                    intent.putExtra(ConstantUtils.SOURCE, getResources().getString(R.string.pp));
                    startActivity(intent);

                } else {
                    AndroidUtils.showToast(SignUpActivity.this, getResources().getString(R.string.no_internet));
                }

            }

        }, spanTxt.length() - getResources().getString(R.string.pp).length(), spanTxt.length(), 0);
        view.setMovementMethod(LinkMovementMethod.getInstance());
        view.setText(spanTxt, TextView.BufferType.SPANNABLE);
    }

    private void onFacebookLoginSuccessful(LoginResult loginResult) {


        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {

                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        try {

                            if (object.has("first_name")) {
                                userName = object.getString("first_name");

                            } else {
                                userName = "";

                            }

                            if (object.has("last_name")) {
                                userName = userName + " " + object.getString("last_name");

                            }
                            if (object.has("email")) {
                                userEmail = object.getString("email");

                            } else {
                                userEmail = "";
                            }

                            if (object.has("id")) {
                                socialId = object.getString("id");
                                userPicUrl = "https://graph.facebook.com/" + socialId + "/picture?width=300&height=300";
                            } else {
                                socialId = "";
                            }
                            if(userEmail.isEmpty())
                            {
                                AndroidUtils.showToast(SignUpActivity.this,getResources().getString(R.string.phone_not_fb));
                            }else {
                                if (ConnectivityController.isNetworkAvailable(SignUpActivity.this))
                                    callLoginApi(userEmail, "fb" + socialId, "F");
                                else
                                    AndroidUtils.showToast(getApplicationContext(), getResources().getString(R.string.no_internet));
                            }

                        } catch (JSONException e) {

                            // TODO Auto-generated catch block
                            e.printStackTrace();


                        }

                    }

                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,first_name,email,last_name,picture");
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            /*case R.id.btn_how_it_works:
                startActivity(new Intent(SignUpActivity.this, HowItWorksActivity.class));
                break;
            case R.id.btn_tutorial_video:
               //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://" + "TcMBFSGVi1c")));
                break;*/
            case R.id.btn_signup:
                if (edtUserName.getText().toString().trim().isEmpty())
                    AndroidUtils.showToast(SignUpActivity.this, edtUserName.getHint().toString() + getResources().getString(R.string.cant_empty));
                else if (edtEmail.getText().toString().trim().isEmpty())
                    AndroidUtils.showToast(SignUpActivity.this, edtEmail.getHint().toString() + getResources().getString(R.string.cant_empty));
                else if (edtAddress.getText().toString().trim().isEmpty())
                    AndroidUtils.showToast(SignUpActivity.this, edtAddress.getHint().toString() + getResources().getString(R.string.cant_empty));
                else if (edtPassword.getText().toString().trim().isEmpty())
                    AndroidUtils.showToast(SignUpActivity.this, edtPassword.getHint().toString() + getResources().getString(R.string.cant_empty));
                else if (!AndroidUtils.isValidEmailAddress(edtEmail.getText().toString().trim()))
                    AndroidUtils.showToast(SignUpActivity.this, getResources().getString(R.string.valid_email));
                else if (edtPassword.getText().toString().trim().length() < 6)
                    AndroidUtils.showToast(SignUpActivity.this, getResources().getString(R.string.valid_pass));
                else if (!imgCheckbox.isSelected())
                    AndroidUtils.showToast(SignUpActivity.this, getResources().getString(R.string.valid_agreement));
                else {
                    if (ConnectivityController.isNetworkAvailable(SignUpActivity.this))
                        callRegisterApi("E", "", edtPassword.getText().toString(), edtUserName.getText().toString(), edtEmail.getText().toString(), edtAddress.getText().toString(), "");
                    else
                        AndroidUtils.showToast(this, getResources().getString(R.string.no_internet));
                }
                break;
            case R.id.txt_login:
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                break;
            case R.id.img_checkbox:
                if (imgCheckbox.isSelected()) {
                    imgCheckbox.setSelected(false);
                } else {
                    imgCheckbox.setSelected(true);
                }
                break;
            case R.id.img_fb:
                if (imgCheckbox.isSelected()) {

                    if (ConnectivityController.isNetworkAvailable(SignUpActivity.this)) {
                        LoginManager.getInstance().logOut();
                        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email"));

                    } else {
                        AndroidUtils.showToast(getApplicationContext(), getResources().getString(R.string.no_internet));
                    }
                } else
                    AndroidUtils.showToast(SignUpActivity.this, getResources().getString(R.string.valid_agreement));

                break;
            case R.id.img_google:
                if (imgCheckbox.isSelected()) {
                    if (ConnectivityController.isNetworkAvailable(SignUpActivity.this)) {
                        googleSignIn();

                    } else {
                        AndroidUtils.showToast(SignUpActivity.this, getResources().getString(R.string.no_internet));
                    }
                } else
                    AndroidUtils.showToast(SignUpActivity.this, getResources().getString(R.string.valid_agreement));
                break;
            case R.id.img_show_pass:
                if(imgShowPassword.isSelected())
                {

                    imgShowPassword.setSelected(false);
                    edtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }else {
                    imgShowPassword.setSelected(true);
                    edtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
        }

    }

    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);

        } else if (FacebookSdk.isFacebookRequestCode(requestCode)) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                if (account.getEmail() != null)
                    userEmail = account.getEmail();
                else userEmail = "";

                if (account.getDisplayName() != null)
                    userName = account.getDisplayName();
                else userName = "";

                if (account.getId() != null)
                    socialId = account.getId();
                else socialId = "";

                if (account.getPhotoUrl() != null)
                    userPicUrl = account.getPhotoUrl().toString();
                else userPicUrl = "";


            }


                if (ConnectivityController.isNetworkAvailable(SignUpActivity.this))
                    callLoginApi(userEmail, "g" + socialId, "G");
                else
                    AndroidUtils.showToast(getApplicationContext(), getResources().getString(R.string.no_internet));


        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("api exception", "signInResult:failed code=" + e.getStatusCode());

        }
    }

    private void callRegisterApi(final String authType, String socialId, String password, String name, String email, String address, String profilePic) {
        progressDialog.show();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetLoginResponse> signupResponseCall = apiService.register(name, email,
                address, password, profilePic, socialId, authType, ConstantUtils.DEVICE_TYPE, sharedPreferences.getString(ConstantUtils.DEVICE_TOKEN, ""));
        signupResponseCall.enqueue(new Callback<GetLoginResponse>() {
            @Override
            public void onResponse(Call<GetLoginResponse> call, Response<GetLoginResponse> response) {
                progressDialog.dismiss();
                if (response.code() == 200) {
                    if (response.body() != null) {
                        editor.putBoolean(ConstantUtils.PREF_IS_LOGIN, true);

                        if (response.body().getAuth_token() != null)
                            editor.putString(ConstantUtils.AUTH_TOKEN, response.body().getAuth_token());

                        if(response.body().getUser_id()!=null)
                            editor.putString(ConstantUtils.USER_ID,response.body().getUser_id());

                        if (response.body().getName() != null)
                            editor.putString(ConstantUtils.USER_NAME, response.body().getName());


                        if (response.body().getEmail_id() != null)
                            editor.putString(ConstantUtils.USER_EMAIL, response.body().getEmail_id());

                        if (response.body().getAddress() != null)
                            editor.putString(ConstantUtils.ADDRESS, response.body().getAddress());

                        if (response.body().getDob() != null)
                            editor.putString(ConstantUtils.DOB, response.body().getDob());

                        if (response.body().getGender() != null)
                            editor.putString(ConstantUtils.GENDER, response.body().getGender());


                        if (response.body().getProfile_pic() != null)
                            editor.putString(ConstantUtils.USER_PROFILE, response.body().getProfile_pic());

                        editor.commit();

                        if (!authType.equalsIgnoreCase("E")) {

                            Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(SignUpActivity.this, UploadPhotoActivity.class);
                            // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }

                    }

                } else if (response.code() == 400) {
                    Gson gson = new Gson();
                    try {
                        ErrorResponse errorResponse = gson.fromJson(
                                response.errorBody().string(), ErrorResponse.class);
                        if (errorResponse != null && errorResponse.getMessage() != null && !errorResponse.getMessage().isEmpty())
                            AndroidUtils.showToast(SignUpActivity.this, errorResponse.getMessage());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else if (response.code() == 500) {
                    AndroidUtils.showToast(SignUpActivity.this, getResources().getString(R.string.some_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<GetLoginResponse> call, Throwable t) {
                progressDialog.dismiss();
                AndroidUtils.showToast(SignUpActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });
    }

    private void callLoginApi(String emailId, String password, final String authType) {
        progressDialog.show();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetLoginResponse> getLoginResponseCall = apiService.login(emailId, password, password, ConstantUtils.DEVICE_TYPE, sharedPreferences.getString(ConstantUtils.DEVICE_TOKEN, ""), authType);
        getLoginResponseCall.enqueue(new Callback<GetLoginResponse>() {
            @Override
            public void onResponse(Call<GetLoginResponse> call, Response<GetLoginResponse> response) {
                progressDialog.dismiss();

                if (response.code() == 200) {
                    if (response.body() != null) {
                        editor.putBoolean(ConstantUtils.PREF_IS_LOGIN, true);

                        if (response.body().getAuth_token() != null)
                            editor.putString(ConstantUtils.AUTH_TOKEN, response.body().getAuth_token());

                        if (response.body().getName() != null)
                            editor.putString(ConstantUtils.USER_NAME, response.body().getName());

                        if(response.body().getUser_id()!=null)
                            editor.putString(ConstantUtils.USER_ID,response.body().getUser_id());

                        if (response.body().getEmail_id() != null)
                            editor.putString(ConstantUtils.USER_EMAIL, response.body().getEmail_id());

                        if (response.body().getAddress() != null)
                            editor.putString(ConstantUtils.ADDRESS, response.body().getAddress());

                        if (response.body().getDob() != null)
                            editor.putString(ConstantUtils.DOB, response.body().getDob());

                        if (response.body().getGender() != null)
                            editor.putString(ConstantUtils.GENDER, response.body().getGender());
                        editor.commit();

                        Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }

                } else if (response.code() == 400) {

                    String password = "";
                    if (authType.equalsIgnoreCase("F"))
                        password = "fb" + socialId;
                    else if (authType.equalsIgnoreCase("G"))
                        password = "g" + socialId;

                    if (ConnectivityController.isNetworkAvailable(SignUpActivity.this))
                        callRegisterApi(authType, socialId, password, userName, userEmail, "", userPicUrl);
                    else
                        AndroidUtils.showToast(getApplicationContext(), getResources().getString(R.string.no_internet));

                }
              /*  else if (response.code() == 400) {
                    Gson gson = new Gson();
                    try {
                        ErrorResponse errorResponse = gson.fromJson(
                                response.errorBody().string(), ErrorResponse.class);
                        if (errorResponse != null && errorResponse.getMessage() != null && !errorResponse.getMessage().isEmpty())
                            AndroidUtils.showToast(SignUpActivity.this, errorResponse.getMessage());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } */
                else if (response.code() == 500) {
                    AndroidUtils.showToast(SignUpActivity.this, getResources().getString(R.string.some_went_wrong));
                }

            }

            @Override
            public void onFailure(Call<GetLoginResponse> call, Throwable t) {
                AndroidUtils.showToast(SignUpActivity.this, getResources().getString(R.string.some_went_wrong));
                progressDialog.dismiss();
            }
        });
    }
}
