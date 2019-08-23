package com.android.yuniapp.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;

import com.android.yuniapp.R;
import com.android.yuniapp.adapter.UploadImageAdapter;
import com.android.yuniapp.adapter.UserImagePagerAdapter;
import com.android.yuniapp.fragment.ImagePickerFragment;
import com.android.yuniapp.listener.RecyclerItemClickListener;
import com.android.yuniapp.listener.UserImageRecyclerListener;
import com.android.yuniapp.model.GetMessageModel;
import com.android.yuniapp.model.GetUserImagesResponse;
import com.android.yuniapp.model.UserImages;
import com.android.yuniapp.rest.ApiClient;
import com.android.yuniapp.rest.ApiInterface;
import com.android.yuniapp.utils.AndroidUtils;
import com.android.yuniapp.utils.AppUtils;
import com.android.yuniapp.utils.ConnectivityController;
import com.android.yuniapp.utils.ConstantUtils;
import com.erikagtierrez.multiple_media_picker.Gallery;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadImagesActivity extends AppCompatActivity implements View.OnClickListener, RecyclerItemClickListener, UserImageRecyclerListener {
    private RecyclerView recyclerViewImages;
    private UploadImageAdapter uploadImageAdapter;
    private ProgressBar progressBar;
    private ArrayList<UserImages> userImagesArrayList;
    private ArrayList<UserImages> userImagesArrayListPager=new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private final int REQUEST_WRITE_STORAGE = 1001;
    private static final int OPEN_MEDIA_PICKER = 1;
    private Dialog progressDialog;
    private List<MultipartBody.Part> parts = new ArrayList<>();
    private ViewPager imagesViewPager;
    private UserImagePagerAdapter userImagePagerAdapter;
    private Button btnCancel;
    private View viewDim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uplod_images);

        sharedPreferences = getSharedPreferences(ConstantUtils.APP_PREF, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        AppUtils.setToolbarWithBothIcon(UploadImagesActivity.this, "", "", R.drawable.back_icon, 0, 0, 0);

        progressDialog = new Dialog(this);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setContentView(R.layout.fragment_loader);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        userImagesArrayList=new ArrayList<>();
        userImagesArrayList.add(new UserImages());

        viewDim=findViewById(R.id.view_dim);
        btnCancel=findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(this);
        imagesViewPager=findViewById(R.id.images_pager);
        progressBar = findViewById(R.id.progress_bar);
        recyclerViewImages = findViewById(R.id.recycler_images);
        recyclerViewImages.setLayoutManager(new GridLayoutManager(this, 3));

        uploadImageAdapter = new UploadImageAdapter(this, userImagesArrayList, this);
        recyclerViewImages.setAdapter(uploadImageAdapter);

        if (ConnectivityController.isNetworkAvailable(this))
            callGetUserImagesApi();
        else AndroidUtils.showToast(this, getResources().getString(R.string.no_internet));

        imagesViewPager.setClipToPadding(false);
        imagesViewPager.setPadding(80, 0, 80, 0);
        imagesViewPager.setPageMargin(40);
        userImagePagerAdapter =new UserImagePagerAdapter(UploadImagesActivity.this,userImagesArrayListPager);
        imagesViewPager.setAdapter(userImagePagerAdapter);


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_lft_most_img:
                onBackPressed();
                break;
            case R.id.btn_cancel:
                imagesViewPager.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
                viewDim.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onItemClick(int position) {
        if(position==0) {
            if(userImagesArrayList.size()<11) {
                ImagePickerFragment imagePickerFragment = new ImagePickerFragment();
                imagePickerFragment.show(getSupportFragmentManager(), imagePickerFragment.getTag());
            }else AndroidUtils.showToast(UploadImagesActivity.this,"You can upload maximum 10 images");
            }else {
           imagesViewPager.setVisibility(View.VISIBLE);
           btnCancel.setVisibility(View.VISIBLE);

           imagesViewPager.setCurrentItem(position-1);

            viewDim.setVisibility(View.VISIBLE);
        }
    }


    public  void openMultiGalleryPicker()
    {
        Intent intent = new Intent(UploadImagesActivity.this, Gallery.class);
        intent.putExtra("title", "Select Images");
        // Mode 1 for both images and videos selection, 2 for images only and 3 for videos!
        intent.putExtra("mode", 2);
        intent.putExtra("maxSelection", 11-userImagesArrayList.size()); // Optional
        startActivityForResult(intent, OPEN_MEDIA_PICKER);
    }

    @Override
    public void onItemLongClick(final int position) {
       if(position!=0)
       {
           AlertDialog.Builder alertDialog = new AlertDialog.Builder(UploadImagesActivity.this);
           alertDialog.setMessage(getResources().getString(R.string.dialog_delete_title));

           alertDialog.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int which) {
                  if(ConnectivityController.isNetworkAvailable(UploadImagesActivity.this))
                      callDeleteUserImageApi(position);
                  else AndroidUtils.showToast(UploadImagesActivity.this,getResources().getString(R.string.no_internet));

               }
           });

           alertDialog.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int which) {
                   dialog.cancel();
               }
           });
           alertDialog.setCancelable(false);
           alertDialog.show();
       }
    }

    private void callDeleteUserImageApi(final int position) {
        progressDialog.show();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetMessageModel> deleteUserImageResponseCall=apiService.callDeleteUserImage(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN,""),userImagesArrayList.get(position).getId(),userImagesArrayList.get(position).getImage_url());
        deleteUserImageResponseCall.enqueue(new Callback<GetMessageModel>() {
            @Override
            public void onResponse(Call<GetMessageModel> call, Response<GetMessageModel> response) {
                progressDialog.dismiss();
                if(response.code()==200 && response.body()!=null)
                {
                    if(response.body().getMessage()!=null)
                        AndroidUtils.showToast(UploadImagesActivity.this,response.body().getMessage());

                    userImagesArrayList.remove(position);
                    uploadImageAdapter.notifyItemRemoved(position);
                    uploadImageAdapter.notifyItemRangeChanged(position,userImagesArrayList.size());

                    userImagesArrayListPager.remove(position-1);
                    userImagePagerAdapter.notifyDataSetChanged();
                }else if (response.code() == 500) {
                    AndroidUtils.showToast(UploadImagesActivity.this, getResources().getString(R.string.some_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<GetMessageModel> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                AndroidUtils.showToast(UploadImagesActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });

    }


    private void callGetUserImagesApi() {
        progressBar.setVisibility(View.VISIBLE);
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetUserImagesResponse> getUserImagesResponseCall = apiService.callGetUserImages(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""));
        getUserImagesResponseCall.enqueue(new Callback<GetUserImagesResponse>() {
            @Override
            public void onResponse(Call<GetUserImagesResponse> call, Response<GetUserImagesResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.code() == 200 && response.body() != null) {
                    if (response.body().getImages() != null && response.body().getImages().size() > 0) {
                        userImagesArrayList.addAll(response.body().getImages());
                        uploadImageAdapter.notifyDataSetChanged();

                        userImagesArrayListPager.addAll(response.body().getImages());
                        userImagePagerAdapter.notifyDataSetChanged();



                    }

                } else if (response.code() == 500) {
                    AndroidUtils.showToast(UploadImagesActivity.this, getResources().getString(R.string.some_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<GetUserImagesResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                AndroidUtils.showToast(UploadImagesActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(UploadImagesActivity.this, Gallery.class);
                intent.putExtra("title", "Select Images");
                // Mode 1 for both images and videos selection, 2 for images only and 3 for videos!
                intent.putExtra("mode", 2);
                intent.putExtra("maxSelection", 11-userImagesArrayList.size()); // Optional
                startActivityForResult(intent, OPEN_MEDIA_PICKER);
            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // user rejected the permission
                if (!ActivityCompat.shouldShowRequestPermissionRationale(UploadImagesActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    AndroidUtils.showLongToast(UploadImagesActivity.this, "Go to Settings and Grant the permission to use this feature.");

                    // user also CHECKED "never ask again"
                    // you can either enable some fall back,
                    // disable features of your app
                    // or open another dialog explaining
                    // again the permission and directing to
                    // the app setting
                } else {

                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_WRITE_STORAGE);

                    // user did NOT check "never ask again"
                    // this is a good place to explain the user
                    // why you need the permission and ask if he wants
                    // to accept it (the rationale)
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    Uri resultUri = result.getUri();
                    if (resultUri != null) {
                        MultipartBody.Part part;
                        RequestBody requestFile = RequestBody.create(MediaType.parse(AndroidUtils.getMimeType(resultUri.getPath())), new File(resultUri.getPath()));
                        part = MultipartBody.Part.createFormData("images[0]", new File(resultUri.getPath()).getName(), requestFile);
                         parts.clear();
                         parts.add(part);

                        if(ConnectivityController.isNetworkAvailable(UploadImagesActivity.this))
                            callUploadUserImages();
                        else AndroidUtils.showToast(UploadImagesActivity.this,getResources().getString(R.string.no_internet));

                    }
                }
                break;
            case OPEN_MEDIA_PICKER:
                if (resultCode == RESULT_OK && data!=null) {
                    ArrayList<String> selectionResult = data.getStringArrayListExtra("result");
                    MultipartBody.Part part;
                    parts.clear();
                    for (int i=0;i<selectionResult.size();i++)
                    {
                        RequestBody requestFile = RequestBody.create(MediaType.parse(AndroidUtils.getMimeType(selectionResult.get(i))), new File(selectionResult.get(i)));
                        part = MultipartBody.Part.createFormData("images["+i+"]", new File(selectionResult.get(i)).getName(), requestFile);
                        parts.add(part);
                    }

                    if(ConnectivityController.isNetworkAvailable(UploadImagesActivity.this))
                        callUploadUserImages();
                    else AndroidUtils.showToast(UploadImagesActivity.this,getResources().getString(R.string.no_internet));
                }
                break;
        }
    }


    private void callUploadUserImages() {
        progressDialog.show();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetUserImagesResponse> getUserImagesResponseCall = apiService.callUploadUserImages(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN,""),parts);
        getUserImagesResponseCall.enqueue(new Callback<GetUserImagesResponse>() {
            @Override
            public void onResponse(Call<GetUserImagesResponse> call, Response<GetUserImagesResponse> response) {
                progressDialog.dismiss();

                if (response.code() == 200 && response.body() != null) {

                    if(response.body().getImages()!=null && response.body().getImages().size()>0) {
                        userImagesArrayList.clear();
                        userImagesArrayList.add(new UserImages());
                        userImagesArrayList.addAll(response.body().getImages());
                        uploadImageAdapter.notifyDataSetChanged();

                        userImagesArrayListPager.clear();
                        userImagesArrayListPager.addAll(response.body().getImages());
                        userImagePagerAdapter.notifyDataSetChanged();
                    }
                } else if (response.code() == 500) {
                    AndroidUtils.showToast(UploadImagesActivity.this, getResources().getString(R.string.some_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<GetUserImagesResponse> call, Throwable t) {
                progressDialog.dismiss();
                AndroidUtils.showToast(UploadImagesActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });

    }
}
