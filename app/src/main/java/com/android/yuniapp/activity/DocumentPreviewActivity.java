package com.android.yuniapp.activity;

import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.yuniapp.R;
import com.android.yuniapp.model.DocumentResponseModel;
import com.android.yuniapp.utils.AndroidUtils;
import com.android.yuniapp.utils.AppUtils;
import com.android.yuniapp.utils.ConstantUtils;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnDrawListener;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.listener.OnRenderListener;
import com.github.barteksc.pdfviewer.listener.OnTapListener;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class DocumentPreviewActivity extends AppCompatActivity implements View.OnClickListener {
    private DocumentResponseModel documentResponseModel;
    private WebView webView;
    private ImageView imgDoc;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_preview);

        webView = findViewById(R.id.webView);
        imgDoc = findViewById(R.id.img_doc);
        progressBar = findViewById(R.id.loading);
        AppUtils.setToolbarWithBothIcon(this, "", "", R.drawable.back_icon, 0, 0, 0);

        webView.getSettings().setJavaScriptEnabled(true);

        if (getIntent() != null) {
            if (getIntent().getParcelableExtra(ConstantUtils.DOCUMENT_MODEL) != null) {
                documentResponseModel = getIntent().getParcelableExtra(ConstantUtils.DOCUMENT_MODEL);

                if (documentResponseModel.getDocument_type().equalsIgnoreCase("I")) {
                    progressBar.setVisibility(View.GONE);
                    imgDoc.setVisibility(View.VISIBLE);
                    webView.setVisibility(View.GONE);
                    Picasso.with(DocumentPreviewActivity.this).load(ConstantUtils.BASE_URL + documentResponseModel.getDocument_url()).placeholder(R.drawable.profile_placeholder).into(imgDoc);

                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    imgDoc.setVisibility(View.GONE);
                    webView.setVisibility(View.VISIBLE);
                    String url = "https://docs.google.com/gview?embedded=true&url=" + ConstantUtils.BASE_URL + documentResponseModel.getDocument_url();
                    AndroidUtils.openWebView(this, webView, url, progressBar);
                }


            }
        }

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
