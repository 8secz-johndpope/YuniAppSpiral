package com.android.yuniapp.activity;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.yuniapp.R;
import com.android.yuniapp.utils.AppUtils;
import com.android.yuniapp.utils.ConstantUtils;
import com.cunoraz.gifview.library.GifView;

public class GetStartedActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnLogin, btnSignup;
    private ImageView imgBottomMoon;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private TextView txtQuote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started);
        AppUtils.setFullScreen(this);
        btnLogin = findViewById(R.id.btn_login);
        btnSignup = findViewById(R.id.btn_signup);
        imgBottomMoon = findViewById(R.id.img_bottom_moon);
        txtQuote=findViewById(R.id.txt_quote);
        btnSignup.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        sharedPreferences = getSharedPreferences(ConstantUtils.APP_PREF, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        AppUtils.rotateAnimation(imgBottomMoon);
        editor.putBoolean(ConstantUtils.IS_FIRST_TIME_LAUNCH, true).commit();

        if(sharedPreferences.getString(ConstantUtils.OWN_QUOTE,"")!=null )
            txtQuote.setText(sharedPreferences.getString(ConstantUtils.OWN_QUOTE,""));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                startActivity(new Intent(this, LoginActivity.class));
                break;
            case R.id.btn_signup:
                startActivity(new Intent(this, SignUpActivity.class));

                break;
        }
    }
}
