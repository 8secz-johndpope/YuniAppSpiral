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
import android.widget.TextView;

import com.android.yuniapp.R;
import com.android.yuniapp.adapter.EntitiesDetailsAdapter;
import com.android.yuniapp.model.EntityModel;
import com.android.yuniapp.model.GetEntityDetailsResponse;
import com.android.yuniapp.rest.ApiClient;
import com.android.yuniapp.rest.ApiInterface;
import com.android.yuniapp.utils.AndroidUtils;
import com.android.yuniapp.utils.AppUtils;
import com.android.yuniapp.utils.ConnectivityController;
import com.android.yuniapp.utils.ConstantUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailPageActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView txtEntityName, txtStartDate, txtFinishDate, txtPriority, txtEntityType;
    private ImageView imgEntityType;
    private RecyclerView recyclerViewPlanets;
    private EntitiesDetailsAdapter entitiesDetailsAdapter;
    private ProgressBar progressBar;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String ENTITY_ID, ENTITY_TYPE;
    private ArrayList<EntityModel> entityArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailpage);
        sharedPreferences = getSharedPreferences(ConstantUtils.APP_PREF, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        initViews();
        setOnClick();

        if (getIntent() != null) {
            if (getIntent().getStringExtra(ConstantUtils.ENTITY_TYPE) != null && getIntent().getStringExtra(ConstantUtils.ENTITY_TYPE).equalsIgnoreCase("S")) {
                AppUtils.setToolbarWithBothIcon(this, "Detail", "(Star Page)", R.drawable.back_icon, 0, 0, 0);
                ENTITY_ID = getIntent().getStringExtra(ConstantUtils.ENTITY_ID);
                ENTITY_TYPE = getIntent().getStringExtra(ConstantUtils.ENTITY_TYPE);
                imgEntityType.setImageResource(R.drawable.middle_planet);
                txtEntityType.setText("Planets");
            } else if (getIntent().getStringExtra(ConstantUtils.ENTITY_TYPE) != null && getIntent().getStringExtra(ConstantUtils.ENTITY_TYPE).equalsIgnoreCase("P")) {
                AppUtils.setToolbarWithBothIcon(this, "Detail", "(Planet Page)", R.drawable.back_icon, 0, 0, 0);
                ENTITY_ID = getIntent().getStringExtra(ConstantUtils.ENTITY_ID);
                ENTITY_TYPE = getIntent().getStringExtra(ConstantUtils.ENTITY_TYPE);
                imgEntityType.setImageResource(R.drawable.middle_moon);
                txtEntityType.setText("Moons");
            } else if (getIntent().getStringExtra(ConstantUtils.ENTITY_TYPE) != null && getIntent().getStringExtra(ConstantUtils.ENTITY_TYPE).equalsIgnoreCase("M")) {
                AppUtils.setToolbarWithBothIcon(this, "Detail", "(Moon Page)", R.drawable.back_icon, 0, 0, 0);
                ENTITY_ID = getIntent().getStringExtra(ConstantUtils.ENTITY_ID);
                ENTITY_TYPE = getIntent().getStringExtra(ConstantUtils.ENTITY_TYPE);
                /*imgEntityType.setImageResource(R.drawable.middle_);
                txtEntityType.setText("Planets");*/
            } else if (getIntent().getStringExtra(ConstantUtils.ENTITY_TYPE) != null && getIntent().getStringExtra(ConstantUtils.ENTITY_TYPE).equalsIgnoreCase("T")) {
                AppUtils.setToolbarWithBothIcon(this, "Detail", "(Satellite Page)", R.drawable.back_icon, 0, 0, 0);
                ENTITY_ID = getIntent().getStringExtra(ConstantUtils.ENTITY_ID);
                ENTITY_TYPE = getIntent().getStringExtra(ConstantUtils.ENTITY_TYPE);
            } else if (getIntent().getStringExtra(ConstantUtils.ENTITY_TYPE) != null && getIntent().getStringExtra(ConstantUtils.ENTITY_TYPE).equalsIgnoreCase("C")) {
                AppUtils.setToolbarWithBothIcon(this, "Detail", "(Comet Page)", R.drawable.back_icon, 0, 0, 0);
                ENTITY_ID = getIntent().getStringExtra(ConstantUtils.ENTITY_ID);
                ENTITY_TYPE = getIntent().getStringExtra(ConstantUtils.ENTITY_TYPE);
            }
        }


        recyclerViewPlanets.setLayoutManager(new LinearLayoutManager(this));
        entitiesDetailsAdapter = new EntitiesDetailsAdapter(this, entityArrayList);
        recyclerViewPlanets.setAdapter(entitiesDetailsAdapter);

        if (ConnectivityController.isNetworkAvailable(this))
            callGetEntityDetailsApi();
        else AndroidUtils.showToast(this, getResources().getString(R.string.no_internet));

    }


    private void initViews() {
        progressBar = findViewById(R.id.progress_bar);
        txtEntityName = findViewById(R.id.txt_entity_name);
        txtStartDate = findViewById(R.id.txt_start_date);
        txtFinishDate = findViewById(R.id.txt_finish_date);
        txtPriority = findViewById(R.id.txt_priority);
        recyclerViewPlanets = findViewById(R.id.recycler_planets_list);
        imgEntityType = findViewById(R.id.img_entity_type);
        txtEntityType = findViewById(R.id.txt_entity_type);
    }

    private void setOnClick() {
        findViewById(R.id.txt_documents).setOnClickListener(this);
        findViewById(R.id.txt_notes).setOnClickListener(this);
        findViewById(R.id.txt_members).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_lft_most_img:
                onBackPressed();
                break;
            case R.id.txt_documents:
                break;
            case R.id.txt_notes:
                break;
            case R.id.txt_members:
                break;
        }
    }


    private void callGetEntityDetailsApi() {
        progressBar.setVisibility(View.VISIBLE);
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetEntityDetailsResponse> getEntityDetailsResponseCall = apiService.getEntityDetails(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), ENTITY_ID, ENTITY_TYPE);
        getEntityDetailsResponseCall.enqueue(new Callback<GetEntityDetailsResponse>() {
            @Override
            public void onResponse(Call<GetEntityDetailsResponse> call, Response<GetEntityDetailsResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.code() == 200 && response.body() != null) {
                    if (response.body().getStorage() != null) {
                        if (response.body().getStorage().getName() != null)
                            txtEntityName.setText(response.body().getStorage().getName());

                        if (response.body().getStorage().getStart_date() != null) {

                            try {

                                txtStartDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(response.body().getStorage().getStart_date())));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        if (response.body().getStorage().getFinish_date() != null) {

                            try {

                                txtFinishDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(response.body().getStorage().getFinish_date())));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }


                        if (response.body().getStorage().getPriority() != null)
                            txtPriority.setText("( Priority " + response.body().getStorage().getPriority() + " )");


                        if (response.body().getStorage().getEntities() != null && response.body().getStorage().getEntities().size() > 0) {
                            imgEntityType.setVisibility(View.VISIBLE);
                            txtEntityType.setVisibility(View.VISIBLE);
                            entityArrayList.clear();
                            entityArrayList.addAll(response.body().getStorage().getEntities());
                            entitiesDetailsAdapter.notifyDataSetChanged();
                        } else {
                            imgEntityType.setVisibility(View.GONE);
                            txtEntityType.setVisibility(View.GONE);
                        }

                    }

                } else if (response.code() == 500)
                    AndroidUtils.showToast(DetailPageActivity.this, getResources().getString(R.string.some_went_wrong));

            }

            @Override
            public void onFailure(Call<GetEntityDetailsResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                AndroidUtils.showToast(DetailPageActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });

    }
}
