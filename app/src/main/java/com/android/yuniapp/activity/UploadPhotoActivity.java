package com.android.yuniapp.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.EditText;

import com.android.yuniapp.R;
import com.android.yuniapp.fragment.GenderSelectionFragment;
import com.android.yuniapp.listener.ItemClickListener;
import com.android.yuniapp.model.GetLoginResponse;
import com.android.yuniapp.rest.ApiClient;
import com.android.yuniapp.rest.ApiInterface;
import com.android.yuniapp.utils.AndroidUtils;
import com.android.yuniapp.utils.AppUtils;
import com.android.yuniapp.utils.ConnectivityController;
import com.android.yuniapp.utils.ConstantUtils;
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

public class UploadPhotoActivity extends AppCompatActivity implements View.OnClickListener, ItemClickListener {
    private EditText edtDob, edtGender;
    private RoundedImageView imgUser;
    private Date dateObject;
    private String callFormattedDate = "", gender = "", imagePath = "";
    private Dialog progressDialog;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private MultipartBody.Part bodyUserProfile;
    private RequestBody requestBodyName, requestBodyDob, requestBodyGender, requestBodyAdd,requestBodyAutomaticQuote,requestBodyOwnQuote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_photo);

        AppUtils.setFullScreen(this);

        sharedPreferences = getSharedPreferences(ConstantUtils.APP_PREF, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        progressDialog = new Dialog(this);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setContentView(R.layout.fragment_loader);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        initViews();
        setOnClick();
        setOnTouchListener();


        if (sharedPreferences.getString(ConstantUtils.USER_NAME, "") != null)
            requestBodyName = RequestBody.create(MediaType.parse("text/plain"), sharedPreferences.getString(ConstantUtils.USER_NAME, ""));

        if (sharedPreferences.getString(ConstantUtils.ADDRESS, "") != null)
            requestBodyAdd = RequestBody.create(MediaType.parse("text/plain"), sharedPreferences.getString(ConstantUtils.ADDRESS, ""));
    }


    private void initViews() {
        edtDob = findViewById(R.id.edt_dob);
        edtGender = findViewById(R.id.edt_gender);
        imgUser = findViewById(R.id.img_user);
    }


    private void setOnClick() {
        findViewById(R.id.btn_done).setOnClickListener(this);
        findViewById(R.id.txt_skip).setOnClickListener(this);
        imgUser.setOnClickListener(this);
    }

    private void setOnTouchListener() {
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

                    DatePickerDialog datePickerDialog = new DatePickerDialog(UploadPhotoActivity.this, new DatePickerDialog.OnDateSetListener() {
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
                    myFragment.setListener(UploadPhotoActivity.this);
                    myFragment.show(getSupportFragmentManager(), myFragment.getTag());
                }
                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.btn_done:

                if(edtDob.getText().toString().isEmpty() && edtGender.getText().toString().isEmpty() && imagePath.isEmpty())
                {
                    intent = new Intent(UploadPhotoActivity.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }else {
                    if (imagePath != null && !imagePath.isEmpty()) {
                        RequestBody requestFile = RequestBody.create(MediaType.parse(AndroidUtils.getMimeType(imagePath)), new File(imagePath));
                        bodyUserProfile = MultipartBody.Part.createFormData("profile_pic", new File(imagePath).getName(), requestFile);
                    }

                    requestBodyDob = RequestBody.create(MediaType.parse("text/plain"), callFormattedDate);

                    requestBodyGender = RequestBody.create(MediaType.parse("text/plain"), gender);
                    requestBodyAutomaticQuote=RequestBody.create(MediaType.parse("text/plain"), "1");
                    requestBodyOwnQuote=RequestBody.create(MediaType.parse("text/plain"),"");

                    if (ConnectivityController.isNetworkAvailable(UploadPhotoActivity.this))
                        callUpdatProfileApi();
                    else
                        AndroidUtils.showToast(UploadPhotoActivity.this, getResources().getString(R.string.no_internet));
                }

                break;
            case R.id.txt_skip:
                intent = new Intent(UploadPhotoActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                break;
            case R.id.img_user:
                CropImage.activity()
                        .setAllowFlipping(false)
                        .setAllowRotation(false)
                        .setOutputCompressQuality(50)
                        .start(UploadPhotoActivity.this, true, true);
                break;
        }
    }

    private void callUpdatProfileApi() {
        progressDialog.show();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);

        Call<GetLoginResponse> updateProfileResponse = apiService.updateProfile(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), requestBodyName, bodyUserProfile, requestBodyDob, requestBodyAdd, requestBodyGender,requestBodyOwnQuote,requestBodyAutomaticQuote);
        updateProfileResponse.enqueue(new Callback<GetLoginResponse>() {
            @Override
            public void onResponse(Call<GetLoginResponse> call, Response<GetLoginResponse> response) {
                progressDialog.dismiss();
                if (response.code() == 200 && response.body() != null) {

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

                    if (response.body().getProfile_pic() != null) {
                        editor.putString(ConstantUtils.USER_PROFILE, response.body().getProfile_pic());
                        Picasso.with(UploadPhotoActivity.this).load(ConstantUtils.BASE_URL + response.body().getProfile_pic()).placeholder(R.drawable.add_img).into(imgUser);
                    }

                    editor.commit();

                    Intent intent = new Intent(UploadPhotoActivity.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }

            @Override
            public void onFailure(Call<GetLoginResponse> call, Throwable t) {
                progressDialog.dismiss();
                AndroidUtils.showToast(UploadPhotoActivity.this, getResources().getString(R.string.some_went_wrong));
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
                    Picasso.with(UploadPhotoActivity.this).load(resultUri).placeholder(R.drawable.upload_img).into(imgUser);

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
