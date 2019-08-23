package com.android.yuniapp.activity;

import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.yuniapp.R;
import com.android.yuniapp.adapter.ArchivedMoonsAdapter;
import com.android.yuniapp.adapter.UnLaunchedMoonsAdapter;
import com.android.yuniapp.fragment.DropEntityPopUpFragment;
import com.android.yuniapp.fragment.InfoPopUpFragment;
import com.android.yuniapp.listener.DropEntityPopUpListener;
import com.android.yuniapp.listener.InfoPopUpEventListener;
import com.android.yuniapp.listener.PopupWindowListener;
import com.android.yuniapp.model.CometResponseModel;
import com.android.yuniapp.model.EntitiesCoordinatesModel;
import com.android.yuniapp.model.EntitiesModel;
import com.android.yuniapp.model.GetMessageModel;
import com.android.yuniapp.model.GetMoonsResponseModel;
import com.android.yuniapp.model.MoonRequestModel;
import com.android.yuniapp.model.MoonResponseModel;
import com.android.yuniapp.model.PlanetRequestModel;
import com.android.yuniapp.model.PlanetResponseModel;
import com.android.yuniapp.model.StarResponseModel;
import com.android.yuniapp.quickAction.ActionItem;
import com.android.yuniapp.quickAction.QuickAction;
import com.android.yuniapp.rest.ApiClient;
import com.android.yuniapp.rest.ApiInterface;
import com.android.yuniapp.utils.AndroidUtils;
import com.android.yuniapp.utils.AppUtils;
import com.android.yuniapp.utils.ConnectivityController;
import com.android.yuniapp.utils.ConstantUtils;
import com.android.yuniapp.utils.CustomView;
import com.android.yuniapp.utils.SpiralView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MoonPageActivity extends AppCompatActivity implements View.OnClickListener, PopupWindowListener, QuickAction.OnActionItemClickListener, View.OnLongClickListener, InfoPopUpEventListener, View.OnDragListener, DropEntityPopUpListener {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private TextView txtMiddlePlanet, txtStarName, txtPlanetName;
    private ImageView imgBottomMoon, imgBack;
    private RelativeLayout layoutHour, layoutDay, layoutWeek, layoutMonth, layoutYear;
    private RelativeLayout layoutCustomView;
    private ProgressBar progressBar;
    private Dialog progressDialog;
    private TextView txtDoStars, txtExceedStars;
    private View viewDoStars, viewExceedStars;
    private boolean isDoStars = true;
    private float centerX, centerY;
    private String planetId = "";
    private QuickAction quickActionUnLaunch, quickActionArchive;
    private int moonPosition;
    private InfoPopUpFragment infoPopUpFragment;
    private SpiralView spiralView;
    //PlanetUpdate related
    private PlanetResponseModel planetResponseModel;
    private int taskPosition;

    //Popup's
    private View viewDim;
    private RecyclerView recyclerViewUnLaunched, recyclerViewArchived;
    private UnLaunchedMoonsAdapter unLaunchedMoonsAdapter;
    private ArchivedMoonsAdapter archivedMoonsAdapter;
    private RelativeLayout popupUnLaunched, popupArchived;
    private ProgressBar progressBarUnLaunched, progressBarArchived;

    //ArrayLists
    private ArrayList<MoonResponseModel> unLaunchedMoonsList = new ArrayList<>();
    private ArrayList<MoonResponseModel> archivedMoonsList = new ArrayList<>();
    private ArrayList<MoonResponseModel> launchedMoonsList = new ArrayList<>();
    private ArrayList<MoonResponseModel> entriesToShowInOrbit1 = new ArrayList<>();
    private ArrayList<MoonResponseModel> entriesToShowInOrbit2 = new ArrayList<>();
    private ArrayList<MoonResponseModel> entriesToShowInOrbit3 = new ArrayList<>();
    private ArrayList<MoonResponseModel> entriesToShowInOrbit4 = new ArrayList<>();
    private ArrayList<EntitiesCoordinatesModel> equidistantPointsOfOrbits = new ArrayList<>();
    private ArrayList<EntitiesModel> entriesToShow = new ArrayList<>();
    private ArrayList<EntitiesCoordinatesModel> coordinatesOfOrbit1 = new ArrayList<>();
    private ArrayList<EntitiesCoordinatesModel> coordinatesOfOrbit2 = new ArrayList<>();
    private ArrayList<EntitiesCoordinatesModel> coordinatesOfOrbit3 = new ArrayList<>();
    private ArrayList<EntitiesCoordinatesModel> coordinatesOfOrbit4 = new ArrayList<>();

    //Constants
    private static final int BACK_REQUEST_CODE = 39;
    private static final int EDIT_LAUNCH_MOON_RESULT_CODE = 40;
    private static final int EDIT_UNLAUNCH_MOON_RESULT_CODE = 41;
    private static final int EDIT_LAUNCH_PLANET_REQUEST_CODE = 42;
    private int entityImageRadius;
    private int noOfOrbits = 5, noOfSegments;

    //DoubleClick
    private final int doubleClickTimeout = ViewConfiguration.getDoubleTapTimeout();
    private Handler handler;
    private long firstClickTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moon_page);
        firstClickTime = 0L;
        handler = new Handler(Looper.getMainLooper());

        sharedPreferences = getSharedPreferences(ConstantUtils.APP_PREF, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        AppUtils.setToolbarWithProfileImage(this, "Moon Page", "(Details)", sharedPreferences.getString(ConstantUtils.USER_PROFILE, ""));


        progressDialog = new Dialog(this);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setContentView(R.layout.fragment_loader);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        ActionItem editAction = new ActionItem(1, R.drawable.edit_icon);
        ActionItem deleteAction = new ActionItem(2, R.drawable.trash_icon);
        ActionItem previewAction = new ActionItem(3, R.drawable.preview_icon);

        quickActionUnLaunch = new QuickAction(this, QuickAction.HORIZONTAL);
        quickActionUnLaunch.setColorRes(R.color.colorWhite);
        quickActionUnLaunch.setEnabledDivider(false);
        quickActionUnLaunch.addActionItem(editAction, deleteAction);
        quickActionUnLaunch.setOnActionItemClickListener(this);

        quickActionArchive = new QuickAction(this, QuickAction.HORIZONTAL);
        quickActionArchive.setColorRes(R.color.colorWhite);
        quickActionArchive.setEnabledDivider(false);
        quickActionArchive.addActionItem(previewAction, deleteAction);
        quickActionArchive.setOnActionItemClickListener(this);

        initViews();
        setOnClick();

        spiralView = new SpiralView(this);
        layoutCustomView.addView(spiralView);
        entityImageRadius = getResources().getDrawable(R.drawable.star_storage_red_priority_1).getIntrinsicHeight();
        unLaunchedMoonsAdapter = new UnLaunchedMoonsAdapter(MoonPageActivity.this, MoonPageActivity.this, unLaunchedMoonsList);
        recyclerViewUnLaunched.setAdapter(unLaunchedMoonsAdapter);

        archivedMoonsAdapter = new ArchivedMoonsAdapter(MoonPageActivity.this, MoonPageActivity.this, archivedMoonsList);
        recyclerViewArchived.setAdapter(archivedMoonsAdapter);

        layoutCustomView.setOnDragListener(this);


        if (getIntent() != null) {

            if (getIntent().getParcelableExtra(ConstantUtils.PLANET_RESPONSE_MODEL) != null) {
                planetResponseModel = getIntent().getParcelableExtra(ConstantUtils.PLANET_RESPONSE_MODEL);

                if (planetResponseModel.getName() != null) {
                    txtMiddlePlanet.setText(planetResponseModel.getName());
                    txtPlanetName.setText(planetResponseModel.getName());
                }
                planetId = planetResponseModel.getPlanet_id();

            }
            taskPosition = getIntent().getIntExtra(ConstantUtils.TASK_POSITION, 0);


            if (getIntent().getStringExtra(ConstantUtils.STAR_NAME) != null) {
                txtStarName.setText(getIntent().getStringExtra(ConstantUtils.STAR_NAME) + ">");
            }

        }

        ViewTreeObserver vto = layoutCustomView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                layoutCustomView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                centerX = layoutCustomView.getMeasuredWidth() / 2;
                centerY = layoutCustomView.getMeasuredHeight() / 2;
                noOfSegments = 1000 * noOfOrbits;

                double radius = 0;
                double x = centerX;
                double y = centerY;
                double last_x = 0;
                double last_y = 0;
                double deltaR = getResources().getDimension(R.dimen._140sdp) / noOfSegments;
                double arc_distance = 0;
                for (int i = 1; i < noOfSegments; i++) {
                    radius += deltaR;

                    x = centerX + radius * Math.cos((2 * -Math.PI * i / noOfSegments) * noOfOrbits);
                    y = centerY + radius * Math.sin((2 * -Math.PI * i / noOfSegments) * noOfOrbits);

                    double distance = Math.sqrt((last_x - x) * (last_x - x) + (last_y - y) * (last_y - y));
                    arc_distance += distance;

                    if (arc_distance > entityImageRadius && i >= 80) {

                        equidistantPointsOfOrbits.add(new EntitiesCoordinatesModel(x, y));
                        if (i > 1000 && i <= 2000)
                            coordinatesOfOrbit1.add(new EntitiesCoordinatesModel(x, y));
                        else if (i > 2000 && i <= 3000)
                            coordinatesOfOrbit2.add(new EntitiesCoordinatesModel(x, y));

                        else if (i > 3000 && i <= 4000)
                            coordinatesOfOrbit3.add(new EntitiesCoordinatesModel(x, y));

                        else if (i > 4000)
                            coordinatesOfOrbit4.add(new EntitiesCoordinatesModel(x, y));
                        arc_distance = 0;
                    }
                    last_x = x;
                    last_y = y;


                }
                if (ConnectivityController.isNetworkAvailable(MoonPageActivity.this)) {
                    if (planetId != null && !planetId.isEmpty())
                        callGetLaunchedMoonsApi(true);
                } else
                    AndroidUtils.showToast(MoonPageActivity.this, getResources().getString(R.string.no_internet));

            }
        });
    }


    private void initViews() {
        txtStarName = findViewById(R.id.txt_star_name);
        txtPlanetName = findViewById(R.id.txt_planet_name);
        progressBar = findViewById(R.id.progress_bar);
        layoutCustomView = findViewById(R.id.layout_custom_view);
        txtMiddlePlanet = findViewById(R.id.txt_middle_planet);
        imgBottomMoon = findViewById(R.id.img_bottom_moon);
        AppUtils.rotateAnimation(imgBottomMoon);
        layoutHour = findViewById(R.id.layout_hour);
        layoutDay = findViewById(R.id.layout_day);
        layoutMonth = findViewById(R.id.layout_month);
        layoutWeek = findViewById(R.id.layout_week);
        layoutYear = findViewById(R.id.layout_year);
        imgBack = findViewById(R.id.img_back);
        txtDoStars = findViewById(R.id.txt_do_stars);
        txtExceedStars = findViewById(R.id.txt_exceed_stars);
        viewDoStars = findViewById(R.id.view_do_stars);
        viewExceedStars = findViewById(R.id.view_exceed_stars);

        //PopUp Views
        viewDim = findViewById(R.id.view_dim);
        progressBarUnLaunched = findViewById(R.id.progress_bar_unlaunched);
        progressBarArchived = findViewById(R.id.progress_bar_archived);
        popupUnLaunched = findViewById(R.id.popup_unlaunched);
        popupArchived = findViewById(R.id.popup_archived);
        recyclerViewUnLaunched = findViewById(R.id.recycler_unlaunched);
        recyclerViewArchived = findViewById(R.id.recycler_archive);
        recyclerViewUnLaunched.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewArchived.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setOnClick() {
        findViewById(R.id.img_unlaunched_moon).setOnClickListener(this);
        findViewById(R.id.img_archive_moon).setOnClickListener(this);
        findViewById(R.id.img_add_moon).setOnClickListener(this);
        txtMiddlePlanet.setOnClickListener(this);
        layoutHour.setOnClickListener(this);
        layoutDay.setOnClickListener(this);
        layoutWeek.setOnClickListener(this);
        layoutMonth.setOnClickListener(this);
        layoutYear.setOnClickListener(this);
        imgBack.setOnClickListener(this);
        txtDoStars.setOnClickListener(this);
        txtExceedStars.setOnClickListener(this);
        viewDim.setOnClickListener(this);
        txtStarName.setOnClickListener(this);
        txtPlanetName.setOnClickListener(this);
    }

    @Override
    public void onClick(final View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.img_profile:
                startActivityForResult(new Intent(MoonPageActivity.this, ProfileActivity.class), BACK_REQUEST_CODE);
                break;
            case R.id.txt_star_name:
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.txt_planet_name:
                onBackPressed();
                break;
            case R.id.txt_middle_planet:
                intent = new Intent(MoonPageActivity.this, AddTaskActivity.class);
                intent.putExtra(ConstantUtils.PURPOSE, "update");
                intent.putExtra(ConstantUtils.SOURCE, "planet");
                intent.putExtra(ConstantUtils.PLANET_RESPONSE_MODEL, planetResponseModel);
                startActivityForResult(intent, EDIT_LAUNCH_PLANET_REQUEST_CODE);
                overridePendingTransition(R.anim.slide_in_up, R.anim.stay);
                break;
            case R.id.img_add_moon:
                Intent activityIntent = new Intent(MoonPageActivity.this, AddTaskActivity.class);
                activityIntent.putExtra(ConstantUtils.SOURCE, "moon");
                activityIntent.putExtra(ConstantUtils.PLANET_ID, planetId);
                startActivityForResult(activityIntent, 103);
                overridePendingTransition(R.anim.slide_in_up, R.anim.stay);
                break;
            case R.id.img_unlaunched_moon:

                popupUnLaunched.setVisibility(View.VISIBLE);
                popupArchived.setVisibility(View.GONE);
                viewDim.setVisibility(View.VISIBLE);

                if (unLaunchedMoonsList.isEmpty())
                    callGetUnLaunchedMoonsApi(true);
                else callGetUnLaunchedMoonsApi(false);
                break;
            case R.id.img_archive_moon:
                popupUnLaunched.setVisibility(View.GONE);
                popupArchived.setVisibility(View.VISIBLE);
                viewDim.setVisibility(View.VISIBLE);


                if (archivedMoonsList.isEmpty())
                    callGetArchivedMoonsApi(true);
                else callGetArchivedMoonsApi(false);

                break;
            case R.id.view_dim:
                viewDim.setVisibility(View.GONE);
                popupUnLaunched.setVisibility(View.GONE);
                popupArchived.setVisibility(View.GONE);
                break;
            case R.id.layout_hour:


                layoutHour.setBackground(getResources().getDrawable(R.drawable.drawable_left_round_solid));
                layoutDay.setBackground(null);
                layoutWeek.setBackground(null);
                layoutMonth.setBackground(null);
                layoutYear.setBackground(null);

                layoutCustomView.removeAllViewsInLayout();
                layoutCustomView.addView(spiralView);

                entriesToShowInOrbit1.clear();
                entriesToShowInOrbit2.clear();
                entriesToShowInOrbit3.clear();
                entriesToShowInOrbit4.clear();
                entriesToShow.clear();

                for (int i = 0; i < launchedMoonsList.size(); i++) {
                    MoonResponseModel dataResponseModel = launchedMoonsList.get(i);
                    if (dataResponseModel.getFinish_time()!= null) {
                        if (isDoStars) {

                            if (AppUtils.isBetween(AppUtils.getHourDiff(dataResponseModel.getFinish_date() + " " + dataResponseModel.getFinish_time()), 0, 2)) {
                                entriesToShowInOrbit1.add(dataResponseModel);
                            } else if (AppUtils.isBetween(AppUtils.getHourDiff(dataResponseModel.getFinish_date() + " " + dataResponseModel.getFinish_time()), 3, 5)) {
                                entriesToShowInOrbit2.add(dataResponseModel);
                            } else if (AppUtils.isBetween(AppUtils.getHourDiff(dataResponseModel.getFinish_date() + " " + dataResponseModel.getFinish_time()), 6, 8)) {
                                entriesToShowInOrbit3.add(dataResponseModel);
                            } else if (AppUtils.isBetween(AppUtils.getHourDiff(dataResponseModel.getFinish_date() + " " + dataResponseModel.getFinish_time()), 9, 11)) {
                                entriesToShowInOrbit4.add(dataResponseModel);
                            }


                        } else {
                            if (AppUtils.isBetweenMinus(AppUtils.getHourDiff(dataResponseModel.getFinish_date() + " " + dataResponseModel.getFinish_time()), -1, -3)) {
                                entriesToShowInOrbit1.add(dataResponseModel);
                            } else if (AppUtils.isBetweenMinus(AppUtils.getHourDiff(dataResponseModel.getFinish_date() + " " + dataResponseModel.getFinish_time()), -4, -6)) {
                                entriesToShowInOrbit2.add(dataResponseModel);
                            } else if (AppUtils.isBetweenMinus(AppUtils.getHourDiff(dataResponseModel.getFinish_date() + " " + dataResponseModel.getFinish_time()), -7, -9)) {
                                entriesToShowInOrbit3.add(dataResponseModel);
                            } else if (AppUtils.isBetweenMinus(AppUtils.getHourDiff(dataResponseModel.getFinish_date() + " " + dataResponseModel.getFinish_time()), -10, -12)) {
                                entriesToShowInOrbit4.add(dataResponseModel);
                            }
                        }

                    }
                }

                setDataForOrbit1();
                setDataForOrbit2();
                setDataForOrbit3();
                setDataForOrbit4();

                break;
            case R.id.layout_day:

                layoutHour.setBackground(null);
                layoutDay.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                layoutWeek.setBackground(null);
                layoutMonth.setBackground(null);
                layoutYear.setBackground(null);

                layoutCustomView.removeAllViewsInLayout();
                layoutCustomView.addView(spiralView);

                entriesToShowInOrbit1.clear();
                entriesToShowInOrbit2.clear();
                entriesToShowInOrbit3.clear();
                entriesToShowInOrbit4.clear();
                entriesToShow.clear();

                int daysDiff = -9;
                for (int i = 0; i < launchedMoonsList.size(); i++) {
                    MoonResponseModel dataResponseModel = launchedMoonsList.get(i);
                    Date currentDate = null, finishDate = null;
                    try {


                        if (dataResponseModel.getFinish_time() != null) {
                            finishDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(dataResponseModel.getFinish_date() + " " + dataResponseModel.getFinish_time());
                            currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime()));

                            daysDiff = AppUtils.getDayDiff(currentDate, finishDate);
                        } else {
                            finishDate = new SimpleDateFormat("yyyy-MM-dd").parse(dataResponseModel.getFinish_date());
                            currentDate = new SimpleDateFormat("yyyy-MM-dd").parse(new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));

                            daysDiff = AppUtils.getDayDiff(currentDate, finishDate);
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }


                    if (isDoStars) {
                        switch (daysDiff) {
                            case 0:
                                entriesToShowInOrbit1.add(dataResponseModel);
                                break;
                            case 1:
                                entriesToShowInOrbit2.add(dataResponseModel);
                                break;
                            case 2:
                                entriesToShowInOrbit3.add(dataResponseModel);
                                break;
                            case 3:
                                entriesToShowInOrbit4.add(dataResponseModel);
                                break;
                        }
                    } else {
                        switch (daysDiff) {
                            case -1:
                                entriesToShowInOrbit1.add(dataResponseModel);
                                break;
                            case -2:
                                entriesToShowInOrbit2.add(dataResponseModel);
                                break;
                            case -3:
                                entriesToShowInOrbit3.add(dataResponseModel);
                                break;
                            case -4:
                                entriesToShowInOrbit4.add(dataResponseModel);
                                break;
                        }
                    }

                }
                setDataForOrbit1();
                setDataForOrbit2();
                setDataForOrbit3();
                setDataForOrbit4();
                break;
            case R.id.layout_week:
                layoutHour.setBackground(null);
                layoutWeek.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                layoutDay.setBackground(null);
                layoutMonth.setBackground(null);
                layoutYear.setBackground(null);

                layoutCustomView.removeAllViewsInLayout();
                layoutCustomView.addView(spiralView);

                entriesToShowInOrbit1.clear();
                entriesToShowInOrbit2.clear();
                entriesToShowInOrbit3.clear();
                entriesToShowInOrbit4.clear();
                entriesToShow.clear();

                for (int i = 0; i < launchedMoonsList.size(); i++) {
                    MoonResponseModel dataResponseModel = launchedMoonsList.get(i);
                    //String currentDate;
                    int weekDiff=-9,hourDiff=0;
                    daysDiff=0;
                    //  currentDate = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());

                    Date currentDate = null, finishDate = null;
                    try {


                        if (dataResponseModel.getFinish_time() != null) {
                            finishDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(dataResponseModel.getFinish_date() + " " + dataResponseModel.getFinish_time());
                            currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime()));

                            weekDiff = AppUtils.getWeekDiff(currentDate, finishDate);
                        } else {
                            finishDate = new SimpleDateFormat("yyyy-MM-dd").parse(dataResponseModel.getFinish_date());
                            currentDate = new SimpleDateFormat("yyyy-MM-dd").parse(new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));

                            weekDiff = AppUtils.getWeekDiff(currentDate, finishDate);
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    if(currentDate!=null && finishDate!=null) {
                        long difference = finishDate.getTime() - currentDate.getTime();
                        hourDiff = (int) TimeUnit.MILLISECONDS.toHours(difference);
                        daysDiff = (int) TimeUnit.MILLISECONDS.toDays(difference);
                    }

                    if (isDoStars) {
                        if(AppUtils.isBetween(daysDiff,0,7))
                        {
                            entriesToShowInOrbit1.add(dataResponseModel);
                        }else  if(AppUtils.isBetween(daysDiff,8,14))
                        {
                            entriesToShowInOrbit2.add(dataResponseModel);
                        }else  if(AppUtils.isBetween(daysDiff,15,21))
                        {
                            entriesToShowInOrbit3.add(dataResponseModel);
                        }else if(AppUtils.isBetween(daysDiff,22,31)){
                            entriesToShowInOrbit4.add(dataResponseModel);
                        }

                    } else {
                        if(AppUtils.isBetweenMinus(daysDiff,-1,-8))
                        {
                            entriesToShowInOrbit1.add(dataResponseModel);
                        }else  if(AppUtils.isBetweenMinus(daysDiff,-9,-16))
                        {
                            entriesToShowInOrbit2.add(dataResponseModel);
                        }else  if(AppUtils.isBetweenMinus(daysDiff,-17,-24))
                        {
                            entriesToShowInOrbit3.add(dataResponseModel);
                        }else if(AppUtils.isBetweenMinus(daysDiff,-25,-32)) {
                            entriesToShowInOrbit4.add(dataResponseModel);
                        }

                    }

                }

                setDataForOrbit1();
                setDataForOrbit2();
                setDataForOrbit3();
                setDataForOrbit4();

                break;
            case R.id.layout_month:
                layoutHour.setBackground(null);
                layoutMonth.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                layoutWeek.setBackground(null);
                layoutDay.setBackground(null);
                layoutYear.setBackground(null);

                layoutCustomView.removeAllViewsInLayout();
                layoutCustomView.addView(spiralView);

                entriesToShowInOrbit1.clear();
                entriesToShowInOrbit2.clear();
                entriesToShowInOrbit3.clear();
                entriesToShowInOrbit4.clear();
                entriesToShow.clear();

                for (int i = 0; i < launchedMoonsList.size(); i++) {
                    MoonResponseModel dataResponseModel = launchedMoonsList.get(i);
                    String currentDate;
                    currentDate = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());

                    if (isDoStars) {
                        switch (AppUtils.getMonthDiff(currentDate, dataResponseModel.getFinish_date())) {
                            case 0:
                                entriesToShowInOrbit1.add(dataResponseModel);
                                break;
                            case 1:
                                entriesToShowInOrbit2.add(dataResponseModel);
                                break;
                            case 2:
                                entriesToShowInOrbit3.add(dataResponseModel);
                                break;
                            case 3:
                                entriesToShowInOrbit4.add(dataResponseModel);
                                break;
                        }
                    } else {
                        switch (AppUtils.getMonthDiff(currentDate, dataResponseModel.getFinish_date())) {
                            case -1:
                                entriesToShowInOrbit1.add(dataResponseModel);
                                break;
                            case -2:
                                entriesToShowInOrbit2.add(dataResponseModel);
                                break;
                            case -3:
                                entriesToShowInOrbit3.add(dataResponseModel);
                                break;
                            case -4:
                                entriesToShowInOrbit4.add(dataResponseModel);
                                break;
                        }
                    }
                }

                setDataForOrbit1();
                setDataForOrbit2();
                setDataForOrbit3();
                setDataForOrbit4();

                break;
            case R.id.layout_year:
                layoutHour.setBackground(null);
                layoutYear.setBackground(getResources().getDrawable(R.drawable.drawable_right_round_solid));
                layoutWeek.setBackground(null);
                layoutMonth.setBackground(null);
                layoutDay.setBackground(null);

                layoutCustomView.removeAllViewsInLayout();
                layoutCustomView.addView(spiralView);

                entriesToShowInOrbit1.clear();
                entriesToShowInOrbit2.clear();
                entriesToShowInOrbit3.clear();
                entriesToShowInOrbit4.clear();
                entriesToShow.clear();

                for (int i = 0; i < launchedMoonsList.size(); i++) {
                    MoonResponseModel dataResponseModel = launchedMoonsList.get(i);
                    String currentDate;
                    currentDate = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());

                    if (isDoStars) {
                        switch (AppUtils.getYearDiff(currentDate, dataResponseModel.getFinish_date())) {
                            case 0:
                                entriesToShowInOrbit1.add(dataResponseModel);
                                break;
                            case 1:
                                entriesToShowInOrbit2.add(dataResponseModel);
                                break;
                            case 2:
                                entriesToShowInOrbit3.add(dataResponseModel);
                                break;
                            case 3:
                                entriesToShowInOrbit4.add(dataResponseModel);
                                break;
                        }
                    } else {
                        switch (AppUtils.getYearDiff(currentDate, dataResponseModel.getFinish_date())) {
                            case -1:
                                entriesToShowInOrbit1.add(dataResponseModel);
                                break;
                            case -2:
                                entriesToShowInOrbit2.add(dataResponseModel);
                                break;
                            case -3:
                                entriesToShowInOrbit3.add(dataResponseModel);
                                break;
                            case -4:
                                entriesToShowInOrbit4.add(dataResponseModel);
                                break;
                        }
                    }
                }

                setDataForOrbit1();
                setDataForOrbit2();
                setDataForOrbit3();
                setDataForOrbit4();

                break;
            case R.id.img_back:
                break;
            case R.id.txt_do_stars:
                viewDoStars.setAlpha(0.5f);
                viewExceedStars.setAlpha(0.1f);
                isDoStars = true;

                if (layoutHour.getBackground() != null)
                    layoutHour.performClick();
                else if (layoutDay.getBackground() != null)
                    layoutDay.performClick();
                else if (layoutWeek.getBackground() != null)
                    layoutWeek.performClick();
                else if (layoutMonth.getBackground() != null)
                    layoutMonth.performClick();
                else layoutYear.performClick();
                break;
            case R.id.txt_exceed_stars:
                viewDoStars.setAlpha(0.1f);
                viewExceedStars.setAlpha(0.5f);
                isDoStars = false;

                if (layoutHour.getBackground() != null)
                    layoutHour.performClick();
                else if (layoutDay.getBackground() != null)
                    layoutDay.performClick();
                else if (layoutWeek.getBackground() != null)
                    layoutWeek.performClick();
                else if (layoutMonth.getBackground() != null)
                    layoutMonth.performClick();
                else layoutYear.performClick();

                break;
            default:
                long now = System.currentTimeMillis();

                if (now - firstClickTime < doubleClickTimeout) {
                    handler.removeCallbacksAndMessages(null);
                    firstClickTime = 0L;
                    onDoubleClick(v);
                } else {
                    firstClickTime = now;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onSingleClick(v);
                            firstClickTime = 0L;
                        }
                    }, doubleClickTimeout);
                }


                break;
        }

    }

    private void onDoubleClick(View v) {
        infoPopUpFragment = new InfoPopUpFragment();
        Bundle bundle = new Bundle();
        for (int i = 0; i < launchedMoonsList.size(); i++) {
            if (launchedMoonsList.get(i).getMoon_id() != null && v.getId() == Integer.parseInt(launchedMoonsList.get(i).getMoon_id())) {
                bundle.putParcelable(ConstantUtils.MOON_RESPONSE_MODEL, launchedMoonsList.get(i));
                bundle.putInt(ConstantUtils.TASK_POSITION, i);
            }
        }
        infoPopUpFragment.setArguments(bundle);
        infoPopUpFragment.setInfoPopUpListener(this);
        infoPopUpFragment.show(getSupportFragmentManager(), infoPopUpFragment.getTag());
        infoPopUpFragment.setCancelable(false);
    }


    private void onSingleClick(View v) {
        for (int i = 0; i < launchedMoonsList.size(); i++) {
            if (launchedMoonsList.get(i).getMoon_id() != null && v.getId() == Integer.parseInt(launchedMoonsList.get(i).getMoon_id())) {
                Intent intent = new Intent(MoonPageActivity.this, SatellitePageActivity.class);
                intent.putExtra(ConstantUtils.TASK_POSITION, i);
                intent.putExtra(ConstantUtils.MOON_RESPONSE_MODEL, launchedMoonsList.get(i));
                intent.putExtra(ConstantUtils.STAR_NAME, txtStarName.getText().toString().trim());
                intent.putExtra(ConstantUtils.PLANET_NAME, txtPlanetName.getText().toString().trim());
                startActivityForResult(intent, BACK_REQUEST_CODE);
            }
        }
    }

    private void callGetLaunchedMoonsApi(boolean needLoader) {
        if (needLoader)
            progressBar.setVisibility(View.VISIBLE);
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetMoonsResponseModel> getLaunchedMoonsResponseCall = apiService.getLaunchedMoons(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), planetId);
        getLaunchedMoonsResponseCall.enqueue(new Callback<GetMoonsResponseModel>() {
            @Override
            public void onResponse(Call<GetMoonsResponseModel> call, Response<GetMoonsResponseModel> response) {
                progressBar.setVisibility(View.GONE);
                if (response.code() == 200 && response.body() != null) {
                    if (response.body().getMoons() != null && !response.body().getMoons().isEmpty()) {
                        launchedMoonsList.clear();
                        launchedMoonsList.addAll(response.body().getMoons());


                        if (layoutHour.getBackground() != null)
                            layoutHour.performClick();
                        else if (layoutDay.getBackground() != null)
                            layoutDay.performClick();
                        else if (layoutWeek.getBackground() != null)
                            layoutWeek.performClick();
                        else if (layoutMonth.getBackground() != null)
                            layoutMonth.performClick();
                        else layoutYear.performClick();


                    }
                } else if (response.code() == 500) {
                    AndroidUtils.showToast(MoonPageActivity.this, getResources().getString(R.string.some_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<GetMoonsResponseModel> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                AndroidUtils.showToast(MoonPageActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });

    }

    private void callGetUnLaunchedMoonsApi(boolean needLoader) {
        if (needLoader)
            progressBarUnLaunched.setVisibility(View.VISIBLE);
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetMoonsResponseModel> getMoonsResponseModelCall = apiService.getUnLaunchedMoons(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), planetId);
        getMoonsResponseModelCall.enqueue(new Callback<GetMoonsResponseModel>() {
            @Override
            public void onResponse(Call<GetMoonsResponseModel> call, Response<GetMoonsResponseModel> response) {
                progressBarUnLaunched.setVisibility(View.GONE);

                if (response.code() == 200 && response.body() != null) {
                    if (response.body().getMoons() != null && !response.body().getMoons().isEmpty()) {
                        unLaunchedMoonsList.clear();
                        unLaunchedMoonsList.addAll(response.body().getMoons());
                        unLaunchedMoonsAdapter.notifyDataSetChanged();
                    } else {
                        popupUnLaunched.setVisibility(View.GONE);
                        viewDim.setVisibility(View.GONE);
                        AndroidUtils.showToast(MoonPageActivity.this, "No new moons waiting for dates.");
                    }
                }
            }

            @Override
            public void onFailure(Call<GetMoonsResponseModel> call, Throwable t) {
                progressBarUnLaunched.setVisibility(View.GONE);
                AndroidUtils.showToast(MoonPageActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });
    }


    private void callGetArchivedMoonsApi(boolean needLoader) {
        if (needLoader)
            progressBarArchived.setVisibility(View.VISIBLE);
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetMoonsResponseModel> getMoonsResponseModelCall = apiService.getArchivedMoons(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), planetId);
        getMoonsResponseModelCall.enqueue(new Callback<GetMoonsResponseModel>() {
            @Override
            public void onResponse(Call<GetMoonsResponseModel> call, Response<GetMoonsResponseModel> response) {
                progressBarArchived.setVisibility(View.GONE);

                if (response.code() == 200 && response.body() != null) {
                    if (response.body().getMoons() != null && !response.body().getMoons().isEmpty()) {
                        archivedMoonsList.clear();
                        archivedMoonsList.addAll(response.body().getMoons());
                        archivedMoonsAdapter.notifyDataSetChanged();
                    } else {
                        popupArchived.setVisibility(View.GONE);
                        viewDim.setVisibility(View.GONE);
                        AndroidUtils.showToast(MoonPageActivity.this, "No old moons.");
                    }
                }
            }

            @Override
            public void onFailure(Call<GetMoonsResponseModel> call, Throwable t) {
                progressBarArchived.setVisibility(View.GONE);
                AndroidUtils.showToast(MoonPageActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });

    }

    /*  private void setDataOnSpiral() {
          TextView orbitEntryImage;
          for (int i = 0; i < entriesToShow.size(); i++) {
              if (i <= equidistantPointsOfOrbits.size() - 1) {
                  orbitEntryImage = new TextView(MoonPageActivity.this);
                  orbitEntryImage.setLayoutParams(new android.view.ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                  orbitEntryImage.setPadding(20,  1, 20, 1);
                  orbitEntryImage.setId(entriesToShow.get(i).getEntityId());
                  orbitEntryImage.setText(entriesToShow.get(i).getEntityName());
                  orbitEntryImage.setEllipsize(TextUtils.TruncateAt.END);
                  orbitEntryImage.setTextSize(7f);
                  orbitEntryImage.setGravity(Gravity.CENTER);
                  orbitEntryImage.setMaxLines(1);
                  setEntityImage(orbitEntryImage, entriesToShow.get(i).getDaysDiff(), entriesToShow.get(i).getPriority());
                  orbitEntryImage.setX((float) entriesToShow.get(i).getX() - entityImageRadius / 2);
                  orbitEntryImage.setY((float) entriesToShow.get(i).getY() - entityImageRadius / 2);
                  orbitEntryImage.setOnClickListener(this);
                  orbitEntryImage.setOnLongClickListener(this);
                  layoutCustomView.addView(orbitEntryImage);
              } else if (entriesToShow.size() > equidistantPointsOfOrbits.size()) {
                  orbitEntryImage = new TextView(MoonPageActivity.this);
                  orbitEntryImage.setLayoutParams(new android.view.ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                  orbitEntryImage.setPadding(20,  1, 20, 1);
                  orbitEntryImage.setText(entriesToShow.size() - equidistantPointsOfOrbits.size() + "+");
                  orbitEntryImage.setEllipsize(TextUtils.TruncateAt.END);
                  orbitEntryImage.setTextSize(7f);
                  orbitEntryImage.setGravity(Gravity.CENTER);
                  orbitEntryImage.setMaxLines(1);
                  orbitEntryImage.setBackgroundResource(R.drawable.moon_overlay);
                  orbitEntryImage.setX((float) entriesToShow.get(i).getX() - entityImageRadius / 2);
                  orbitEntryImage.setY((float) entriesToShow.get(i).getY() - entityImageRadius / 2);
                  orbitEntryImage.setOnClickListener(this);
                  orbitEntryImage.setOnLongClickListener(this);
                  layoutCustomView.addView(orbitEntryImage);
                  break;
              }
          }
      }*/
    private void setDataForOrbit1() {

        TextView orbitEntryImage;
        int noOfEntities = coordinatesOfOrbit1.size();

        for (int i = 0; i < entriesToShowInOrbit1.size(); i++) {
            if (i < noOfEntities - 1) {
                orbitEntryImage = new TextView(MoonPageActivity.this);
                orbitEntryImage.setLayoutParams(new android.view.ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                orbitEntryImage.setTypeface(ResourcesCompat.getFont(MoonPageActivity.this, R.font.teko_medium));
                orbitEntryImage.setTextColor(ContextCompat.getColor(MoonPageActivity.this,R.color.colorBlack));
                orbitEntryImage.setPadding(20, 1, 20, 1);
                orbitEntryImage.setId(Integer.parseInt(entriesToShowInOrbit1.get(i).getMoon_id()));
                orbitEntryImage.setText(entriesToShowInOrbit1.get(i).getName());
                //orbitEntryImage.setEllipsize(TextUtils.TruncateAt.END);
                orbitEntryImage.setTextSize(9f);
                orbitEntryImage.setGravity(Gravity.CENTER);
                orbitEntryImage.setMaxLines(1);
                orbitEntryImage.setTag(1);
                setEntityImage(orbitEntryImage, 1, Integer.valueOf(entriesToShowInOrbit1.get(i).getPriority()),  entriesToShowInOrbit1.get(i).getStart_date(), entriesToShowInOrbit1.get(i).getFinish_date());
                orbitEntryImage.setX((float) (coordinatesOfOrbit1.get(i).getX() - entityImageRadius / 2));
                orbitEntryImage.setY((float) (coordinatesOfOrbit1.get(i).getY() - entityImageRadius / 2));
                orbitEntryImage.setOnClickListener(this);
                orbitEntryImage.setOnLongClickListener(this);
                layoutCustomView.addView(orbitEntryImage);

            } else if (entriesToShowInOrbit1.size() > noOfEntities) {
                orbitEntryImage = new TextView(MoonPageActivity.this);
                orbitEntryImage.setLayoutParams(new android.view.ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                orbitEntryImage.setTypeface(ResourcesCompat.getFont(MoonPageActivity.this, R.font.teko_medium));
                orbitEntryImage.setTextColor(ContextCompat.getColor(MoonPageActivity.this,R.color.colorBlack));
                orbitEntryImage.setPadding(20, 1, 20, 1);
                orbitEntryImage.setText(entriesToShowInOrbit1.size() - noOfEntities + "+");
                orbitEntryImage.setTextSize(8f);
                orbitEntryImage.setGravity(Gravity.CENTER);
                orbitEntryImage.setMaxLines(1);
                orbitEntryImage.setBackgroundResource(R.drawable.galaxy);
                orbitEntryImage.setX((float) (coordinatesOfOrbit1.get(i).getX() - entityImageRadius / 2));
                orbitEntryImage.setY((float) (coordinatesOfOrbit1.get(i).getY() - entityImageRadius / 2));
                orbitEntryImage.setId(Integer.parseInt(entriesToShowInOrbit1.get(i).getMoon_id()));
                layoutCustomView.addView(orbitEntryImage);
                break;

            }
        }
    }

    private void setDataForOrbit2() {

        TextView orbitEntryImage;
        int noOfEntities = coordinatesOfOrbit2.size();

        for (int i = 0; i < entriesToShowInOrbit2.size(); i++) {
            if (i < noOfEntities - 1) {
                orbitEntryImage = new TextView(MoonPageActivity.this);
                orbitEntryImage.setLayoutParams(new android.view.ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                orbitEntryImage.setTypeface(ResourcesCompat.getFont(MoonPageActivity.this, R.font.teko_medium));
                orbitEntryImage.setTextColor(ContextCompat.getColor(MoonPageActivity.this,R.color.colorBlack));
                orbitEntryImage.setPadding(20, 1, 20, 1);
                orbitEntryImage.setId(Integer.parseInt(entriesToShowInOrbit2.get(i).getMoon_id()));
                orbitEntryImage.setText(entriesToShowInOrbit2.get(i).getName());
                //orbitEntryImage.setEllipsize(TextUtils.TruncateAt.END);
                orbitEntryImage.setTextSize(9f);
                orbitEntryImage.setGravity(Gravity.CENTER);
                orbitEntryImage.setMaxLines(1);
                orbitEntryImage.setTag(2);
                setEntityImage(orbitEntryImage, 2, Integer.valueOf(entriesToShowInOrbit2.get(i).getPriority()) , entriesToShowInOrbit2.get(i).getStart_date(), entriesToShowInOrbit2.get(i).getFinish_date());
                orbitEntryImage.setX((float) (coordinatesOfOrbit2.get(i).getX() - entityImageRadius / 2));
                orbitEntryImage.setY((float) (coordinatesOfOrbit2.get(i).getY() - entityImageRadius / 2));
                orbitEntryImage.setOnClickListener(this);
                orbitEntryImage.setOnLongClickListener(this);
                layoutCustomView.addView(orbitEntryImage);

            } else if (entriesToShowInOrbit2.size() > noOfEntities) {
                orbitEntryImage = new TextView(MoonPageActivity.this);
                orbitEntryImage.setLayoutParams(new android.view.ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                orbitEntryImage.setTypeface(ResourcesCompat.getFont(MoonPageActivity.this, R.font.teko_medium));
                orbitEntryImage.setTextColor(ContextCompat.getColor(MoonPageActivity.this,R.color.colorBlack));
                orbitEntryImage.setPadding(20, 1, 20, 1);
                orbitEntryImage.setText(entriesToShowInOrbit2.size() - noOfEntities + "+");
                orbitEntryImage.setTextSize(8f);
                orbitEntryImage.setGravity(Gravity.CENTER);
                orbitEntryImage.setMaxLines(1);
                orbitEntryImage.setBackgroundResource(R.drawable.galaxy);
                orbitEntryImage.setX((float) (coordinatesOfOrbit1.get(i).getX() - entityImageRadius / 2));
                orbitEntryImage.setY((float) (coordinatesOfOrbit1.get(i).getY() - entityImageRadius / 2));
                orbitEntryImage.setId(Integer.parseInt(entriesToShowInOrbit2.get(i).getMoon_id()));
                layoutCustomView.addView(orbitEntryImage);
                break;

            }
        }
    }

    private void setDataForOrbit3() {

        TextView orbitEntryImage;
        int noOfEntities = coordinatesOfOrbit3.size();

        for (int i = 0; i < entriesToShowInOrbit3.size(); i++) {
            if (i < noOfEntities - 1) {
                orbitEntryImage = new TextView(MoonPageActivity.this);
                orbitEntryImage.setLayoutParams(new android.view.ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                orbitEntryImage.setTypeface(ResourcesCompat.getFont(MoonPageActivity.this, R.font.teko_medium));
                orbitEntryImage.setTextColor(ContextCompat.getColor(MoonPageActivity.this,R.color.colorBlack));
                orbitEntryImage.setPadding(20, 1, 20, 1);
                orbitEntryImage.setId(Integer.parseInt(entriesToShowInOrbit3.get(i).getMoon_id()));
                orbitEntryImage.setText(entriesToShowInOrbit3.get(i).getName());
               // orbitEntryImage.setEllipsize(TextUtils.TruncateAt.END);
                orbitEntryImage.setTextSize(9f);
                orbitEntryImage.setGravity(Gravity.CENTER);
                orbitEntryImage.setMaxLines(1);
                orbitEntryImage.setTag(3);
                setEntityImage(orbitEntryImage, 1, Integer.valueOf(entriesToShowInOrbit3.get(i).getPriority()) , entriesToShowInOrbit3.get(i).getStart_date(), entriesToShowInOrbit3.get(i).getFinish_date());
                orbitEntryImage.setX((float) (coordinatesOfOrbit3.get(i).getX() - entityImageRadius / 2));
                orbitEntryImage.setY((float) (coordinatesOfOrbit3.get(i).getY() - entityImageRadius / 2));
                orbitEntryImage.setOnClickListener(this);
                orbitEntryImage.setOnLongClickListener(this);
                layoutCustomView.addView(orbitEntryImage);

            } else if (entriesToShowInOrbit3.size() > noOfEntities) {
                orbitEntryImage = new TextView(MoonPageActivity.this);
                orbitEntryImage.setLayoutParams(new android.view.ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                orbitEntryImage.setTypeface(ResourcesCompat.getFont(MoonPageActivity.this, R.font.teko_medium));
                orbitEntryImage.setTextColor(ContextCompat.getColor(MoonPageActivity.this,R.color.colorBlack));
                orbitEntryImage.setPadding(20, 1, 20, 1);
                orbitEntryImage.setText(entriesToShowInOrbit3.size() - noOfEntities + "+");
                orbitEntryImage.setTextSize(8f);
                orbitEntryImage.setGravity(Gravity.CENTER);
                orbitEntryImage.setMaxLines(1);
                orbitEntryImage.setBackgroundResource(R.drawable.galaxy);
                orbitEntryImage.setX((float) (coordinatesOfOrbit3.get(i).getX() - entityImageRadius / 2));
                orbitEntryImage.setY((float) (coordinatesOfOrbit3.get(i).getY() - entityImageRadius / 2));
                orbitEntryImage.setId(Integer.parseInt(entriesToShowInOrbit3.get(i).getMoon_id()));
                layoutCustomView.addView(orbitEntryImage);
                break;

            }
        }
    }

    private void setDataForOrbit4() {

        TextView orbitEntryImage;
        int noOfEntities = coordinatesOfOrbit4.size();

        for (int i = 0; i < entriesToShowInOrbit4.size(); i++) {
            if (i < noOfEntities - 1) {
                orbitEntryImage = new TextView(MoonPageActivity.this);
                orbitEntryImage.setLayoutParams(new android.view.ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                orbitEntryImage.setTypeface(ResourcesCompat.getFont(MoonPageActivity.this, R.font.teko_medium));
                orbitEntryImage.setTextColor(ContextCompat.getColor(MoonPageActivity.this,R.color.colorBlack));
                orbitEntryImage.setPadding(20, 1, 20, 1);
                orbitEntryImage.setId(Integer.parseInt(entriesToShowInOrbit4.get(i).getMoon_id()));
                orbitEntryImage.setText(entriesToShowInOrbit4.get(i).getName());
                //orbitEntryImage.setEllipsize(TextUtils.TruncateAt.END);
                orbitEntryImage.setTextSize(9f);
                orbitEntryImage.setGravity(Gravity.CENTER);
                orbitEntryImage.setMaxLines(1);
                orbitEntryImage.setTag(4);
                setEntityImage(orbitEntryImage, 1, Integer.valueOf(entriesToShowInOrbit4.get(i).getPriority()) , entriesToShowInOrbit4.get(i).getStart_date(), entriesToShowInOrbit4.get(i).getFinish_date());
                orbitEntryImage.setX((float) (coordinatesOfOrbit4.get(i).getX() - entityImageRadius / 2));
                orbitEntryImage.setY((float) (coordinatesOfOrbit4.get(i).getY() - entityImageRadius / 2));
                orbitEntryImage.setOnClickListener(this);
                orbitEntryImage.setOnLongClickListener(this);
                layoutCustomView.addView(orbitEntryImage);

            } else if (entriesToShowInOrbit4.size() > noOfEntities) {
                orbitEntryImage = new TextView(MoonPageActivity.this);
                orbitEntryImage.setLayoutParams(new android.view.ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                orbitEntryImage.setTypeface(ResourcesCompat.getFont(MoonPageActivity.this, R.font.teko_medium));
                orbitEntryImage.setTextColor(ContextCompat.getColor(MoonPageActivity.this,R.color.colorBlack));
                orbitEntryImage.setPadding(20, 1, 20, 1);
                orbitEntryImage.setText(entriesToShowInOrbit4.size() - noOfEntities + "+");
                orbitEntryImage.setTextSize(8f);
                orbitEntryImage.setGravity(Gravity.CENTER);
                orbitEntryImage.setMaxLines(1);
                orbitEntryImage.setBackgroundResource(R.drawable.galaxy);
                orbitEntryImage.setX((float) (coordinatesOfOrbit4.get(i).getX() - entityImageRadius / 2));
                orbitEntryImage.setY((float) (coordinatesOfOrbit4.get(i).getY() - entityImageRadius / 2));
                orbitEntryImage.setId(Integer.parseInt(entriesToShowInOrbit4.get(i).getMoon_id()));
                layoutCustomView.addView(orbitEntryImage);
                break;

            }
        }
    }

    private void setEntityImage(View orbitEntryImage, int orbit, int priority,String startDate, String finishDate) {
        Date currentDate = null, dateFinish = null, dateStart = null;
        try {
            dateStart = new SimpleDateFormat("yyyy-MM-dd").parse(startDate);
            dateFinish = new SimpleDateFormat("yyyy-MM-dd").parse(finishDate);
            currentDate = new SimpleDateFormat("yyyy-MM-dd").parse(new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));

        } catch (ParseException e) {
            e.printStackTrace();
        }

        switch (priority) {
            case 1:

                if (isDoStars)
                {
                    if (dateStart.after(currentDate))
                        orbitEntryImage.setBackgroundResource(R.drawable.moon_yellow_priority_1);
                    else
                        orbitEntryImage.setBackgroundResource(R.drawable.moon_green_priority_1);
                }
                else
                    orbitEntryImage.setBackgroundResource(R.drawable.moon_green_priority_1);
                break;
            case 2:
                if (isDoStars )
                {
                    if (dateStart.after(currentDate))
                        orbitEntryImage.setBackgroundResource(R.drawable.moon_yellow_priority_2);
                    else
                        orbitEntryImage.setBackgroundResource(R.drawable.moon_green_priority_2);
                }
                else
                    orbitEntryImage.setBackgroundResource(R.drawable.moon_green_priority_2);
                break;
            case 3:
                if (isDoStars )
            {
                if (dateStart.after(currentDate))
                    orbitEntryImage.setBackgroundResource(R.drawable.moon_yellow_priority_3);
                else
                    orbitEntryImage.setBackgroundResource(R.drawable.moon_green_priority_3);
            }
                else
                    orbitEntryImage.setBackgroundResource(R.drawable.moon_green_priority_3);
                break;
            case 4:
                if (isDoStars )
                {
                    if (dateStart.after(currentDate))
                        orbitEntryImage.setBackgroundResource(R.drawable.moon_yellow_priority_4);
                    else
                        orbitEntryImage.setBackgroundResource(R.drawable.moon_green_priority_4);
                }
                else
                    orbitEntryImage.setBackgroundResource(R.drawable.moon_green_priority_4);
                break;
            case 5:
                if (isDoStars )
                {
                    if (dateStart.after(currentDate))
                        orbitEntryImage.setBackgroundResource(R.drawable.moon_yellow_priority_5);
                    else
                        orbitEntryImage.setBackgroundResource(R.drawable.moon_green_priority_5);
                }
                else
                    orbitEntryImage.setBackgroundResource(R.drawable.moon_green_priority_5);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 103:
                if (resultCode == RESULT_OK) {
                    if (ConnectivityController.isNetworkAvailable(MoonPageActivity.this)) {
                        if (planetId != null && !planetId.isEmpty())
                            callGetLaunchedMoonsApi(false);
                    } else
                        AndroidUtils.showToast(MoonPageActivity.this, getResources().getString(R.string.no_internet));
                }
                break;
            case EDIT_LAUNCH_MOON_RESULT_CODE:
                if (resultCode == RESULT_OK) {
                    if (infoPopUpFragment != null)
                        infoPopUpFragment.dismiss();
                    if (data != null && data.getParcelableExtra(ConstantUtils.MOON_RESPONSE_MODEL) != null) {

                        launchedMoonsList.remove(data.getIntExtra(ConstantUtils.TASK_POSITION, 0));
                        launchedMoonsList.add(data.getIntExtra(ConstantUtils.TASK_POSITION, 0), (MoonResponseModel) data.getParcelableExtra(ConstantUtils.MOON_RESPONSE_MODEL));


                        if (layoutHour.getBackground() != null)
                            layoutHour.performClick();
                        else if (layoutDay.getBackground() != null)
                            layoutDay.performClick();
                        else if (layoutWeek.getBackground() != null)
                            layoutWeek.performClick();
                        else if (layoutMonth.getBackground() != null)
                            layoutMonth.performClick();
                        else layoutYear.performClick();
                    } else {
                        if (ConnectivityController.isNetworkAvailable(MoonPageActivity.this))
                            callGetLaunchedMoonsApi(false);
                        else
                            AndroidUtils.showToast(MoonPageActivity.this, getResources().getString(R.string.no_internet));
                    }
                }

                break;
            case EDIT_UNLAUNCH_MOON_RESULT_CODE:
                if (resultCode == RESULT_OK) {
                    if (infoPopUpFragment != null)
                        infoPopUpFragment.dismiss();
                    MoonResponseModel moonResponseModel;
                    if (data != null && data.getParcelableExtra(ConstantUtils.MOON_RESPONSE_MODEL) != null) {
                        moonResponseModel = data.getParcelableExtra(ConstantUtils.MOON_RESPONSE_MODEL);

                        if (moonResponseModel != null && moonResponseModel.getStart_date() != null && moonResponseModel.getFinish_date() != null) {
                            unLaunchedMoonsList.remove(data.getIntExtra(ConstantUtils.TASK_POSITION, 0));
                            unLaunchedMoonsAdapter.notifyDataSetChanged();

                            launchedMoonsList.add(moonResponseModel);


                            if (layoutHour.getBackground() != null)
                                layoutHour.performClick();
                            else if (layoutDay.getBackground() != null)
                                layoutDay.performClick();
                            else if (layoutWeek.getBackground() != null)
                                layoutWeek.performClick();
                            else if (layoutMonth.getBackground() != null)
                                layoutMonth.performClick();
                            else layoutYear.performClick();
                        } else if (moonResponseModel != null) {
                            unLaunchedMoonsList.remove(data.getIntExtra(ConstantUtils.TASK_POSITION, 0));
                            unLaunchedMoonsList.add(data.getIntExtra(ConstantUtils.TASK_POSITION, 0), moonResponseModel);
                            unLaunchedMoonsAdapter.notifyDataSetChanged();
                        }

                    } else {
                        if (ConnectivityController.isNetworkAvailable(MoonPageActivity.this)) {
                            callGetUnLaunchedMoonsApi(false);
                            callGetLaunchedMoonsApi(false);

                        } else
                            AndroidUtils.showToast(MoonPageActivity.this, getResources().getString(R.string.no_internet));
                    }
                }
                break;
            case BACK_REQUEST_CODE:
                if (resultCode == RESULT_FIRST_USER) {
                    finish();
                } else if (resultCode == RESULT_OK) {
                    setResult(RESULT_OK);
                    finish();
                } else if (resultCode == ConstantUtils.RESULT_UPDATE) {
                    if (data != null && data.getParcelableExtra(ConstantUtils.MOON_RESPONSE_MODEL) != null) {

                        launchedMoonsList.remove(data.getIntExtra(ConstantUtils.TASK_POSITION, 0));
                        launchedMoonsList.add(data.getIntExtra(ConstantUtils.TASK_POSITION, 0), (MoonResponseModel) data.getParcelableExtra(ConstantUtils.MOON_RESPONSE_MODEL));


                        if (layoutHour.getBackground() != null)
                            layoutHour.performClick();
                        else if (layoutDay.getBackground() != null)
                            layoutDay.performClick();
                        else if (layoutWeek.getBackground() != null)
                            layoutWeek.performClick();
                        else if (layoutMonth.getBackground() != null)
                            layoutMonth.performClick();
                        else layoutYear.performClick();
                    } else {
                        if (ConnectivityController.isNetworkAvailable(MoonPageActivity.this))
                            callGetLaunchedMoonsApi(false);
                        else
                            AndroidUtils.showToast(MoonPageActivity.this, getResources().getString(R.string.no_internet));
                    }
                }
                break;
            case EDIT_LAUNCH_PLANET_REQUEST_CODE:
                if (resultCode == RESULT_OK && data != null) {
                    planetResponseModel = data.getParcelableExtra(ConstantUtils.PLANET_RESPONSE_MODEL);
                    if (planetResponseModel != null) {
                        if (planetResponseModel.getName() != null) {
                            txtMiddlePlanet.setText(planetResponseModel.getName());
                            txtPlanetName.setText(planetResponseModel.getName());
                        }
                    }
                }
                break;
        }


    }

    @Override
    public void onTaskClick(View view, int position, String source) {
        if (source.equals("archive"))
            quickActionArchive.show(view);
        else
            quickActionUnLaunch.show(view);
        this.moonPosition = position;
    }

    @Override
    public void onItemClick(ActionItem item) {
        switch (item.getActionId()) {
            case 1:

                Intent activityIntent = new Intent(MoonPageActivity.this, AddTaskActivity.class);
                activityIntent.putExtra(ConstantUtils.PURPOSE, "update");
                activityIntent.putExtra(ConstantUtils.SOURCE, "moon");
                activityIntent.putExtra(ConstantUtils.TASK_POSITION, moonPosition);
                activityIntent.putExtra(ConstantUtils.MOON_RESPONSE_MODEL, unLaunchedMoonsList.get(moonPosition));
                startActivityForResult(activityIntent, EDIT_UNLAUNCH_MOON_RESULT_CODE);
                overridePendingTransition(R.anim.slide_in_up, R.anim.stay);

                break;
            case 2:
                if (ConnectivityController.isNetworkAvailable(MoonPageActivity.this)) {
                    if (popupUnLaunched.getVisibility() == View.VISIBLE) {

                        callDeleteMoonApi("unLaunch", unLaunchedMoonsList.get(moonPosition).getMoon_id());
                    } else {
                        callDeleteMoonApi("archive", archivedMoonsList.get(moonPosition).getMoon_id());
                    }

                } else
                    AndroidUtils.showToast(MoonPageActivity.this, getResources().getString(R.string.no_internet));
                break;
            case 3:
                Intent previewIntent = new Intent(MoonPageActivity.this, AddTaskActivity.class);
                previewIntent.putExtra(ConstantUtils.PURPOSE, "preview");
                previewIntent.putExtra(ConstantUtils.SOURCE, "moon");
                previewIntent.putExtra(ConstantUtils.MOON_RESPONSE_MODEL, archivedMoonsList.get(moonPosition));
                startActivity(previewIntent);
                overridePendingTransition(R.anim.slide_in_up, R.anim.stay);
                break;
        }
    }

    private void callDeleteMoonApi(final String source, final String moon_id) {
        progressDialog.show();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetMessageModel> deleteMoonResponseCall = apiService.deleteTask(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), moon_id, "M");
        deleteMoonResponseCall.enqueue(new Callback<GetMessageModel>() {
            @Override
            public void onResponse(Call<GetMessageModel> call, Response<GetMessageModel> response) {
                progressDialog.dismiss();
                if (response.code() == 200 && response.body() != null) {
                    if (response.body().getMessage() != null)
                        AndroidUtils.showToast(MoonPageActivity.this, response.body().getMessage());

                    if (source.equals("unLaunch")) {
                        unLaunchedMoonsList.remove(moonPosition);
                        unLaunchedMoonsAdapter.notifyDataSetChanged();
                    } else if (source.equals("archive")) {
                        archivedMoonsList.remove(moonPosition);
                        archivedMoonsAdapter.notifyDataSetChanged();
                    } else {
                        launchedMoonsList.remove(moonPosition);


                        if (layoutHour.getBackground() != null)
                            layoutHour.performClick();
                        else if (layoutDay.getBackground() != null)
                            layoutDay.performClick();
                        else if (layoutWeek.getBackground() != null)
                            layoutWeek.performClick();
                        else if (layoutMonth.getBackground() != null)
                            layoutMonth.performClick();
                        else layoutYear.performClick();
                    }


                } else if (response.code() == 500) {
                    AndroidUtils.showToast(MoonPageActivity.this, getResources().getString(R.string.some_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<GetMessageModel> call, Throwable t) {
                progressDialog.dismiss();
                AndroidUtils.showToast(MoonPageActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });
    }

    @Override
    public boolean onLongClick(View v) {

        ClipData data = ClipData.newPlainText("", "");
        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                v);
        v.startDrag(data, shadowBuilder, v, 0);

        return true;
    }

    @Override
    public void onEditTaskClick(int position, String source) {
        Intent activityIntent = new Intent(MoonPageActivity.this, AddTaskActivity.class);
        activityIntent.putExtra(ConstantUtils.PURPOSE, "update");
        activityIntent.putExtra(ConstantUtils.SOURCE, "moon");
        activityIntent.putExtra(ConstantUtils.TASK_POSITION, position);
        activityIntent.putExtra(ConstantUtils.MOON_RESPONSE_MODEL, launchedMoonsList.get(position));
        startActivityForResult(activityIntent, EDIT_LAUNCH_MOON_RESULT_CODE);
        overridePendingTransition(R.anim.slide_in_up, R.anim.stay);
    }

    @Override
    public void onDeleteTaskClick(int position, String source) {
        if (infoPopUpFragment != null)
            infoPopUpFragment.dismiss();
        this.moonPosition = position;
        if (ConnectivityController.isNetworkAvailable(MoonPageActivity.this))
            callDeleteMoonApi("launch", launchedMoonsList.get(position).getMoon_id());
        else
            AndroidUtils.showToast(MoonPageActivity.this, getResources().getString(R.string.no_internet));
    }

    @Override
    public void onStorageTaskClick(int position) {
        Intent intent = new Intent(MoonPageActivity.this, StarStorageActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDetailsClick(int position, String source) {
        Intent intent = new Intent(MoonPageActivity.this, DetailPageActivity.class);
        intent.putExtra(ConstantUtils.ENTITY_ID, launchedMoonsList.get(position).getMoon_id());
        intent.putExtra(ConstantUtils.ENTITY_TYPE, "M");
        startActivity(intent);
    }

    @Override
    public void onMarkDoneClick(int position, String source) {
        if (infoPopUpFragment != null)
            infoPopUpFragment.dismiss();
        if (ConnectivityController.isNetworkAvailable(MoonPageActivity.this))
            callMarkTaskDoneApi(position);
        else
            AndroidUtils.showToast(MoonPageActivity.this, getResources().getString(R.string.no_internet));
    }

    private void callMarkTaskDoneApi(final int position) {
        progressDialog.show();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetMessageModel> markTaskDoneReposneCall = apiService.markTaskDone(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), launchedMoonsList.get(position).getMoon_id(), "M");
        markTaskDoneReposneCall.enqueue(new Callback<GetMessageModel>() {
            @Override
            public void onResponse(Call<GetMessageModel> call, Response<GetMessageModel> response) {
                progressDialog.dismiss();
                if (response.code() == 200 && response.body() != null) {
                    if (response.body().getMessage() != null)
                        AndroidUtils.showToast(MoonPageActivity.this, response.body().getMessage());

                    launchedMoonsList.remove(position);


                    if (layoutHour.getBackground() != null)
                        layoutHour.performClick();
                    else if (layoutDay.getBackground() != null)
                        layoutDay.performClick();
                    else if (layoutWeek.getBackground() != null)
                        layoutWeek.performClick();
                    else if (layoutMonth.getBackground() != null)
                        layoutMonth.performClick();
                    else layoutYear.performClick();
                } else if (response.code() == 500)
                    AndroidUtils.showToast(MoonPageActivity.this, getResources().getString(R.string.some_went_wrong));

            }

            @Override
            public void onFailure(Call<GetMessageModel> call, Throwable t) {
                progressDialog.dismiss();
                AndroidUtils.showToast(MoonPageActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        intent.putExtra(ConstantUtils.PLANET_RESPONSE_MODEL, planetResponseModel);
        intent.putExtra(ConstantUtils.TASK_POSITION, taskPosition);
        setResult(ConstantUtils.RESULT_UPDATE, intent);
        finish();
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:

                return true;

            case DragEvent.ACTION_DRAG_ENTERED:
                //  v.setBackgroundColor(Color.LTGRAY);
                return true;

            case DragEvent.ACTION_DRAG_LOCATION:
                //og.e("DragStared", "onDrag: " + event.getX());
                //Log.e("DragStared", "onDrag: " + event.getY());
                return true;

            case DragEvent.ACTION_DRAG_EXITED:
                //  v.setBackgroundColor(Color.TRANSPARENT);
                return true;

            case DragEvent.ACTION_DROP:

                double dist = entityImageRadius;
                int NewOrbitNo = -1;

                for (int i = 0; i < coordinatesOfOrbit1.size(); i++) {
                    double pointX = coordinatesOfOrbit1.get(i).getX() - event.getX();
                    double pointY = coordinatesOfOrbit1.get(i).getY() - event.getY();
                    double distance = Math.sqrt(pointX * pointX + pointY * pointY);

                    if (distance < dist && distance > 0) {
                        dist = distance;
                        NewOrbitNo = 1;
                    }

                }

                for (int i = 0; i < coordinatesOfOrbit2.size(); i++) {
                    double pointX = coordinatesOfOrbit2.get(i).getX() - event.getX();
                    double pointY = coordinatesOfOrbit2.get(i).getY() - event.getY();
                    double distance = Math.sqrt(pointX * pointX + pointY * pointY);

                    if (distance < dist && distance > 0) {
                        dist = distance;
                        NewOrbitNo = 2;
                    }

                }

                for (int i = 0; i < coordinatesOfOrbit3.size(); i++) {
                    double pointX = coordinatesOfOrbit3.get(i).getX() - event.getX();
                    double pointY = coordinatesOfOrbit3.get(i).getY() - event.getY();
                    double distance = Math.sqrt(pointX * pointX + pointY * pointY);

                    if (distance < dist && distance > 0) {
                        dist = distance;
                        NewOrbitNo = 3;
                    }

                }

                for (int i = 0; i < coordinatesOfOrbit4.size(); i++) {
                    double pointX = coordinatesOfOrbit4.get(i).getX() - event.getX();
                    double pointY = coordinatesOfOrbit4.get(i).getY() - event.getY();
                    double distance = Math.sqrt(pointX * pointX + pointY * pointY);

                    if (distance < dist && distance > 0) {
                        dist = distance;
                        NewOrbitNo = 4;
                    }

                }
                Log.e("Drop", " index " + NewOrbitNo);
                Calendar calendar = Calendar.getInstance();
                if (NewOrbitNo != -1) {
                    View view = (View) event.getLocalState();
                    ViewGroup owner = (ViewGroup) view.getParent();
                    owner.removeView(view);
                    view.setX(event.getX() - (view.getWidth() / 2));
                    view.setY(event.getY() - (view.getWidth() / 2));
                  /*  layoutCustomView.addView(view);
                    view.setVisibility(View.VISIBLE);*/
                    int PrevOrbitNo = (int) view.getTag();

                    for (int i = 0; i < launchedMoonsList.size(); i++) {
                        if (launchedMoonsList.get(i).getMoon_id() != null && view.getId() == Integer.parseInt(launchedMoonsList.get(i).getMoon_id())) {
                            try {
                                if(launchedMoonsList.get(i).getFinish_time()!=null)
                                calendar.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(launchedMoonsList.get(i).getFinish_date() + " " + launchedMoonsList.get(i).getFinish_time()));
                                else calendar.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(launchedMoonsList.get(i).getFinish_date() ));

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            MoonResponseModel moonResponseModel = launchedMoonsList.get(i);
                            MoonRequestModel moonRequestModel = new MoonRequestModel();
                            moonRequestModel.setStar_id(moonResponseModel.getStar_id());
                            moonRequestModel.setPlanet_id(moonResponseModel.getPlanet_id());
                            moonRequestModel.setMoon_id(moonResponseModel.getMoon_id());
                            moonRequestModel.setName(moonResponseModel.getName());
                            moonRequestModel.setDescription(moonResponseModel.getDescription());
                            moonRequestModel.setStart_date(moonResponseModel.getStart_date());
                            moonRequestModel.setStart_time(moonResponseModel.getStart_time());
                            moonRequestModel.setStart_time(moonResponseModel.getFinish_time());
                            moonRequestModel.setAlarm_date(moonResponseModel.getAlarm_date());
                            moonRequestModel.setAlarm_time(moonResponseModel.getAlarm_time());
                            moonRequestModel.setNotes(moonResponseModel.getNotes());
                            moonRequestModel.setPriority(moonResponseModel.getPriority());

                            int diff;
                            if(isDoStars)
                                diff=NewOrbitNo-PrevOrbitNo;
                            else diff=PrevOrbitNo-NewOrbitNo;

                            if (layoutHour.getBackground() != null) {
                                calendar.add(Calendar.HOUR, diff * 3);
                                moonRequestModel.setFinish_date(moonResponseModel.getFinish_date());
                                moonRequestModel.setFinish_time(new SimpleDateFormat("HH:mm").format(calendar.getTime()));

                            } else {
                                if (layoutDay.getBackground() != null) {
                                    calendar.add(Calendar.DAY_OF_YEAR, diff);
                                } else if (layoutWeek.getBackground() != null) {
                                    calendar.add(Calendar.WEEK_OF_YEAR, diff);
                                } else if (layoutMonth.getBackground() != null) {
                                    calendar.add(Calendar.MONTH, diff);
                                } else if (layoutYear.getBackground() != null) {
                                    calendar.add(Calendar.YEAR, diff);
                                }
                                moonRequestModel.setFinish_date(new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()));
                                moonRequestModel.setFinish_time(moonResponseModel.getFinish_time());
                            }




                            if (ConnectivityController.isNetworkAvailable(MoonPageActivity.this))
                                callUpdateMoonApi(moonRequestModel);
                            else
                                AndroidUtils.showToast(MoonPageActivity.this, getResources().getString(R.string.no_internet));


                        }
                    }


                }


                return true;

            case DragEvent.ACTION_DRAG_ENDED:
                Log.e("ACTION_DRAG_ENDED", "dragEnded: ");
                  /*   View view1 = (View) event.getLocalState();
                     view1.setVisibility(View.VISIBLE);*/
                return true;

            default:
                break;
        }
        return false;
    }

    private void callUpdateMoonApi(MoonRequestModel moonRequestModel) {
        progressDialog.show();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<MoonResponseModel> moonResponseModelCall = apiService.updateMoon(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), moonRequestModel);
        moonResponseModelCall.enqueue(new Callback<MoonResponseModel>() {
            @Override
            public void onResponse(Call<MoonResponseModel> call, Response<MoonResponseModel> response) {
                progressDialog.dismiss();
                if (response.code() == 200 && response.body() != null) {
                    AndroidUtils.showToast(MoonPageActivity.this, "Moon has been updated successfully");
                    if (ConnectivityController.isNetworkAvailable(MoonPageActivity.this))
                        callGetLaunchedMoonsApi(false);
                    else
                        AndroidUtils.showToast(MoonPageActivity.this, getResources().getString(R.string.no_internet));
                } else if (response.code() == 500) {
                    AndroidUtils.showToast(MoonPageActivity.this, getResources().getString(R.string.some_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<MoonResponseModel> call, Throwable t) {
                progressDialog.dismiss();
                AndroidUtils.showToast(MoonPageActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });
    }

    @Override
    public void onCloseClick() {
        if (layoutHour.getBackground() != null)
            layoutHour.performClick();
        else if (layoutDay.getBackground() != null)
            layoutDay.performClick();
        else if (layoutWeek.getBackground() != null)
            layoutWeek.performClick();
        else if (layoutMonth.getBackground() != null)
            layoutMonth.performClick();
        else layoutYear.performClick();
    }

    @Override
    public void afterUpdate() {
        if (ConnectivityController.isNetworkAvailable(MoonPageActivity.this))
            callGetLaunchedMoonsApi(false);
        else
            AndroidUtils.showToast(MoonPageActivity.this, getResources().getString(R.string.no_internet));
    }
}
