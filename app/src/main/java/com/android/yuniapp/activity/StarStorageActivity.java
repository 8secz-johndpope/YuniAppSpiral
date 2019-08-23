package com.android.yuniapp.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.yuniapp.R;
import com.android.yuniapp.adapter.StoragePlanetAdapter;
import com.android.yuniapp.model.GetStarStorageResponse;
import com.android.yuniapp.model.PlanetStorageModel;
import com.android.yuniapp.rest.ApiClient;
import com.android.yuniapp.rest.ApiInterface;
import com.android.yuniapp.utils.AndroidUtils;
import com.android.yuniapp.utils.AppUtils;
import com.android.yuniapp.utils.ConnectivityController;
import com.android.yuniapp.utils.ConstantUtils;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StarStorageActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView imgStar, imgBottomMoon;
    private RecyclerView recyclerViewPlanet;
    private StoragePlanetAdapter storagePlanetAdapter;
    private ProgressBar progressBar;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String starId = "";
    private ArrayList<PlanetStorageModel> planetStorageList = new ArrayList<>();
    private TextView txtStar;
    private RelativeLayout layoutViews;
    private boolean isExpanded = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_star_storage);

        AppUtils.setToolbarWithBothIcon(this, "Star Storage", "", R.drawable.back_icon, 0, 0, 0);


        sharedPreferences = getSharedPreferences(ConstantUtils.APP_PREF, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        if (getIntent() != null) {
            if (getIntent().getStringExtra(ConstantUtils.STAR_ID) != null && !getIntent().getStringExtra(ConstantUtils.STAR_ID).isEmpty())
                starId = getIntent().getStringExtra(ConstantUtils.STAR_ID);
        }

        imgBottomMoon = findViewById(R.id.img_bottom_moon);
        AppUtils.rotateAnimation(imgBottomMoon);
        layoutViews = findViewById(R.id.layout_views);
        txtStar = findViewById(R.id.txt_star_name);
        imgStar = findViewById(R.id.img_star);
        progressBar = findViewById(R.id.progress_bar);
        recyclerViewPlanet = findViewById(R.id.recycler_planet);
        recyclerViewPlanet.setLayoutManager(new LinearLayoutManager(this));
        storagePlanetAdapter = new StoragePlanetAdapter(this, planetStorageList);
        recyclerViewPlanet.setAdapter(storagePlanetAdapter);
        imgStar.setOnClickListener(this);

        if (ConnectivityController.isNetworkAvailable(StarStorageActivity.this))
            callGetStarStorageApi();
        else AndroidUtils.showToast(this, getResources().getString(R.string.no_internet));


    }

    private void callGetStarStorageApi() {
        progressBar.setVisibility(View.VISIBLE);
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetStarStorageResponse> getStarStorageResponseCall = apiService.getStarStorage(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), starId);
        getStarStorageResponseCall.enqueue(new Callback<GetStarStorageResponse>() {
            @Override
            public void onResponse(Call<GetStarStorageResponse> call, Response<GetStarStorageResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.code() == 200 && response.body() != null) {
                    if (response.body().getStar_storage() != null) {
                        imgStar.setVisibility(View.VISIBLE);
                        /*if(response.body().getStar_storage().getIs_completed().equals("0"))
                            imgStar.setImageResource(R.drawable.star_orange_border);
                        else imgStar.setImageResource(R.drawable.star_green_border);*/

                        txtStar.setText(response.body().getStar_storage().getName());

                        if (response.body().getStar_storage().getPlanets() != null && response.body().getStar_storage().getPlanets().size() > 0) {
                            layoutViews.setVisibility(View.VISIBLE);
                            planetStorageList.addAll(response.body().getStar_storage().getPlanets());
                            storagePlanetAdapter.notifyDataSetChanged();
                        }
                    }

                } else if (response.code() == 500)
                    AndroidUtils.showToast(StarStorageActivity.this, getResources().getString(R.string.some_went_wrong));

            }

            @Override
            public void onFailure(Call<GetStarStorageResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                AndroidUtils.showToast(StarStorageActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_lft_most_img:
                onBackPressed();
                break;
            case R.id.img_star:
                if (isExpanded) {
                    isExpanded = false;
                    layoutViews.setVisibility(View.GONE);
                    recyclerViewPlanet.setVisibility(View.GONE);

                } else {
                    isExpanded = true;
                    if (planetStorageList != null && planetStorageList.size() > 0) {
                        layoutViews.setVisibility(View.VISIBLE);
                        recyclerViewPlanet.setVisibility(View.VISIBLE);
                    }
                }
                break;
        }
    }
}
