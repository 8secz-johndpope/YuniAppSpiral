package com.android.yuniapp.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.yuniapp.R;
import com.android.yuniapp.utils.AndroidUtils;
import com.android.yuniapp.utils.AppUtils;
import com.android.yuniapp.utils.ConstantUtils;

public class WebViewActivity extends AppCompatActivity implements View.OnClickListener {
    private ProgressBar progressBar;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);



        progressBar = findViewById(R.id.loading);
        webView = findViewById(R.id.webview_layout);
        if(getIntent()!=null && getIntent().getStringExtra(ConstantUtils.SOURCE)!=null && !getIntent().getStringExtra(ConstantUtils.SOURCE).isEmpty())
        {
            if(getIntent().getStringExtra(ConstantUtils.SOURCE).equals(getResources().getString(R.string.tos)))
            {
                AppUtils.setToolbarWithBothIcon(this, getResources().getString(R.string.tos), "", R.drawable.back_icon, 0, 0,0);

                AndroidUtils.openWebView(this, webView, ConstantUtils.TERMS_OF_SERVICES_URL, progressBar);

            }else if(getIntent().getStringExtra(ConstantUtils.SOURCE).equals(getResources().getString(R.string.pp)))
            {
                AppUtils.setToolbarWithBothIcon(this, getResources().getString(R.string.pp), "", R.drawable.back_icon, 0, 0,0);

                AndroidUtils.openWebView(this, webView, ConstantUtils.PRIVACY_POLICY_URL, progressBar);

            }
        }


    }

    @Override
    public void onClick(View v) {
     switch (v.getId())
     {
         case R.id.toolbar_lft_most_img:
             onBackPressed();
             break;
     }
    }
}
