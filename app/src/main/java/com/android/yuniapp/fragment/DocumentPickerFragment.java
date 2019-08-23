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
import com.android.yuniapp.activity.AddTaskActivity;
import com.android.yuniapp.activity.ProfileActivity;
import com.theartofdev.edmodo.cropper.CropImage;
import com.vincent.filepicker.Constant;
import com.vincent.filepicker.activity.NormalFilePickActivity;

import static com.vincent.filepicker.activity.BaseActivity.IS_NEED_FOLDER_LIST;


public class DocumentPickerFragment extends DialogFragment implements View.OnClickListener {
    private RelativeLayout layoutPdf,layoutPhoto;
    private final int REQUEST_WRITE_STORAGE = 1001;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.fragment_document_picker, container, false);
       view.findViewById(R.id.layout_pdf).setOnClickListener(this);
       view.findViewById(R.id.layout_photo).setOnClickListener(this);
       view.findViewById(R.id.txt_cancel).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
      switch (v.getId())
      {
          case R.id.layout_pdf:
              if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                  ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                          REQUEST_WRITE_STORAGE);
              } else {

                  Intent intent4 = new Intent(getActivity(), NormalFilePickActivity.class);
                  intent4.putExtra(Constant.MAX_NUMBER, 1);
                  intent4.putExtra(IS_NEED_FOLDER_LIST, false);
                  intent4.putExtra(NormalFilePickActivity.SUFFIX,
                          new String[] {/*"doc", "docX",*/"pdf"});
                  getActivity().startActivityForResult(intent4, Constant.REQUEST_CODE_PICK_FILE);

              }
              dismiss();
              break;
          case R.id.layout_photo:
              CropImage.activity()
                      .setAllowFlipping(false)
                      .setAllowRotation(false)
                      .setOutputCompressQuality(50)
                      .start(getActivity(), true, true);
              dismiss();
              break;
          case R.id.txt_cancel:
              dismiss();
              break;
      }
    }
}
