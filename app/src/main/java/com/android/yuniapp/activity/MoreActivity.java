package com.android.yuniapp.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.android.yuniapp.R;
import com.android.yuniapp.utils.AndroidUtils;
import com.android.yuniapp.utils.AppUtils;
import com.android.yuniapp.utils.ConnectivityController;
import com.android.yuniapp.utils.ConstantUtils;

public class MoreActivity extends AppCompatActivity implements View.OnClickListener {
     private TextView txtVersionName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);

        AppUtils.setToolbarWithBothIcon(this, "More", "", R.drawable.back_icon, 0, 0,0);

        findViewById(R.id.txt_how_it_works).setOnClickListener(this);
        findViewById(R.id.txt_tutorial_video).setOnClickListener(this);
        findViewById(R.id.txt_tos).setOnClickListener(this);
        findViewById(R.id.txt_pp).setOnClickListener(this);
        txtVersionName=findViewById(R.id.txt_version_name);
        try {
            txtVersionName.setText("v"+AndroidUtils.getAppVersionName(this));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
     switch (v.getId())
     {
         case R.id.toolbar_lft_most_img:
             onBackPressed();
             break;
         case R.id.txt_how_it_works:
             startActivity(new Intent(MoreActivity.this, HowItWorksActivity.class));
             break;
         case R.id.txt_tutorial_video:
             startActivity(new Intent(MoreActivity.this, TutorialVideoActivity.class));
             break;
         case R.id.txt_tos:
             if (ConnectivityController.isNetworkAvailable(MoreActivity.this)) {

                 Intent intent = new Intent(MoreActivity.this, WebViewActivity.class);
                 intent.putExtra(ConstantUtils.SOURCE, getResources().getString(R.string.tos));
                 startActivity(intent);

             } else {
                 AndroidUtils.showToast(MoreActivity.this, getResources().getString(R.string.no_internet));
             }
             break;
         case R.id.txt_pp:
             if (ConnectivityController.isNetworkAvailable(MoreActivity.this)) {

                 Intent intent = new Intent(MoreActivity.this, WebViewActivity.class);
                 intent.putExtra(ConstantUtils.SOURCE, getResources().getString(R.string.pp));
                 startActivity(intent);

             } else {
                 AndroidUtils.showToast(MoreActivity.this, getResources().getString(R.string.no_internet));
             }
             break;

     }
    }
}
