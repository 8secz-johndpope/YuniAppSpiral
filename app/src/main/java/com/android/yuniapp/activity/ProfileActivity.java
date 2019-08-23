package com.android.yuniapp.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.yuniapp.R;
import com.android.yuniapp.application.BaseActivity;
import com.android.yuniapp.fcm.DeleteFirebaseInstanceIdService;
import com.android.yuniapp.fragment.GenderSelectionFragment;
import com.android.yuniapp.listener.ItemClickListener;
import com.android.yuniapp.model.GetLoginResponse;
import com.android.yuniapp.rest.ApiClient;
import com.android.yuniapp.rest.ApiInterface;
import com.android.yuniapp.utils.AndroidUtils;
import com.android.yuniapp.utils.AppUtils;
import com.android.yuniapp.utils.ConnectivityController;
import com.android.yuniapp.utils.ConstantUtils;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.JsonElement;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends BaseActivity implements View.OnClickListener, ItemClickListener {
    private EditText edtDob, edtGender, edtAddress, edtQwnQuote, edtUserName;
    private SwitchCompat switchAutomaticQuote;
    private TextView txtLogout, txtEmail;
    private RoundedImageView imgProfile;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private LinearLayout layoutQuote;
    private Date dateObject;
    private String callFormattedDate = "", gender = "", imagePath = "", imageUrl = "", automaticQuote = "1";
    private GoogleSignInClient mGoogleSignInClient;
    private MultipartBody.Part bodyUserProfile;
    private RequestBody requestBodyName, requestBodyDob, requestBodyGender, requestBodyAdd, requestBodyAutomaticQuote, requestBodyOwnQuote;
    private Dialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sharedPreferences = getSharedPreferences(ConstantUtils.APP_PREF, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        progressDialog = new Dialog(this);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setContentView(R.layout.fragment_loader);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        //AppUtils.setToolbarWithBothIcon(this, "Profile", "", R.drawable.home_icon, 0, R.drawable.edit_icon_copy,R.drawable.more_icon);
        initViews();
        setOnClick();


        switchAutomaticQuote.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AppUtils.setToolbarWithBothIcon(ProfileActivity.this, "Profile", "", R.drawable.home_icon, R.drawable.upload_photos_icon, R.drawable.green_check, R.drawable.more_icon);
                edtQwnQuote.setEnabled(true);
                if (isChecked) {
                    layoutQuote.setVisibility(View.GONE);
                    automaticQuote = "1";
                } else {
                    automaticQuote = "0";
                    layoutQuote.setVisibility(View.VISIBLE);
                }
            }
        });

        setData();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        AppUtils.setToolbarWithBothIcon(ProfileActivity.this, "Profile", "", R.drawable.home_icon, R.drawable.upload_photos_icon, R.drawable.edit_icon_copy, R.drawable.more_icon);

    }


    private void initViews() {
        imgProfile = findViewById(R.id.img_profile);
        edtUserName = findViewById(R.id.edt_user_name);
        txtEmail = findViewById(R.id.txt_email);
        edtDob = findViewById(R.id.edt_dob);
        edtGender = findViewById(R.id.edt_gender);
        edtAddress = findViewById(R.id.edt_address);
        edtQwnQuote = findViewById(R.id.edt_own_quote);
        switchAutomaticQuote = findViewById(R.id.switch_automatic_quote);
        txtLogout = findViewById(R.id.txt_logout);
        layoutQuote = findViewById(R.id.layout_quote);

    }

    private void setOnClick() {
        txtLogout.setOnClickListener(this);
    }

    private void setData() {

        if (sharedPreferences.getString(ConstantUtils.USER_NAME, "") != null && !sharedPreferences.getString(ConstantUtils.USER_NAME, "").isEmpty())
            edtUserName.setText(sharedPreferences.getString(ConstantUtils.USER_NAME, ""));

        if (sharedPreferences.getString(ConstantUtils.USER_EMAIL, "") != null && !sharedPreferences.getString(ConstantUtils.USER_EMAIL, "").isEmpty())
            txtEmail.setText(sharedPreferences.getString(ConstantUtils.USER_EMAIL, ""));


        if (sharedPreferences.getString(ConstantUtils.ADDRESS, "") != null && !sharedPreferences.getString(ConstantUtils.ADDRESS, "").isEmpty())
            edtAddress.setText(sharedPreferences.getString(ConstantUtils.ADDRESS, ""));

        if (sharedPreferences.getString(ConstantUtils.DOB, "") != null && !sharedPreferences.getString(ConstantUtils.DOB, "").isEmpty()) {
            if (!sharedPreferences.getString(ConstantUtils.DOB, "").equals("1970-01-01")) {
                try {
                    dateObject = new SimpleDateFormat("yyyy-MM-dd").parse(sharedPreferences.getString(ConstantUtils.DOB, ""));
                    callFormattedDate = new SimpleDateFormat("yyyy-MM-dd").format(dateObject);
                    edtDob.setText(new SimpleDateFormat("MMM dd, yyyy").format(dateObject));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

        if (sharedPreferences.getString(ConstantUtils.GENDER, "") != null && !sharedPreferences.getString(ConstantUtils.GENDER, "").isEmpty()) {
            gender = sharedPreferences.getString(ConstantUtils.GENDER, "");
            if (gender.equalsIgnoreCase("m"))
                edtGender.setText(getResources().getString(R.string.male));
            else if (gender.equalsIgnoreCase("f"))
                edtGender.setText(getResources().getString(R.string.female));

        }

        if (sharedPreferences.getString(ConstantUtils.USER_PROFILE, "") != null && !sharedPreferences.getString(ConstantUtils.USER_PROFILE, "").isEmpty()) {
            imageUrl = sharedPreferences.getString(ConstantUtils.USER_PROFILE, "");
            Picasso.with(ProfileActivity.this).load(sharedPreferences.getString(ConstantUtils.USER_PROFILE, "")).placeholder(R.drawable.profile_placeholder).into(imgProfile);
        }

        if (sharedPreferences.getString(ConstantUtils.AUTOMATIC_QUOTE, "") != null && !sharedPreferences.getString(ConstantUtils.AUTOMATIC_QUOTE, "").isEmpty()) {
            automaticQuote = sharedPreferences.getString(ConstantUtils.AUTOMATIC_QUOTE, "");
            if (automaticQuote.equals("1"))
                switchAutomaticQuote.setChecked(true);
            else switchAutomaticQuote.setChecked(false);
        }

        if (sharedPreferences.getString(ConstantUtils.OWN_QUOTE, "") != null && !sharedPreferences.getString(ConstantUtils.OWN_QUOTE, "").isEmpty()) {
            edtQwnQuote.setText(sharedPreferences.getString(ConstantUtils.OWN_QUOTE, ""));
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_lft_most_img:
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.toolbar_lft_img:
                startActivity(new Intent(ProfileActivity.this, UploadImagesActivity.class));
                break;
            case R.id.toolbar_right_img:
                ImageView toolbarRightImage;
                toolbarRightImage = findViewById(R.id.toolbar_right_img);
                if (toolbarRightImage.getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.edit_icon_copy).getConstantState())) {
                    AppUtils.setToolbarWithBothIcon(this, "Profile", "", R.drawable.home_icon, R.drawable.upload_photos_icon, R.drawable.green_check, R.drawable.more_icon);

                    imgProfile.setOnClickListener(this);
                    edtDob.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.calendar_icon, 0);
                    edtGender.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.caret_icon, 0);

                    edtDob.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if (event.ACTION_UP == event.getAction()) {
                                int mYear, mMonth, mDay;
                                Calendar c;
                                if (dateObject != null) {
                                    c = AndroidUtils.getCalendarDateObject(dateObject);
                                    mYear = c.get(Calendar.YEAR);
                                    mMonth = c.get(Calendar.MONTH);
                                    mDay = c.get(Calendar.DAY_OF_MONTH);

                                } else {
                                    c = Calendar.getInstance();
                                    mYear = c.get(Calendar.YEAR);
                                    mMonth = c.get(Calendar.MONTH);
                                    mDay = c.get(Calendar.DAY_OF_MONTH);
                                }

                                DatePickerDialog datePickerDialog = new DatePickerDialog(ProfileActivity.this, new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                        try {
                                            dateObject = new SimpleDateFormat("dd/MM/yyyy").parse(String.valueOf(dayOfMonth) + "/" + String.valueOf(month + 1) + "/" + String.valueOf(year));
                                            callFormattedDate = new SimpleDateFormat("yyyy-MM-dd").format(dateObject);
                                            edtDob.setText(new SimpleDateFormat("MMM dd, yyyy").format(dateObject));
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, mYear, mMonth, mDay);
                                Calendar cuurentCalendar = Calendar.getInstance();
                                datePickerDialog.getDatePicker().setMaxDate(cuurentCalendar.getTimeInMillis() - 1000);
                                datePickerDialog.show();
                            }
                            return true;
                        }
                    });

                    edtGender.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if (event.ACTION_UP == event.getAction()) {
                                GenderSelectionFragment myFragment = new GenderSelectionFragment();
                                myFragment.setListener(ProfileActivity.this);
                                myFragment.show(getSupportFragmentManager(), myFragment.getTag());
                            }
                            return true;
                        }
                    });
                    edtUserName.setEnabled(true);
                    edtAddress.setEnabled(true);


                    edtUserName.setBackground(this.getResources().getDrawable(R.drawable.edittext_border_default));
                    edtDob.setBackground(this.getResources().getDrawable(R.drawable.edittext_border_default));
                    edtGender.setBackground(this.getResources().getDrawable(R.drawable.edittext_border_default));
                    edtAddress.setBackground(this.getResources().getDrawable(R.drawable.edittext_border_default));
                } else {

                    if (edtUserName.getText().toString().trim().isEmpty())
                        AndroidUtils.showToast(ProfileActivity.this, edtUserName.getHint().toString() + getResources().getString(R.string.cant_empty));
                    else if (edtAddress.getText().toString().trim().isEmpty())
                        AndroidUtils.showToast(ProfileActivity.this, edtAddress.getHint().toString() + getResources().getString(R.string.cant_empty));

                    else {


                        if (imagePath != null && !imagePath.isEmpty()) {
                            RequestBody requestFile = RequestBody.create(MediaType.parse(AndroidUtils.getMimeType(imagePath)), new File(imagePath));
                            bodyUserProfile = MultipartBody.Part.createFormData("profile_pic", new File(imagePath).getName(), requestFile);
                        }

                        if (!edtUserName.getText().toString().trim().isEmpty())
                            requestBodyName = RequestBody.create(MediaType.parse("text/plain"), edtUserName.getText().toString());

                        requestBodyDob = RequestBody.create(MediaType.parse("text/plain"), callFormattedDate);

                        requestBodyGender = RequestBody.create(MediaType.parse("text/plain"), gender);
                        requestBodyAdd = RequestBody.create(MediaType.parse("text/plain"), edtAddress.getText().toString());

                        requestBodyAutomaticQuote = RequestBody.create(MediaType.parse("text/plain"), automaticQuote);
                        requestBodyOwnQuote = RequestBody.create(MediaType.parse("text/plain"), edtQwnQuote.getText().toString().trim());

                        if (ConnectivityController.isNetworkAvailable(ProfileActivity.this))
                            callUpdatProfileApi();
                        else
                            AndroidUtils.showToast(ProfileActivity.this, getResources().getString(R.string.no_internet));


                    }
                }

                break;
            case R.id.txt_logout:

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ProfileActivity.this);
                alertDialog.setMessage(getResources().getString(R.string.dialog_title));

                alertDialog.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        updateDeviceToken();

                        String oldQuote = "";
                        oldQuote = sharedPreferences.getString(ConstantUtils.OWN_QUOTE, "");
                        editor.clear().apply();
                        editor.putString(ConstantUtils.OWN_QUOTE, oldQuote).commit();
                        startService(new Intent(ProfileActivity.this, DeleteFirebaseInstanceIdService.class));

                        LoginManager.getInstance().logOut();
                        mGoogleSignInClient.signOut()
                                .addOnCompleteListener(ProfileActivity.this, new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                    }
                                });
                        Intent intent = new Intent(new Intent(ProfileActivity.this, GetStartedActivity.class));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    }
                });

                alertDialog.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialog.setCancelable(false);
                alertDialog.show();
                break;
            case R.id.img_profile:
                CropImage.activity()
                        .setAllowFlipping(false)
                        .setAllowRotation(false)
                        .setOutputCompressQuality(50)
                        .start(ProfileActivity.this, true, true);
                break;
            case R.id.toolbar_right_most_img:
                startActivity(new Intent(ProfileActivity.this, MoreActivity.class));
                break;

        }
    }

    private void updateDeviceToken() {
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        apiService.updateDeviceDetails(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), "A", "none")
                .enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {

                    }

                    @Override
                    public void onFailure(Call<JsonElement> call, Throwable t) {

                    }
                });

    }

    private void callUpdatProfileApi() {
        progressDialog.show();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetLoginResponse> updateProfileResponse;

        if (imageUrl.isEmpty()) {
            updateProfileResponse = apiService.updateProfile(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), requestBodyName, bodyUserProfile, requestBodyDob, requestBodyAdd, requestBodyGender, requestBodyOwnQuote, requestBodyAutomaticQuote);
        } else {
            updateProfileResponse = apiService.updateProfileWithoutMultipart(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), edtUserName.getText().toString(), imageUrl, callFormattedDate, edtAddress.getText().toString(), gender, edtQwnQuote.getText().toString().trim(), automaticQuote);
        }

        updateProfileResponse.enqueue(new Callback<GetLoginResponse>() {
            @Override
            public void onResponse(Call<GetLoginResponse> call, Response<GetLoginResponse> response) {
                progressDialog.dismiss();
                if (response.code() == 200 && response.body() != null) {
                    AndroidUtils.showToast(ProfileActivity.this, "Profile has been updated successfully.");

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

                    if (response.body().getProfile_pic() != null && !response.body().getProfile_pic().isEmpty()) {
                        editor.putString(ConstantUtils.USER_PROFILE, response.body().getProfile_pic());
                        Picasso.with(ProfileActivity.this).load(response.body().getProfile_pic()).placeholder(R.drawable.profile_placeholder).into(imgProfile);
                    }


                    if (response.body().getAutomatic_quote() != null)
                        editor.putString(ConstantUtils.AUTOMATIC_QUOTE, response.body().getAutomatic_quote());

                    if (response.body().getOwn_quote() != null)
                        editor.putString(ConstantUtils.OWN_QUOTE, response.body().getOwn_quote());

                    editor.commit();

                    AppUtils.setToolbarWithBothIcon(ProfileActivity.this, "Profile", "", R.drawable.home_icon, R.drawable.upload_photos_icon, R.drawable.edit_icon_copy, R.drawable.more_icon);
                    imgProfile.setOnClickListener(null);
                    edtDob.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    edtGender.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    edtDob.setOnTouchListener(null);
                    edtGender.setOnTouchListener(null);

                    edtUserName.setEnabled(false);
                    edtAddress.setEnabled(false);
                    edtDob.setBackground(null);
                    edtGender.setBackground(null);
                    edtAddress.setBackground(null);
                    edtUserName.setBackground(null);
                }
            }

            @Override
            public void onFailure(Call<GetLoginResponse> call, Throwable t) {
                progressDialog.dismiss();
                AndroidUtils.showToast(ProfileActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                if (resultUri != null) {
                    imagePath = resultUri.getPath();
                    imageUrl = "";
                    Picasso.with(ProfileActivity.this).load(resultUri).placeholder(R.drawable.upload_img).into(imgProfile);

                }
            }
        }
    }

    @Override
    public void onClick(String text, String constant) {
        edtGender.setText(text);
        this.gender = constant;
    }
}
