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
import com.android.yuniapp.adapter.ArchivedSatellitesAdapter;
import com.android.yuniapp.adapter.UnLaunchedSatellitesAdapter;
import com.android.yuniapp.fragment.DropEntityPopUpFragment;
import com.android.yuniapp.fragment.InfoPopUpFragment;
import com.android.yuniapp.listener.DropEntityPopUpListener;
import com.android.yuniapp.listener.InfoPopUpEventListener;
import com.android.yuniapp.listener.PopupWindowListener;
import com.android.yuniapp.model.EntitiesCoordinatesModel;
import com.android.yuniapp.model.EntitiesModel;
import com.android.yuniapp.model.GetMessageModel;
import com.android.yuniapp.model.GetSatelliteResponseModel;
import com.android.yuniapp.model.MoonResponseModel;
import com.android.yuniapp.model.PlanetRequestModel;
import com.android.yuniapp.model.PlanetResponseModel;
import com.android.yuniapp.model.SatelliteRequestModel;
import com.android.yuniapp.model.SatelliteResponseModel;
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

public class SatellitePageActivity extends AppCompatActivity implements View.OnClickListener, PopupWindowListener, QuickAction.OnActionItemClickListener, View.OnLongClickListener, InfoPopUpEventListener, View.OnDragListener, DropEntityPopUpListener {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private TextView txtMiddleMoon, txtStarName, txtPlanetName, txtMoonName;
    private ImageView imgBottomMoon, imgBack;
    private RelativeLayout layoutHour, layoutDay, layoutWeek, layoutMonth, layoutYear;
    private RelativeLayout layoutCustomView;
    private ProgressBar progressBar;
    private Dialog progressDialog;
    private TextView txtDoStars, txtExceedStars;
    private View viewDoStars, viewExceedStars;
    private boolean isDoStars = true;
    private float centerX, centerY;
    private String moonId = "";
    private QuickAction quickActionUnLaunch, quickActionArchive;
    private int satellitePosition;
    private InfoPopUpFragment infoPopUpFragment;
    private SpiralView spiralView;
    //Moonupdate Related
    private MoonResponseModel moonResponseModel;
    private int taskPosition;

    //Popup's
    private View viewDim;
    private RecyclerView recyclerViewUnLaunched, recyclerViewArchived;
    private UnLaunchedSatellitesAdapter unLaunchedSatellitesAdapter;
    private ArchivedSatellitesAdapter archivedSatellitesAdapter;
    private RelativeLayout popupUnLaunched, popupArchived;
    private ProgressBar progressBarUnLaunched, progressBarArchived;

    //ArrayLists
    private ArrayList<SatelliteResponseModel> unLaunchedSatellitesList = new ArrayList<>();
    private ArrayList<SatelliteResponseModel> archivedSatellitesList = new ArrayList<>();
    private ArrayList<SatelliteResponseModel> launchedSatellitesList = new ArrayList<>();
    private ArrayList<SatelliteResponseModel> entriesToShowInOrbit1 = new ArrayList<>();
    private ArrayList<SatelliteResponseModel> entriesToShowInOrbit2 = new ArrayList<>();
    private ArrayList<SatelliteResponseModel> entriesToShowInOrbit3 = new ArrayList<>();
    private ArrayList<SatelliteResponseModel> entriesToShowInOrbit4 = new ArrayList<>();
    private ArrayList<EntitiesCoordinatesModel> equidistantPointsOfOrbits = new ArrayList<>();
    private ArrayList<EntitiesModel> entriesToShow = new ArrayList<>();
    private ArrayList<EntitiesCoordinatesModel> coordinatesOfOrbit1 = new ArrayList<>();
    private ArrayList<EntitiesCoordinatesModel> coordinatesOfOrbit2 = new ArrayList<>();
    private ArrayList<EntitiesCoordinatesModel> coordinatesOfOrbit3 = new ArrayList<>();
    private ArrayList<EntitiesCoordinatesModel> coordinatesOfOrbit4 = new ArrayList<>();

    //Constants
    private static final int BACK_REQUEST_CODE = 49;
    private static final int EDIT_LAUNCH_SATELLITE_RESULT_CODE = 50;
    private static final int EDIT_UNLAUNCH_SATELLITE_RESULT_CODE = 51;
    private static final int EDIT_LAUNCH_MOON_REQUEST_CODE = 53;
    private int entityImageRadius;
    private int noOfOrbits = 5, noOfSegments;

