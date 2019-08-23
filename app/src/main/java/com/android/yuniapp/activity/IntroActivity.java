package com.android.yuniapp.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.yuniapp.R;
import com.android.yuniapp.adapter.IntroAdapter;
import com.android.yuniapp.model.IntroPagerModel;
import com.android.yuniapp.utils.AppUtils;
import com.android.yuniapp.utils.ConstantUtils;

import java.util.ArrayList;

import me.relex.circleindicator.CircleIndicator;


public class IntroActivity extends AppCompatActivity implements View.OnClickListener, ViewPager.OnPageChangeListener {
    private TextView btnNext;
    private ViewPager viewPagerIntro;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ArrayList<IntroPagerModel> introPagerModelArrayList = new ArrayList<>();
    private IntroAdapter introAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        AppUtils.setFullScreen(this);

        introPagerModelArrayList.add(new IntroPagerModel(R.drawable.onboarding_two_img, getResources().getString(R.string.first_page_title), getResources().getString(R.string.first_page_subtitle)));
        introPagerModelArrayList.add(new IntroPagerModel(R.drawable.onboarding_one_img, getResources().getString(R.string.second_page_title), getResources().getString(R.string.second_page_subtitle)));

        sharedPreferences = getSharedPreferences(ConstantUtils.APP_PREF, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        btnNext = findViewById(R.id.btn_next);
        viewPagerIntro = findViewById(R.id.intro_viewpager);
        btnNext.setOnClickListener(this);
        findViewById(R.id.txt_how_it_works).setOnClickListener(this);
        findViewById(R.id.txt_tutorial_video).setOnClickListener(this);
        introAdapter = new IntroAdapter(this, introPagerModelArrayList);
        viewPagerIntro.setAdapter(introAdapter);
        viewPagerIntro.addOnPageChangeListener(this);
        CircleIndicator indicator = findViewById(R.id.indicator);
        indicator.setViewPager(viewPagerIntro);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btn_next:
                if (viewPagerIntro.getCurrentItem() == 0)
                    viewPagerIntro.setCurrentItem(1);

                else {
                    Intent intent = new Intent(IntroActivity.this, GetStartedActivity.class);
                    //intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
                break;
            case R.id.txt_how_it_works:
                startActivity(new Intent(IntroActivity.this,HowItWorksActivity.class));
                break;
            case R.id.txt_tutorial_video:
                startActivity(new Intent(IntroActivity.this,TutorialVideoActivity.class));
                break;
        }
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {
        if (i == 0 ) {
            btnNext.setText("Next");
        } else {
            btnNext.setText("Get Start");
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }
}
