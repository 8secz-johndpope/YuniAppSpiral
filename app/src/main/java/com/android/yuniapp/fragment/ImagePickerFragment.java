package com.android.yuniapp.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.android.yuniapp.R;
import com.android.yuniapp.activity.UploadImagesActivity;
import com.erikagtierrez.multiple_media_picker.Gallery;
import com.theartofdev.edmodo.cropper.CropImage;

public class ImagePickerFragment extends DialogFragment implements View.OnClickListener {
    private final int REQUEST_WRITE_STORAGE = 1001;
    private static final int OPEN_MEDIA_PICKER = 1;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.fragment_image_picker, container, false);
       view.findViewById(R.id.layout_gallery).setOnClickListener(this);
       view.findViewById(R.id.layout_camera).setOnClickListener(this);
       view.findViewById(R.id.txt_cancel).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
      switch (v.getId())
      {
          case R.id.layout_gallery:
              if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                  ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                          REQUEST_WRITE_STORAGE);
              } else {
                  ((UploadImagesActivity)getActivity()).openMultiGalleryPicker();

              }
              dismiss();
              break;
          case R.id.layout_camera:
              CropImage.activity()
                      .setAllowFlipping(false)
                      .setAllowRotation(false)
                      .setOutputCompressQuality(50)
                      .start(getActivity(), true, false);
              dismiss();
              break;
          case R.id.txt_cancel:
              dismiss();
              break;
      }
    }
}