    //DoubleClick
    private final int doubleClickTimeout = ViewConfiguration.getDoubleTapTimeout();
    private Handler handler;
    private long firstClickTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_satellite_page);

        firstClickTime = 0L;
        handler = new Handler(Looper.getMainLooper());
        sharedPreferences = getSharedPreferences(ConstantUtils.APP_PREF, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        AppUtils.setToolbarWithProfileImage(this, "Satellite Page", "(Miscellaneous)", sharedPreferences.getString(ConstantUtils.USER_PROFILE, ""));

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
        unLaunchedSatellitesAdapter = new UnLaunchedSatellitesAdapter(SatellitePageActivity.this, SatellitePageActivity.this, unLaunchedSatellitesList);
        recyclerViewUnLaunched.setAdapter(unLaunchedSatellitesAdapter);

        archivedSatellitesAdapter = new ArchivedSatellitesAdapter(SatellitePageActivity.this, SatellitePageActivity.this, archivedSatellitesList);
        recyclerViewArchived.setAdapter(archivedSatellitesAdapter);

        layoutCustomView.setOnDragListener(this);

        if (getIntent() != null) {


            if (getIntent().getParcelableExtra(ConstantUtils.MOON_RESPONSE_MODEL) != null) {
                moonResponseModel = getIntent().getParcelableExtra(ConstantUtils.MOON_RESPONSE_MODEL);

                if (moonResponseModel.getName() != null) {
                    txtMiddleMoon.setText(moonResponseModel.getName());
                    txtMoonName.setText(moonResponseModel.getName());
                }
                moonId = moonResponseModel.getMoon_id();

            }
            taskPosition = getIntent().getIntExtra(ConstantUtils.TASK_POSITION, 0);

            if (getIntent().getStringExtra(ConstantUtils.STAR_NAME) != null) {
                txtStarName.setText(getIntent().getStringExtra(ConstantUtils.STAR_NAME));
            }
            if (getIntent().getStringExtra(ConstantUtils.STAR_NAME) != null) {
                txtPlanetName.setText(getIntent().getStringExtra(ConstantUtils.PLANET_NAME) + ">");
            }

        /*    if (getIntent().getStringExtra(ConstantUtils.MOON_NAME) != null)
            {
                txtMiddleMoon.setText(getIntent().getStringExtra(ConstantUtils.MOON_NAME));
                txtMoonName.setText(getIntent().getStringExtra(ConstantUtils.MOON_NAME));

            }

            if(getIntent().getStringExtra(ConstantUtils.MOON_ID)!=null && !getIntent().getStringExtra(ConstantUtils.MOON_ID).isEmpty())
                moonId=getIntent().getStringExtra(ConstantUtils.MOON_ID);*/
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
                if (ConnectivityController.isNetworkAvailable(SatellitePageActivity.this)) {
                    if (moonId != null && !moonId.isEmpty())
                        callGetLaunchedSatellitesApi(true);
                } else
                    AndroidUtils.showToast(SatellitePageActivity.this, getResources().getString(R.string.no_internet));

            }
        });
    }


    private void initViews() {
        txtStarName = findViewById(R.id.txt_star_name);
        txtPlanetName = findViewById(R.id.txt_planet_name);
        txtMoonName = findViewById(R.id.txt_moon_name);
        progressBar = findViewById(R.id.progress_bar);
        layoutCustomView = findViewById(R.id.layout_custom_view);
        txtMiddleMoon = findViewById(R.id.txt_middle_moon);
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
        findViewById(R.id.img_unlaunched_satellites).setOnClickListener(this);
        findViewById(R.id.img_archive_satellites).setOnClickListener(this);
        findViewById(R.id.img_add_satellite).setOnClickListener(this);
        txtMiddleMoon.setOnClickListener(this);
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
        txtMoonName.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.img_profile:
                startActivityForResult(new Intent(SatellitePageActivity.this, ProfileActivity.class), BACK_REQUEST_CODE);
                break;
            case R.id.txt_star_name:
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.txt_planet_name:
                setResult(RESULT_FIRST_USER);
                finish();
                break;
            case R.id.txt_moon_name:
                onBackPressed();
                break;
            case R.id.txt_middle_moon:
                intent = new Intent(SatellitePageActivity.this, AddTaskActivity.class);
                intent.putExtra(ConstantUtils.PURPOSE, "update");
                intent.putExtra(ConstantUtils.SOURCE, "moon");
                intent.putExtra(ConstantUtils.MOON_RESPONSE_MODEL, moonResponseModel);
                startActivityForResult(intent, EDIT_LAUNCH_MOON_REQUEST_CODE);
                overridePendingTransition(R.anim.slide_in_up, R.anim.stay);
                break;
            case R.id.img_add_satellite:
                Intent activityIntent = new Intent(SatellitePageActivity.this, AddTaskActivity.class);
                activityIntent.putExtra(ConstantUtils.SOURCE, "satellite");
                activityIntent.putExtra(ConstantUtils.MOON_ID, moonId);
                startActivityForResult(activityIntent, 104);
                overridePendingTransition(R.anim.slide_in_up, R.anim.stay);
                break;
            case R.id.img_unlaunched_satellites:
                popupUnLaunched.setVisibility(View.VISIBLE);
                popupArchived.setVisibility(View.GONE);
                viewDim.setVisibility(View.VISIBLE);

                if (unLaunchedSatellitesList.isEmpty())
                    callGetUnLaunchedSatellitesApi(true);
                else callGetUnLaunchedSatellitesApi(false);
                break;
            case R.id.img_archive_satellites:
                popupUnLaunched.setVisibility(View.GONE);
                popupArchived.setVisibility(View.VISIBLE);
                viewDim.setVisibility(View.VISIBLE);

                if (archivedSatellitesList.isEmpty())
                    callGetArchivedSatellitesApi(true);
                else callGetArchivedSatellitesApi(false);

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

                for (int i = 0; i < launchedSatellitesList.size(); i++) {
                    SatelliteResponseModel dataResponseModel = launchedSatellitesList.get(i);
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
                for (int i = 0; i < launchedSatellitesList.size(); i++) {
                    SatelliteResponseModel dataResponseModel = launchedSatellitesList.get(i);
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
                for (int i = 0; i < launchedSatellitesList.size(); i++) {
                    SatelliteResponseModel dataResponseModel = launchedSatellitesList.get(i);
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
                for (int i = 0; i < launchedSatellitesList.size(); i++) {
                    SatelliteResponseModel dataResponseModel = launchedSatellitesList.get(i);
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

                for (int i = 0; i < launchedSatellitesList.size(); i++) {
                    SatelliteResponseModel dataResponseModel = launchedSatellitesList.get(i);
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
                    infoPopUpFragment = new InfoPopUpFragment();
                    Bundle bundle = new Bundle();
                    for (int i = 0; i < launchedSatellitesList.size(); i++) {
                        if (launchedSatellitesList.get(i).getSatellite_id() != null && v.getId() == Integer.parseInt(launchedSatellitesList.get(i).getSatellite_id())) {
                            bundle.putParcelable(ConstantUtils.SATELLITE_RESPONSE_MODEL, launchedSatellitesList.get(i));
                            bundle.putInt(ConstantUtils.TASK_POSITION, i);
                        }
                    }
                    infoPopUpFragment.setArguments(bundle);
                    infoPopUpFragment.setInfoPopUpListener(this);
                    infoPopUpFragment.show(getSupportFragmentManager(), infoPopUpFragment.getTag());
                    infoPopUpFragment.setCancelable(false);
                } else {
                    firstClickTime = now;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //   onSingleClick(v);
                            firstClickTime = 0L;
                        }
                    }, doubleClickTimeout);
                }


                break;
        }

    }


    private void callGetLaunchedSatellitesApi(boolean needLoader) {
        if (needLoader)
            progressBar.setVisibility(View.VISIBLE);
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetSatelliteResponseModel> getLaunchedSatelliteResponseCal = apiService.getLaunchedSatellites(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), moonId);
        getLaunchedSatelliteResponseCal.enqueue(new Callback<GetSatelliteResponseModel>() {
            @Override
            public void onResponse(Call<GetSatelliteResponseModel> call, Response<GetSatelliteResponseModel> response) {
                progressBar.setVisibility(View.GONE);
                if (response.code() == 200 && response.body() != null) {
                    if (response.body().getSatellites() != null && !response.body().getSatellites().isEmpty()) {
                        launchedSatellitesList.clear();
                        launchedSatellitesList.addAll(response.body().getSatellites());

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
                    AndroidUtils.showToast(SatellitePageActivity.this, getResources().getString(R.string.some_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<GetSatelliteResponseModel> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                AndroidUtils.showToast(SatellitePageActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });

    }

    private void callGetUnLaunchedSatellitesApi(boolean needLoader) {
        if (needLoader)
            progressBarUnLaunched.setVisibility(View.VISIBLE);
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetSatelliteResponseModel> getSatelliteResponseModelCall = apiService.getUnLaunchedSatellites(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), moonId);
        getSatelliteResponseModelCall.enqueue(new Callback<GetSatelliteResponseModel>() {
            @Override
            public void onResponse(Call<GetSatelliteResponseModel> call, Response<GetSatelliteResponseModel> response) {
                progressBarUnLaunched.setVisibility(View.GONE);
                if (response.code() == 200 && response.body() != null) {
                    if (response.body().getSatellites() != null && !response.body().getSatellites().isEmpty()) {
                        unLaunchedSatellitesList.clear();
                        unLaunchedSatellitesList.addAll(response.body().getSatellites());
                        unLaunchedSatellitesAdapter.notifyDataSetChanged();
                    } else {
                        popupUnLaunched.setVisibility(View.GONE);
                        viewDim.setVisibility(View.GONE);
                        AndroidUtils.showToast(SatellitePageActivity.this, "No new satellites waiting for dates.");
                    }
                } else if (response.code() == 500) {
                    AndroidUtils.showToast(SatellitePageActivity.this, getResources().getString(R.string.some_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<GetSatelliteResponseModel> call, Throwable t) {
                progressBarUnLaunched.setVisibility(View.GONE);
                AndroidUtils.showToast(SatellitePageActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });
    }

    private void callGetArchivedSatellitesApi(boolean needLoader) {
        if (needLoader)
            progressBarArchived.setVisibility(View.VISIBLE);
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetSatelliteResponseModel> getSatelliteResponseModelCall = apiService.getArchivedSatellites(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), moonId);
        getSatelliteResponseModelCall.enqueue(new Callback<GetSatelliteResponseModel>() {
            @Override
            public void onResponse(Call<GetSatelliteResponseModel> call, Response<GetSatelliteResponseModel> response) {
                progressBarArchived.setVisibility(View.GONE);
                if (response.code() == 200 && response.body() != null) {
                    if (response.body().getSatellites() != null && !response.body().getSatellites().isEmpty()) {
                        archivedSatellitesList.clear();
                        archivedSatellitesList.addAll(response.body().getSatellites());
                        archivedSatellitesAdapter.notifyDataSetChanged();
                    } else {
                        popupArchived.setVisibility(View.GONE);
                        viewDim.setVisibility(View.GONE);
                        AndroidUtils.showToast(SatellitePageActivity.this, "No old satellites.");
                    }
                } else if (response.code() == 500) {
                    AndroidUtils.showToast(SatellitePageActivity.this, getResources().getString(R.string.some_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<GetSatelliteResponseModel> call, Throwable t) {
                progressBarArchived.setVisibility(View.GONE);
                AndroidUtils.showToast(SatellitePageActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });

    }

    private void setDataForOrbit1() {

        TextView orbitEntryImage;
        int noOfEntities = coordinatesOfOrbit1.size();

        for (int i = 0; i < entriesToShowInOrbit1.size(); i++) {
            if (i < noOfEntities - 1) {
                orbitEntryImage = new TextView(SatellitePageActivity.this);
                orbitEntryImage.setLayoutParams(new android.view.ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                orbitEntryImage.setTypeface(ResourcesCompat.getFont(SatellitePageActivity.this, R.font.teko_medium));
                orbitEntryImage.setTextColor(ContextCompat.getColor(SatellitePageActivity.this,R.color.colorBlack));
                orbitEntryImage.setPadding(20, 1, 20, 1);
                orbitEntryImage.setId(Integer.parseInt(entriesToShowInOrbit1.get(i).getSatellite_id()));
                orbitEntryImage.setText(entriesToShowInOrbit1.get(i).getName());
                //orbitEntryImage.setEllipsize(TextUtils.TruncateAt.END);
                orbitEntryImage.setTextSize(9f);
                orbitEntryImage.setGravity(Gravity.CENTER);
                orbitEntryImage.setMaxLines(1);
                orbitEntryImage.setTag(1);
                setEntityImage(orbitEntryImage, 1, Integer.valueOf(entriesToShowInOrbit1.get(i).getPriority()) , entriesToShowInOrbit1.get(i).getStart_date(), entriesToShowInOrbit1.get(i).getFinish_date());
                orbitEntryImage.setX((float) (coordinatesOfOrbit1.get(i).getX() - entityImageRadius / 2));
                orbitEntryImage.setY((float) (coordinatesOfOrbit1.get(i).getY() - entityImageRadius / 2));
                orbitEntryImage.setOnClickListener(this);
                orbitEntryImage.setOnLongClickListener(this);
                layoutCustomView.addView(orbitEntryImage);

            } else if (entriesToShowInOrbit1.size() > noOfEntities) {
                orbitEntryImage = new TextView(SatellitePageActivity.this);
                orbitEntryImage.setLayoutParams(new android.view.ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                orbitEntryImage.setTypeface(ResourcesCompat.getFont(SatellitePageActivity.this, R.font.teko_medium));
                orbitEntryImage.setTextColor(ContextCompat.getColor(SatellitePageActivity.this,R.color.colorBlack));
                orbitEntryImage.setPadding(10, entityImageRadius / 4, 10, 1);
                orbitEntryImage.setText(entriesToShowInOrbit1.size() - noOfEntities + "+");
                orbitEntryImage.setTextSize(8f);
                orbitEntryImage.setGravity(Gravity.CENTER_HORIZONTAL);
                orbitEntryImage.setMaxLines(1);
                orbitEntryImage.setBackgroundResource(R.drawable.moon_overlay);
                orbitEntryImage.setX((float) (coordinatesOfOrbit1.get(i).getX() - entityImageRadius / 2));
                orbitEntryImage.setY((float) (coordinatesOfOrbit1.get(i).getY() - entityImageRadius / 2));
                orbitEntryImage.setId(Integer.parseInt(entriesToShowInOrbit1.get(i).getSatellite_id()));
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
                orbitEntryImage = new TextView(SatellitePageActivity.this);
                orbitEntryImage.setLayoutParams(new android.view.ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                orbitEntryImage.setTypeface(ResourcesCompat.getFont(SatellitePageActivity.this, R.font.teko_medium));
                orbitEntryImage.setTextColor(ContextCompat.getColor(SatellitePageActivity.this,R.color.colorBlack));
                orbitEntryImage.setPadding(20, 1, 20, 1);
                orbitEntryImage.setId(Integer.parseInt(entriesToShowInOrbit2.get(i).getSatellite_id()));
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
                orbitEntryImage = new TextView(SatellitePageActivity.this);
                orbitEntryImage.setLayoutParams(new android.view.ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                orbitEntryImage.setTypeface(ResourcesCompat.getFont(SatellitePageActivity.this, R.font.teko_medium));
                orbitEntryImage.setTextColor(ContextCompat.getColor(SatellitePageActivity.this,R.color.colorBlack));
                orbitEntryImage.setPadding(10, entityImageRadius / 4, 10, 1);
                orbitEntryImage.setText(entriesToShowInOrbit2.size() - noOfEntities + "+");
                //orbitEntryImage.setEllipsize(TextUtils.TruncateAt.END);
                orbitEntryImage.setTextSize(8f);
                orbitEntryImage.setGravity(Gravity.CENTER_HORIZONTAL);
                orbitEntryImage.setMaxLines(1);
                orbitEntryImage.setBackgroundResource(R.drawable.moon_overlay);
                orbitEntryImage.setX((float) (coordinatesOfOrbit1.get(i).getX() - entityImageRadius / 2));
                orbitEntryImage.setY((float) (coordinatesOfOrbit1.get(i).getY() - entityImageRadius / 2));
                orbitEntryImage.setId(Integer.parseInt(entriesToShowInOrbit2.get(i).getSatellite_id()));
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
                orbitEntryImage = new TextView(SatellitePageActivity.this);
                orbitEntryImage.setLayoutParams(new android.view.ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                orbitEntryImage.setTypeface(ResourcesCompat.getFont(SatellitePageActivity.this, R.font.teko_medium));
                orbitEntryImage.setTextColor(ContextCompat.getColor(SatellitePageActivity.this,R.color.colorBlack));
                orbitEntryImage.setPadding(20, 1, 20, 1);
                orbitEntryImage.setId(Integer.parseInt(entriesToShowInOrbit3.get(i).getSatellite_id()));
                orbitEntryImage.setText(entriesToShowInOrbit3.get(i).getName());
               // orbitEntryImage.setEllipsize(TextUtils.TruncateAt.END);
                orbitEntryImage.setTextSize(9f);
                orbitEntryImage.setGravity(Gravity.CENTER);
                orbitEntryImage.setMaxLines(1);
                orbitEntryImage.setTag(3);
                setEntityImage(orbitEntryImage, 3, Integer.valueOf(entriesToShowInOrbit3.get(i).getPriority()) , entriesToShowInOrbit3.get(i).getStart_date(), entriesToShowInOrbit3.get(i).getFinish_date());
                orbitEntryImage.setX((float) (coordinatesOfOrbit3.get(i).getX() - entityImageRadius / 2));
                orbitEntryImage.setY((float) (coordinatesOfOrbit3.get(i).getY() - entityImageRadius / 2));
                orbitEntryImage.setOnClickListener(this);
                orbitEntryImage.setOnLongClickListener(this);
                layoutCustomView.addView(orbitEntryImage);

            } else if (entriesToShowInOrbit3.size() > noOfEntities) {
                orbitEntryImage = new TextView(SatellitePageActivity.this);
                orbitEntryImage.setLayoutParams(new android.view.ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                orbitEntryImage.setTypeface(ResourcesCompat.getFont(SatellitePageActivity.this, R.font.teko_medium));
                orbitEntryImage.setTextColor(ContextCompat.getColor(SatellitePageActivity.this,R.color.colorBlack));
                orbitEntryImage.setPadding(10, entityImageRadius / 4, 10, 1);
                orbitEntryImage.setText(entriesToShowInOrbit3.size() - noOfEntities + "+");
                orbitEntryImage.setTextSize(8f);
                orbitEntryImage.setGravity(Gravity.CENTER_HORIZONTAL);
                orbitEntryImage.setMaxLines(1);
                orbitEntryImage.setBackgroundResource(R.drawable.moon_overlay);
                orbitEntryImage.setX((float) (coordinatesOfOrbit3.get(i).getX() - entityImageRadius / 2));
                orbitEntryImage.setY((float) (coordinatesOfOrbit3.get(i).getY() - entityImageRadius / 2));
                orbitEntryImage.setId(Integer.parseInt(entriesToShowInOrbit3.get(i).getSatellite_id()));
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
                orbitEntryImage = new TextView(SatellitePageActivity.this);
                orbitEntryImage.setLayoutParams(new android.view.ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                orbitEntryImage.setTypeface(ResourcesCompat.getFont(SatellitePageActivity.this, R.font.teko_medium));
                orbitEntryImage.setTextColor(ContextCompat.getColor(SatellitePageActivity.this,R.color.colorBlack));
                orbitEntryImage.setPadding(20, 1, 20, 1);
                orbitEntryImage.setId(Integer.parseInt(entriesToShowInOrbit4.get(i).getSatellite_id()));
                orbitEntryImage.setText(entriesToShowInOrbit4.get(i).getName());
               // orbitEntryImage.setEllipsize(TextUtils.TruncateAt.END);
                orbitEntryImage.setTextSize(9f);
                orbitEntryImage.setGravity(Gravity.CENTER);
                orbitEntryImage.setMaxLines(1);
                orbitEntryImage.setTag(4);
                setEntityImage(orbitEntryImage, 4, Integer.valueOf(entriesToShowInOrbit4.get(i).getPriority()), entriesToShowInOrbit4.get(i).getStart_date(), entriesToShowInOrbit4.get(i).getFinish_date());
                orbitEntryImage.setX((float) (coordinatesOfOrbit4.get(i).getX() - entityImageRadius / 2));
                orbitEntryImage.setY((float) (coordinatesOfOrbit4.get(i).getY() - entityImageRadius / 2));
                orbitEntryImage.setOnClickListener(this);
                orbitEntryImage.setOnLongClickListener(this);
                layoutCustomView.addView(orbitEntryImage);

            } else if (entriesToShowInOrbit4.size() > noOfEntities) {
                orbitEntryImage = new TextView(SatellitePageActivity.this);
                orbitEntryImage.setLayoutParams(new android.view.ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                orbitEntryImage.setTypeface(ResourcesCompat.getFont(SatellitePageActivity.this, R.font.teko_medium));
                orbitEntryImage.setTextColor(ContextCompat.getColor(SatellitePageActivity.this,R.color.colorBlack));
                orbitEntryImage.setPadding(10, entityImageRadius / 4, 10, 1);
                orbitEntryImage.setText(entriesToShowInOrbit4.size() - noOfEntities + "+");
                orbitEntryImage.setTextSize(8f);
                orbitEntryImage.setGravity(Gravity.CENTER_HORIZONTAL);
                orbitEntryImage.setMaxLines(1);
                orbitEntryImage.setBackgroundResource(R.drawable.moon_overlay);
                orbitEntryImage.setX((float) (coordinatesOfOrbit4.get(i).getX() - entityImageRadius / 2));
                orbitEntryImage.setY((float) (coordinatesOfOrbit4.get(i).getY() - entityImageRadius / 2));
                orbitEntryImage.setId(Integer.parseInt(entriesToShowInOrbit4.get(i).getSatellite_id()));
                layoutCustomView.addView(orbitEntryImage);
                break;

            }
        }
    }

    /*  private void setDataOnSpiral() {
          TextView orbitEntryImage;
          for (int i = 0; i < entriesToShow.size(); i++) {
              if (i <= equidistantPointsOfOrbits.size() - 1) {
                  orbitEntryImage = new TextView(SatellitePageActivity.this);
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
                  orbitEntryImage = new TextView(SatellitePageActivity.this);
                  orbitEntryImage.setLayoutParams(new android.view.ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                  orbitEntryImage.setPadding(20,  1, 20, 1);
                  orbitEntryImage.setText(entriesToShow.size() - equidistantPointsOfOrbits.size() + "+");
                  orbitEntryImage.setEllipsize(TextUtils.TruncateAt.END);
                  orbitEntryImage.setTextSize(7f);
                  orbitEntryImage.setGravity(Gravity.CENTER);
                  orbitEntryImage.setMaxLines(1);
                  orbitEntryImage.setBackgroundResource(R.drawable.satellite_overlay);
                  orbitEntryImage.setX((float) entriesToShow.get(i).getX() - entityImageRadius / 2);
                  orbitEntryImage.setY((float) entriesToShow.get(i).getY() - entityImageRadius / 2);
                  orbitEntryImage.setOnClickListener(this);
                  orbitEntryImage.setOnLongClickListener(this);
                  layoutCustomView.addView(orbitEntryImage);
                  break;
              }
          }
      }*/
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
                        orbitEntryImage.setBackgroundResource(R.drawable.satellite_yellow_priority_1);
                    else
                        orbitEntryImage.setBackgroundResource(R.drawable.satellite_green_priority_1);
                }
                else
                    orbitEntryImage.setBackgroundResource(R.drawable.satellite_green_priority_1);
                break;
            case 2:
                if (isDoStars )
                {
                    if (dateStart.after(currentDate))
                        orbitEntryImage.setBackgroundResource(R.drawable.satellite_yellow_priority_2);
                    else
                        orbitEntryImage.setBackgroundResource(R.drawable.satellite_green_priority_2);
                }
                else
                    orbitEntryImage.setBackgroundResource(R.drawable.satellite_green_priority_2);
                break;
            case 3:
                if (isDoStars )
                {
                    if (dateStart.after(currentDate))
                        orbitEntryImage.setBackgroundResource(R.drawable.satellite_yellow_priority_3);
                    else
                        orbitEntryImage.setBackgroundResource(R.drawable.satellite_green_priority_3);
                }
                else
                    orbitEntryImage.setBackgroundResource(R.drawable.satellite_green_priority_3);
                break;
            case 4:
                if (isDoStars)
                {
                    if (dateStart.after(currentDate))
                        orbitEntryImage.setBackgroundResource(R.drawable.satellite_yellow_priority_4);
                    else
                        orbitEntryImage.setBackgroundResource(R.drawable.satellite_green_priority_4);
                }
                else
                    orbitEntryImage.setBackgroundResource(R.drawable.satellite_green_priority_4);
                break;
            case 5:
                if (isDoStars)
                {
                    if (dateStart.after(currentDate))
                        orbitEntryImage.setBackgroundResource(R.drawable.satellite_yellow_priority_5);
                    else
                        orbitEntryImage.setBackgroundResource(R.drawable.satellite_green_priority_5);
                }
                else
                    orbitEntryImage.setBackgroundResource(R.drawable.satellite_green_priority_5);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 104:
                if (resultCode == RESULT_OK) {
                    if (ConnectivityController.isNetworkAvailable(SatellitePageActivity.this)) {
                        if (moonId != null && !moonId.isEmpty())
                            callGetLaunchedSatellitesApi(false);
                    } else
                        AndroidUtils.showToast(SatellitePageActivity.this, getResources().getString(R.string.no_internet));
                }
                break;
            case EDIT_LAUNCH_SATELLITE_RESULT_CODE:
                if (resultCode == RESULT_OK) {
                    if (infoPopUpFragment != null)
                        infoPopUpFragment.dismiss();
                    if (data != null && data.getParcelableExtra(ConstantUtils.SATELLITE_RESPONSE_MODEL) != null) {

                        launchedSatellitesList.remove(data.getIntExtra(ConstantUtils.TASK_POSITION, 0));
                        launchedSatellitesList.add(data.getIntExtra(ConstantUtils.TASK_POSITION, 0), (SatelliteResponseModel) data.getParcelableExtra(ConstantUtils.SATELLITE_RESPONSE_MODEL));

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
                        if (ConnectivityController.isNetworkAvailable(SatellitePageActivity.this))
                            callGetLaunchedSatellitesApi(false);
                        else
                            AndroidUtils.showToast(SatellitePageActivity.this, getResources().getString(R.string.no_internet));
                    }
                }
                break;
            case EDIT_UNLAUNCH_SATELLITE_RESULT_CODE:
                if (resultCode == RESULT_OK) {
                    if (infoPopUpFragment != null)
                        infoPopUpFragment.dismiss();
                    SatelliteResponseModel satelliteResponseModel;
                    if (data != null && data.getParcelableExtra(ConstantUtils.SATELLITE_RESPONSE_MODEL) != null) {
                        satelliteResponseModel = data.getParcelableExtra(ConstantUtils.SATELLITE_RESPONSE_MODEL);

                        if (satelliteResponseModel != null && satelliteResponseModel.getStart_date() != null && satelliteResponseModel.getFinish_date() != null) {
                            unLaunchedSatellitesList.remove(data.getIntExtra(ConstantUtils.TASK_POSITION, 0));
                            unLaunchedSatellitesAdapter.notifyDataSetChanged();

                            launchedSatellitesList.add(satelliteResponseModel);
                            if (layoutHour.getBackground() != null)
                                layoutHour.performClick();
                            else if (layoutDay.getBackground() != null)
                                layoutDay.performClick();
                            else if (layoutWeek.getBackground() != null)
                                layoutWeek.performClick();
                            else if (layoutMonth.getBackground() != null)
                                layoutMonth.performClick();
                            else layoutYear.performClick();
                        } else if (satelliteResponseModel != null) {
                            unLaunchedSatellitesList.remove(data.getIntExtra(ConstantUtils.TASK_POSITION, 0));
                            unLaunchedSatellitesList.add(data.getIntExtra(ConstantUtils.TASK_POSITION, 0), satelliteResponseModel);
                            unLaunchedSatellitesAdapter.notifyDataSetChanged();
                        }

                    } else {
                        if (ConnectivityController.isNetworkAvailable(SatellitePageActivity.this)) {
                            callGetUnLaunchedSatellitesApi(false);
                            callGetLaunchedSatellitesApi(false);

                        } else
                            AndroidUtils.showToast(SatellitePageActivity.this, getResources().getString(R.string.no_internet));
                    }
                }
                break;
            case BACK_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    setResult(RESULT_OK);
                    finish();
                }
                break;
            case EDIT_LAUNCH_MOON_REQUEST_CODE:

                if (resultCode == RESULT_OK && data != null) {
                    moonResponseModel = data.getParcelableExtra(ConstantUtils.MOON_RESPONSE_MODEL);
                    if (moonResponseModel != null) {
                        if (moonResponseModel.getName() != null) {
                            txtMiddleMoon.setText(moonResponseModel.getName());
                            txtMoonName.setText(moonResponseModel.getName());
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
        this.satellitePosition = position;
    }

    @Override
    public void onItemClick(ActionItem item) {
        switch (item.getActionId()) {
            case 1:

                Intent activityIntent = new Intent(SatellitePageActivity.this, AddTaskActivity.class);
                activityIntent.putExtra(ConstantUtils.PURPOSE, "update");
                activityIntent.putExtra(ConstantUtils.SOURCE, "satellite");
                activityIntent.putExtra(ConstantUtils.TASK_POSITION, satellitePosition);
                activityIntent.putExtra(ConstantUtils.SATELLITE_RESPONSE_MODEL, unLaunchedSatellitesList.get(satellitePosition));
                startActivityForResult(activityIntent, EDIT_UNLAUNCH_SATELLITE_RESULT_CODE);
                overridePendingTransition(R.anim.slide_in_up, R.anim.stay);

                break;
            case 2:
                if (ConnectivityController.isNetworkAvailable(SatellitePageActivity.this)) {
                    if (popupUnLaunched.getVisibility() == View.VISIBLE) {

                        callDeleteSatelliteApi("unLaunch", unLaunchedSatellitesList.get(satellitePosition).getSatellite_id());
                    } else {
                        callDeleteSatelliteApi("archive", archivedSatellitesList.get(satellitePosition).getSatellite_id());
                    }

                } else
                    AndroidUtils.showToast(SatellitePageActivity.this, getResources().getString(R.string.no_internet));
                break;
            case 3:
                Intent previewIntent = new Intent(SatellitePageActivity.this, AddTaskActivity.class);
                previewIntent.putExtra(ConstantUtils.PURPOSE, "preview");
                previewIntent.putExtra(ConstantUtils.SOURCE, "satellite");
                previewIntent.putExtra(ConstantUtils.SATELLITE_RESPONSE_MODEL, archivedSatellitesList.get(satellitePosition));
                startActivity(previewIntent);
                overridePendingTransition(R.anim.slide_in_up, R.anim.stay);
                break;
        }
    }

    private void callDeleteSatelliteApi(final String source, String satellite_id) {
        progressDialog.show();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetMessageModel> deleteSatelliteResponseCall = apiService.deleteTask(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), satellite_id, "T");
        deleteSatelliteResponseCall.enqueue(new Callback<GetMessageModel>() {
            @Override
            public void onResponse(Call<GetMessageModel> call, Response<GetMessageModel> response) {
                progressDialog.dismiss();
                if (response.code() == 200 && response.body() != null) {
                    if (response.body().getMessage() != null)
                        AndroidUtils.showToast(SatellitePageActivity.this, response.body().getMessage());

                    if (source.equals("unLaunch")) {
                        unLaunchedSatellitesList.remove(satellitePosition);
                        unLaunchedSatellitesAdapter.notifyDataSetChanged();
                    } else if (source.equals("archive")) {
                        archivedSatellitesList.remove(satellitePosition);
                        archivedSatellitesAdapter.notifyDataSetChanged();
                    } else {
                        launchedSatellitesList.remove(satellitePosition);

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
                    AndroidUtils.showToast(SatellitePageActivity.this, getResources().getString(R.string.some_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<GetMessageModel> call, Throwable t) {
                progressDialog.dismiss();
                AndroidUtils.showToast(SatellitePageActivity.this, getResources().getString(R.string.some_went_wrong));
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
        Intent activityIntent = new Intent(SatellitePageActivity.this, AddTaskActivity.class);
        activityIntent.putExtra(ConstantUtils.PURPOSE, "update");
        activityIntent.putExtra(ConstantUtils.SOURCE, "satellite");
        activityIntent.putExtra(ConstantUtils.TASK_POSITION, position);
        activityIntent.putExtra(ConstantUtils.SATELLITE_RESPONSE_MODEL, launchedSatellitesList.get(position));
        startActivityForResult(activityIntent, EDIT_LAUNCH_SATELLITE_RESULT_CODE);
        overridePendingTransition(R.anim.slide_in_up, R.anim.stay);
    }

    @Override
    public void onDeleteTaskClick(int position, String source) {
        if (infoPopUpFragment != null)
            infoPopUpFragment.dismiss();
        this.satellitePosition = position;
        if (ConnectivityController.isNetworkAvailable(SatellitePageActivity.this))
            callDeleteSatelliteApi("launch", launchedSatellitesList.get(position).getSatellite_id());
        else
            AndroidUtils.showToast(SatellitePageActivity.this, getResources().getString(R.string.no_internet));
    }

    @Override
    public void onStorageTaskClick(int position) {
        Intent intent = new Intent(SatellitePageActivity.this, StarStorageActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDetailsClick(int position, String source) {
        Intent intent = new Intent(SatellitePageActivity.this, DetailPageActivity.class);
        intent.putExtra(ConstantUtils.ENTITY_ID, launchedSatellitesList.get(position).getSatellite_id());
        intent.putExtra(ConstantUtils.ENTITY_TYPE, "T");
        startActivity(intent);
    }

    @Override
    public void onMarkDoneClick(int position, String source) {
        if (infoPopUpFragment != null)
            infoPopUpFragment.dismiss();
        if (ConnectivityController.isNetworkAvailable(SatellitePageActivity.this))
            callMarkTaskDoneApi(position);
        else
            AndroidUtils.showToast(SatellitePageActivity.this, getResources().getString(R.string.no_internet));
    }

    private void callMarkTaskDoneApi(final int position) {
        progressDialog.show();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetMessageModel> markTaskDoneReposneCall = apiService.markTaskDone(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), launchedSatellitesList.get(position).getSatellite_id(), "T");
        markTaskDoneReposneCall.enqueue(new Callback<GetMessageModel>() {
            @Override
            public void onResponse(Call<GetMessageModel> call, Response<GetMessageModel> response) {
                progressDialog.dismiss();
                if (response.code() == 200 && response.body() != null) {
                    if (response.body().getMessage() != null)
                        AndroidUtils.showToast(SatellitePageActivity.this, response.body().getMessage());

                    launchedSatellitesList.remove(position);

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
                    AndroidUtils.showToast(SatellitePageActivity.this, getResources().getString(R.string.some_went_wrong));

            }

            @Override
            public void onFailure(Call<GetMessageModel> call, Throwable t) {
                progressDialog.dismiss();
                AndroidUtils.showToast(SatellitePageActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        intent.putExtra(ConstantUtils.MOON_RESPONSE_MODEL, moonResponseModel);
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

                    for (int i = 0; i < launchedSatellitesList.size(); i++) {
                        if (launchedSatellitesList.get(i).getSatellite_id() != null && view.getId() == Integer.parseInt(launchedSatellitesList.get(i).getSatellite_id())) {

                            try {
                                if(launchedSatellitesList.get(i).getFinish_time()!=null)
                                calendar.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(launchedSatellitesList.get(i).getFinish_date() + " " + launchedSatellitesList.get(i).getFinish_time()));
                                else  calendar.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(launchedSatellitesList.get(i).getFinish_date()));

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            SatelliteResponseModel satelliteResponseModel = launchedSatellitesList.get(i);
                            SatelliteRequestModel satelliteRequestModel = new SatelliteRequestModel();
                            satelliteRequestModel.setStar_id(satelliteResponseModel.getStar_id());
                            satelliteRequestModel.setMoon_id(satelliteResponseModel.getPlanet_id());
                            satelliteRequestModel.setSatellite_id(satelliteResponseModel.getSatellite_id());
                            satelliteRequestModel.setName(satelliteResponseModel.getName());
                            satelliteRequestModel.setDescription(satelliteResponseModel.getDescription());
                            satelliteRequestModel.setStart_date(satelliteResponseModel.getStart_date());
                            satelliteRequestModel.setStart_time(satelliteResponseModel.getStart_time());

                            satelliteRequestModel.setStart_time(satelliteResponseModel.getFinish_time());
                            satelliteRequestModel.setAlarm_date(satelliteResponseModel.getAlarm_date());
                            satelliteRequestModel.setAlarm_time(satelliteResponseModel.getAlarm_time());
                            satelliteRequestModel.setNotes(satelliteResponseModel.getNotes());
                            satelliteRequestModel.setPriority(satelliteResponseModel.getPriority());

                            int diff;
                            if(isDoStars)
                                diff=NewOrbitNo-PrevOrbitNo;
                            else diff=PrevOrbitNo-NewOrbitNo;


                            if (layoutHour.getBackground() != null) {
                                calendar.add(Calendar.HOUR, diff * 3);
                                satelliteRequestModel.setFinish_date(satelliteResponseModel.getFinish_date());
                                satelliteRequestModel.setFinish_time(new SimpleDateFormat("HH:mm").format(calendar.getTime()));

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
                                satelliteRequestModel.setFinish_date(new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()));
                                satelliteRequestModel.setFinish_time(satelliteResponseModel.getFinish_time());
                            }




                            if (ConnectivityController.isNetworkAvailable(SatellitePageActivity.this))
                                callUpdateSatelliteApi(satelliteRequestModel);
                            else
                                AndroidUtils.showToast(SatellitePageActivity.this, getResources().getString(R.string.no_internet));

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
        if (ConnectivityController.isNetworkAvailable(SatellitePageActivity.this))
            callGetLaunchedSatellitesApi(false);
        else
            AndroidUtils.showToast(SatellitePageActivity.this, getResources().getString(R.string.no_internet));
    }


    private void callUpdateSatelliteApi(SatelliteRequestModel satelliteRequestModel) {
        progressDialog.show();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<SatelliteResponseModel> satelliteResponseModelCall=apiService.updateSatellite(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN,""),satelliteRequestModel);
        satelliteResponseModelCall.enqueue(new Callback<SatelliteResponseModel>() {
            @Override
            public void onResponse(Call<SatelliteResponseModel> call, Response<SatelliteResponseModel> response) {
                progressDialog.dismiss();
                if (response.code() == 200 && response.body() != null) {
                    AndroidUtils.showToast(SatellitePageActivity.this, "Satellite has been updated successfully");
                    if (ConnectivityController.isNetworkAvailable(SatellitePageActivity.this))
                        callGetLaunchedSatellitesApi(false);
                    else
                        AndroidUtils.showToast(SatellitePageActivity.this, getResources().getString(R.string.no_internet));
                } else if (response.code() == 500) {
                    AndroidUtils.showToast(SatellitePageActivity.this, getResources().getString(R.string.some_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<SatelliteResponseModel> call, Throwable t) {
                progressDialog.dismiss();
                AndroidUtils.showToast(SatellitePageActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });

    }
}
