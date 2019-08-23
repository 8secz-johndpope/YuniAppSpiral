package com.android.yuniapp.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.android.yuniapp.R;
import com.android.yuniapp.utils.AppUtils;

public class HowItWorksActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_it_works);


        AppUtils.setToolbarWithBothIcon(this, getResources().getString(R.string.how_it_works), "", R.drawable.back_icon, 0, 0,0);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_lft_most_img:
                onBackPressed();
                break;

        }
    }
}
