package com.android.yuniapp.activity;

import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
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
import com.android.yuniapp.adapter.ArchivedCometsAdapter;
import com.android.yuniapp.adapter.UnLaunchedCometsAdapter;
import com.android.yuniapp.fragment.InfoPopUpFragment;
import com.android.yuniapp.listener.InfoPopUpEventListener;
import com.android.yuniapp.listener.PopupWindowListener;
import com.android.yuniapp.model.CometRequestModel;
import com.android.yuniapp.model.CometResponseModel;
import com.android.yuniapp.model.EntitiesCoordinatesModel;
import com.android.yuniapp.model.EntitiesModel;
import com.android.yuniapp.model.GetCometsResponseModel;
import com.android.yuniapp.model.GetLaunchedStarsResponseModel;
import com.android.yuniapp.model.GetMessageModel;
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
import com.android.yuniapp.utils.SpiralView;
import com.makeramen.roundedimageview.RoundedImageView;

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

public class CometPageActivity extends AppCompatActivity implements View.OnClickListener, PopupWindowListener, QuickAction.OnActionItemClickListener, View.OnLongClickListener, InfoPopUpEventListener, View.OnDragListener {
    private RoundedImageView imgProfile;
    private ImageView imgBottomMoon, imgBack;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private RelativeLayout layoutHour, layoutDay, layoutWeek, layoutMonth, layoutYear;
    private RelativeLayout layoutCustomView;
    private ProgressBar progressBar;
    private Dialog progressDialog;
    private TextView txtDoStars, txtExceedStars;
    private View viewDoStars, viewExceedStars;
    private boolean isDoStars = true;
    private SpiralView spiralView;
    private float centerX, centerY;
    private QuickAction quickActionUnLaunch, quickActionArchive;
    private int cometPosition;
    private InfoPopUpFragment infoPopUpFragment;
    private int entityImageRadius;

    //Popup's
    private View viewDim;
    private RecyclerView recyclerViewUnLaunched, recyclerViewArchived;
    private UnLaunchedCometsAdapter unLaunchedCometsAdapter;
    private ArchivedCometsAdapter archivedCometsAdapter;
    private RelativeLayout popupUnLaunched, popupArchived;
    private ProgressBar progressBarUnLaunched, progressBarArchived;

    //ArrayLists
    private ArrayList<CometResponseModel> unLaunchedCometsList = new ArrayList<>();
    private ArrayList<StarResponseModel> launchedCometsList = new ArrayList<>();
    private ArrayList<CometResponseModel> archivedCometsList = new ArrayList<>();
    private ArrayList<StarResponseModel> entriesToShowInOrbit1 = new ArrayList<>();
    private ArrayList<StarResponseModel> entriesToShowInOrbit2 = new ArrayList<>();
    private ArrayList<StarResponseModel> entriesToShowInOrbit3 = new ArrayList<>();
    private ArrayList<StarResponseModel> entriesToShowInOrbit4 = new ArrayList<>();
    private ArrayList<EntitiesCoordinatesModel> equidistantPointsOfOrbits = new ArrayList<>();
    private ArrayList<EntitiesModel> entriesToShow = new ArrayList<>();
    private ArrayList<EntitiesCoordinatesModel> coordinatesOfOrbit1 = new ArrayList<>();
    private ArrayList<EntitiesCoordinatesModel> coordinatesOfOrbit2 = new ArrayList<>();
    private ArrayList<EntitiesCoordinatesModel> coordinatesOfOrbit3 = new ArrayList<>();
    private ArrayList<EntitiesCoordinatesModel> coordinatesOfOrbit4 = new ArrayList<>();
    //Constants
    private static final int EDIT_LAUNCH_COMET_RESULT_CODE = 60;
    private static final int EDIT_UNLAUNCH_COMET_RESULT_CODE = 61;

    private int noOfOrbits = 5, noOfSegments;

    //DoubleClick
    private final int doubleClickTimeout = ViewConfiguration.getDoubleTapTimeout();
    private Handler handler;
    private long firstClickTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comet_page);

        AppUtils.setToolbarWithBothIcon(this, "Comet Page", "(Events/To-do)", R.drawable.home_icon, 0, 0, 0);

        sharedPreferences = getSharedPreferences(ConstantUtils.APP_PREF, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

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

        layoutCustomView.setOnDragListener(this);

        unLaunchedCometsAdapter = new UnLaunchedCometsAdapter(CometPageActivity.this, CometPageActivity.this, unLaunchedCometsList);
        recyclerViewUnLaunched.setAdapter(unLaunchedCometsAdapter);

        archivedCometsAdapter = new ArchivedCometsAdapter(CometPageActivity.this, CometPageActivity.this, archivedCometsList);
        recyclerViewArchived.setAdapter(archivedCometsAdapter);

        entityImageRadius = getResources().getDrawable(R.drawable.star_storage_red_priority_1).getIntrinsicHeight();


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

                    // coordinatesOfOrbits.add(new EntitiesCoordinatesModel(x, y));

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
                        /*if (i > 80 && i < 160)
                            coordinatesOfOrbit1.add(new EntitiesCoordinatesModel(x, y));
                        else if (i > 161 && i < 240)
                            coordinatesOfOrbit2.add(new EntitiesCoordinatesModel(x, y));
                        else if (i > 241 && i < 320)
                            coordinatesOfOrbit3.add(new EntitiesCoordinatesModel(x, y));
                        else if (i > 321 && i < 400)
                            coordinatesOfOrbit4.add(new EntitiesCoordinatesModel(x, y));*/

                        arc_distance = 0;
                    }
                    last_x = x;
                    last_y = y;

                }

                if (ConnectivityController.isNetworkAvailable(CometPageActivity.this))
                    callGetLaunchedCometsApi(true);
                else
                    AndroidUtils.showToast(CometPageActivity.this, getResources().getString(R.string.no_internet));

            }
        });

    }


    private void initViews() {
        progressBar = findViewById(R.id.progress_bar);
        layoutCustomView = findViewById(R.id.layout_custom_view);
        imgProfile = findViewById(R.id.img_profile);
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
        recyclerViewArchived = findViewById(R.id.recycler_archive_stars);
        recyclerViewUnLaunched.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewArchived.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setOnClick() {
        findViewById(R.id.img_unlaunched_comet).setOnClickListener(this);
        findViewById(R.id.img_archive_comet).setOnClickListener(this);
        findViewById(R.id.img_add_comet).setOnClickListener(this);
        layoutHour.setOnClickListener(this);
        imgProfile.setOnClickListener(this);
        layoutDay.setOnClickListener(this);
        layoutWeek.setOnClickListener(this);
        layoutMonth.setOnClickListener(this);
        layoutYear.setOnClickListener(this);
        imgBack.setOnClickListener(this);
        txtDoStars.setOnClickListener(this);
        txtExceedStars.setOnClickListener(this);
        viewDim.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_lft_most_img:
                onBackPressed();
                break;
            case R.id.img_profile:
                startActivity(new Intent(CometPageActivity.this, ProfileActivity.class));
                break;
            case R.id.img_add_comet:
                Intent activityIntent = new Intent(CometPageActivity.this, AddTaskActivity.class);
                activityIntent.putExtra(ConstantUtils.SOURCE, "comet");
                startActivityForResult(activityIntent, 101);
                overridePendingTransition(R.anim.slide_in_up, R.anim.stay);
                break;
            case R.id.img_unlaunched_comet:
                popupUnLaunched.setVisibility(View.VISIBLE);
                popupArchived.setVisibility(View.GONE);
                viewDim.setVisibility(View.VISIBLE);

                if (unLaunchedCometsList.isEmpty())
                    callGetUnLaunchedCometsApi(true);
                else callGetUnLaunchedCometsApi(false);
                break;
            case R.id.img_archive_comet:
                popupUnLaunched.setVisibility(View.GONE);
                popupArchived.setVisibility(View.VISIBLE);
                viewDim.setVisibility(View.VISIBLE);

                if (archivedCometsList.isEmpty())
                    callGetArchivedCometsApi(true);
                else callGetArchivedCometsApi(false);
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

                for (int i = 0; i < launchedCometsList.size(); i++) {
                    StarResponseModel  dataResponseModel = launchedCometsList.get(i);
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
                for (int i = 0; i < launchedCometsList.size(); i++) {
                    StarResponseModel dataResponseModel = launchedCometsList.get(i);
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

                for (int i = 0; i < launchedCometsList.size(); i++) {
                    StarResponseModel dataResponseModel = launchedCometsList.get(i);
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

                for (int i = 0; i < launchedCometsList.size(); i++) {
                    StarResponseModel dataResponseModel = launchedCometsList.get(i);
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

                for (int i = 0; i < launchedCometsList.size(); i++) {
                    StarResponseModel dataResponseModel = launchedCometsList.get(i);
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
                    infoPopUpFragment = new InfoPopUpFragment();
                    Bundle bundle = new Bundle();
                    for (int i = 0; i < launchedCometsList.size(); i++) {
                        if (launchedCometsList.get(i).getId() != null && v.getId() == Integer.parseInt(launchedCometsList.get(i).getId())) {
                            bundle.putParcelable(ConstantUtils.COMET_RESPONSE_MODEL, launchedCometsList.get(i));
                            bundle.putInt(ConstantUtils.TASK_POSITION, i);
                        }
                    }
                    infoPopUpFragment.setArguments(bundle);
                    infoPopUpFragment.setInfoPopUpListener(this);
                    infoPopUpFragment.show(getSupportFragmentManager(), infoPopUpFragment.getTag());
                    infoPopUpFragment.setCancelable(false);
                    break;
        }
    }

    /*private void setDataOnSpiral() {
        TextView orbitEntryImage;
        for (int i = 0; i < entriesToShow.size(); i++) {
            if (i <= equidistantPointsOfOrbits.size() - 1) {
                orbitEntryImage = new TextView(CometPageActivity.this);
                orbitEntryImage.setLayoutParams(new android.view.ViewGroup.LayoutParams(e, entityImageHeight));
                orbitEntryImage.setPadding(20, entityImageHeight / 4, 20, 1);
                orbitEntryImage.setId(entriesToShow.get(i).getEntityId());
                orbitEntryImage.setText(entriesToShow.get(i).getEntityName());
                orbitEntryImage.setEllipsize(TextUtils.TruncateAt.END);
                orbitEntryImage.setTextSize(7f);
                orbitEntryImage.setGravity(Gravity.CENTER_HORIZONTAL);
                orbitEntryImage.setMaxLines(1);
                orbitEntryImage.setBackgroundResource(R.drawable.comets_element);
                orbitEntryImage.setX((float) equidistantPointsOfOrbits.get(i).getX() - entityImageWidth / 2);
                orbitEntryImage.setY((float) equidistantPointsOfOrbits.get(i).getY() - entityImageHeight / 2);
                orbitEntryImage.setOnClickListener(this);
                orbitEntryImage.setOnLongClickListener(this);
                layoutCustomView.addView(orbitEntryImage);
            } else if (entriesToShow.size() > equidistantPointsOfOrbits.size()) {
                orbitEntryImage = new TextView(CometPageActivity.this);
                orbitEntryImage.setLayoutParams(new android.view.ViewGroup.LayoutParams(entityImageWidth, entityImageHeight));
                orbitEntryImage.setPadding(20, entityImageHeight / 4, 20, 1);
                orbitEntryImage.setText(entriesToShow.size() - equidistantPointsOfOrbits.size() + "+");
                orbitEntryImage.setEllipsize(TextUtils.TruncateAt.END);
                orbitEntryImage.setTextSize(7f);
                orbitEntryImage.setGravity(Gravity.CENTER_HORIZONTAL);
                orbitEntryImage.setMaxLines(1);
                orbitEntryImage.setBackgroundResource(R.drawable.comets_overlay);
                orbitEntryImage.setX((float) equidistantPointsOfOrbits.get(i).getX() - entityImageWidth / 2);
                orbitEntryImage.setY((float) equidistantPointsOfOrbits.get(i).getY() - entityImageHeight / 2);
                orbitEntryImage.setOnClickListener(this);
                orbitEntryImage.setOnLongClickListener(this);
                layoutCustomView.addView(orbitEntryImage);
                break;
            }
        }
    }
*/

    private void callGetLaunchedCometsApi(boolean needLoader) {
        if (needLoader)
            progressBar.setVisibility(View.VISIBLE);
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetLaunchedStarsResponseModel> getLaunchedCometsResponseCall = apiService.getLaunchedComets(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""));
        getLaunchedCometsResponseCall.enqueue(new Callback<GetLaunchedStarsResponseModel>() {
            @Override
            public void onResponse(Call<GetLaunchedStarsResponseModel> call, Response<GetLaunchedStarsResponseModel> response) {
                progressBar.setVisibility(View.GONE);
                if (response.code() == 200 && response.body() != null) {
                    if (response.body().getData() != null && !response.body().getData().isEmpty()) {
                        launchedCometsList.clear();
                        launchedCometsList.addAll(response.body().getData());

                        if (layoutHour.getBackground() != null)
                            layoutHour.performClick();
                        else if (layoutDay.getBackground() != null)
                            layoutDay.performClick();
                        else if (layoutWeek.getBackground() != null)
                            layoutWeek.performClick();
                        else if (layoutMonth.getBackground() != null)
                            layoutMonth.performClick();
                        else layoutYear.performClick();


                    } else if (response.code() == 500) {
                        AndroidUtils.showToast(CometPageActivity.this, getResources().getString(R.string.some_went_wrong));
                    }
                }
            }

            @Override
            public void onFailure(Call<GetLaunchedStarsResponseModel> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                AndroidUtils.showToast(CometPageActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });

    }

    private void callGetUnLaunchedCometsApi(boolean needLoader) {
        if (needLoader)
            progressBarUnLaunched.setVisibility(View.VISIBLE);
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetCometsResponseModel> getCometsResponseModelCall = apiService.getUnLaunchedComets(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""));
        getCometsResponseModelCall.enqueue(new Callback<GetCometsResponseModel>() {
            @Override
            public void onResponse(Call<GetCometsResponseModel> call, Response<GetCometsResponseModel> response) {
                progressBarUnLaunched.setVisibility(View.GONE);
                if (response.code() == 200 && response.body() != null) {
                    if (response.body().getComets() != null && !response.body().getComets().isEmpty()) {
                        unLaunchedCometsList.clear();
                        unLaunchedCometsList.addAll(response.body().getComets());
                        unLaunchedCometsAdapter.notifyDataSetChanged();
                    } else {
                        popupUnLaunched.setVisibility(View.GONE);
                        viewDim.setVisibility(View.GONE);
                        AndroidUtils.showToast(CometPageActivity.this, "No new comets waiting for dates.");
                    }


                }
            }

            @Override
            public void onFailure(Call<GetCometsResponseModel> call, Throwable t) {
                progressBarUnLaunched.setVisibility(View.GONE);
                AndroidUtils.showToast(CometPageActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });
    }

    private void callGetArchivedCometsApi(boolean needLoader) {
        if (needLoader)
            progressBarArchived.setVisibility(View.VISIBLE);
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetCometsResponseModel> getCometsResponseModelCall = apiService.getArchivedComets(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""));
        getCometsResponseModelCall.enqueue(new Callback<GetCometsResponseModel>() {
            @Override
            public void onResponse(Call<GetCometsResponseModel> call, Response<GetCometsResponseModel> response) {
                progressBarArchived.setVisibility(View.GONE);
                if (response.code() == 200 && response.body() != null) {
                    if (response.body().getComets() != null && !response.body().getComets().isEmpty()) {
                        archivedCometsList.clear();
                        archivedCometsList.addAll(response.body().getComets());
                        archivedCometsAdapter.notifyDataSetChanged();
                    } else {
                        popupArchived.setVisibility(View.GONE);
                        viewDim.setVisibility(View.GONE);
                        AndroidUtils.showToast(CometPageActivity.this, "No old comets.");
                    }
                }
            }

            @Override
            public void onFailure(Call<GetCometsResponseModel> call, Throwable t) {
                progressBarArchived.setVisibility(View.GONE);
                AndroidUtils.showToast(CometPageActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });
    }

    private void setDataForOrbit1() {

        TextView orbitEntryImage;
        int noOfEntities = coordinatesOfOrbit1.size();
        int entitiesToShow = entriesToShowInOrbit1.size();
        int v;

        if (entitiesToShow > noOfEntities)
            v = 1;
        else
            v = 0;
        for (int i = 0; i < entriesToShowInOrbit1.size(); i++) {
            if (i < noOfEntities - 1 && v < noOfEntities) {
                v++;
                orbitEntryImage = new TextView(CometPageActivity.this);
                orbitEntryImage.setLayoutParams(new android.view.ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                orbitEntryImage.setTypeface(ResourcesCompat.getFont(CometPageActivity.this, R.font.teko_medium));
                orbitEntryImage.setTextColor(ContextCompat.getColor(CometPageActivity.this,R.color.colorBlack));
                orbitEntryImage.setPadding(20, entityImageRadius / 4, 20, 1);
                orbitEntryImage.setId(Integer.parseInt(entriesToShowInOrbit1.get(i).getId()));
                orbitEntryImage.setText(entriesToShowInOrbit1.get(i).getName());
                //orbitEntryImage.setEllipsize(TextUtils.TruncateAt.END);
                orbitEntryImage.setTextSize(9f);
                orbitEntryImage.setGravity(Gravity.CENTER_HORIZONTAL);
                orbitEntryImage.setMaxLines(1);
                orbitEntryImage.setTag(1);
                setEntityImage(orbitEntryImage, 1, Integer.valueOf(entriesToShowInOrbit1.get(i).getPriority()), entriesToShowInOrbit1.get(i).getStart_date(), entriesToShowInOrbit1.get(i).getFinish_date());
                orbitEntryImage.setX((float) (coordinatesOfOrbit1.get(i).getX() - entityImageRadius / 2));
                orbitEntryImage.setY((float) (coordinatesOfOrbit1.get(i).getY() - entityImageRadius / 2));
                orbitEntryImage.setOnClickListener(this);
                orbitEntryImage.setOnLongClickListener(this);
                layoutCustomView.addView(orbitEntryImage);

            } else if (entriesToShowInOrbit1.size() > noOfEntities) {
                orbitEntryImage = new TextView(CometPageActivity.this);
                orbitEntryImage.setLayoutParams(new android.view.ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                orbitEntryImage.setTypeface(ResourcesCompat.getFont(CometPageActivity.this, R.font.teko_medium));
                orbitEntryImage.setTextColor(ContextCompat.getColor(CometPageActivity.this,R.color.colorBlack));
                orbitEntryImage.setPadding(10, entityImageRadius / 4, 10, 1);
                orbitEntryImage.setText(entriesToShowInOrbit1.size() - noOfEntities + "+");
                orbitEntryImage.setTextSize(8f);
                orbitEntryImage.setGravity(Gravity.CENTER_HORIZONTAL);
                orbitEntryImage.setMaxLines(1);
                orbitEntryImage.setBackgroundResource(R.drawable.galaxy_tail);
                orbitEntryImage.setX((float) (coordinatesOfOrbit1.get(i).getX() - entityImageRadius / 2));
                orbitEntryImage.setY((float) (coordinatesOfOrbit1.get(i).getY() - entityImageRadius / 2));
                orbitEntryImage.setId(Integer.parseInt(entriesToShowInOrbit1.get(i).getId()));
                layoutCustomView.addView(orbitEntryImage);
                break;

            }
        }
    }

    private void setDataForOrbit2() {

        TextView orbitEntryImage;
        int noOfEntities = coordinatesOfOrbit2.size();
        int entitiesToShow = entriesToShowInOrbit2.size();
        int v;

        if (entitiesToShow > noOfEntities)
            v = 1;
        else
            v = 0;
        for (int i = 0; i < entriesToShowInOrbit2.size(); i++) {
            if (i < noOfEntities - 1 && v < noOfEntities) {
                v++;
                orbitEntryImage = new TextView(CometPageActivity.this);
                orbitEntryImage.setLayoutParams(new android.view.ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                orbitEntryImage.setTypeface(ResourcesCompat.getFont(CometPageActivity.this, R.font.teko_medium));
                orbitEntryImage.setTextColor(ContextCompat.getColor(CometPageActivity.this,R.color.colorBlack));
                orbitEntryImage.setPadding(20, entityImageRadius / 4, 20, 1);
                orbitEntryImage.setId(Integer.parseInt(entriesToShowInOrbit2.get(i).getId()));
                orbitEntryImage.setText(entriesToShowInOrbit2.get(i).getName());
                //orbitEntryImage.setEllipsize(TextUtils.TruncateAt.END);
                orbitEntryImage.setTextSize(9f);
                orbitEntryImage.setGravity(Gravity.CENTER_HORIZONTAL);
                orbitEntryImage.setMaxLines(1);
                orbitEntryImage.setTag(2);
                setEntityImage(orbitEntryImage, 2, Integer.valueOf(entriesToShowInOrbit2.get(i).getPriority()), entriesToShowInOrbit2.get(i).getStart_date(), entriesToShowInOrbit2.get(i).getFinish_date());
                orbitEntryImage.setX((float) (coordinatesOfOrbit2.get(i).getX() - entityImageRadius / 2));
                orbitEntryImage.setY((float) (coordinatesOfOrbit2.get(i).getY() - entityImageRadius / 2));
                orbitEntryImage.setOnClickListener(this);
                orbitEntryImage.setOnLongClickListener(this);
                layoutCustomView.addView(orbitEntryImage);

            } else if (entriesToShowInOrbit2.size() > noOfEntities) {
                orbitEntryImage = new TextView(CometPageActivity.this);
                orbitEntryImage.setLayoutParams(new android.view.ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                orbitEntryImage.setTypeface(ResourcesCompat.getFont(CometPageActivity.this, R.font.teko_medium));
                orbitEntryImage.setTextColor(ContextCompat.getColor(CometPageActivity.this,R.color.colorBlack));
                orbitEntryImage.setPadding(10, entityImageRadius / 4, 10, 1);
                orbitEntryImage.setText(entriesToShowInOrbit2.size() - noOfEntities + "+");
                orbitEntryImage.setTextSize(8f);
                orbitEntryImage.setGravity(Gravity.CENTER_HORIZONTAL);
                orbitEntryImage.setMaxLines(1);
                orbitEntryImage.setBackgroundResource(R.drawable.galaxy_tail);
                orbitEntryImage.setX((float) (coordinatesOfOrbit1.get(i).getX() - entityImageRadius / 2));
                orbitEntryImage.setY((float) (coordinatesOfOrbit1.get(i).getY() - entityImageRadius / 2));
                orbitEntryImage.setId(Integer.parseInt(entriesToShowInOrbit2.get(i).getId()));
                layoutCustomView.addView(orbitEntryImage);
                break;

            }
        }
    }

    private void setDataForOrbit3() {

        TextView orbitEntryImage;
        int noOfEntities = coordinatesOfOrbit3.size();
        int entitiesToShow = entriesToShowInOrbit3.size();
        int v;

        if (entitiesToShow > noOfEntities)
            v = 1;
        else
            v = 0;
        for (int i = 0; i < entriesToShowInOrbit3.size(); i++) {
            if (i < noOfEntities - 1 && v < noOfEntities) {
                v++;
                orbitEntryImage = new TextView(CometPageActivity.this);
                orbitEntryImage.setLayoutParams(new android.view.ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                orbitEntryImage.setTypeface(ResourcesCompat.getFont(CometPageActivity.this, R.font.teko_medium));
                orbitEntryImage.setTextColor(ContextCompat.getColor(CometPageActivity.this,R.color.colorBlack));
                orbitEntryImage.setPadding(20, entityImageRadius / 4, 20, 1);
                orbitEntryImage.setId(Integer.parseInt(entriesToShowInOrbit3.get(i).getId()));
                orbitEntryImage.setText(entriesToShowInOrbit3.get(i).getName());
               // orbitEntryImage.setEllipsize(TextUtils.TruncateAt.END);
                orbitEntryImage.setTextSize(9f);
                orbitEntryImage.setGravity(Gravity.CENTER_HORIZONTAL);
                orbitEntryImage.setMaxLines(1);
                orbitEntryImage.setTag(3);
                setEntityImage(orbitEntryImage, 3, Integer.valueOf(entriesToShowInOrbit3.get(i).getPriority()), entriesToShowInOrbit3.get(i).getStart_date(), entriesToShowInOrbit3.get(i).getFinish_date());
                orbitEntryImage.setX((float) (coordinatesOfOrbit3.get(i).getX() - entityImageRadius / 2));
                orbitEntryImage.setY((float) (coordinatesOfOrbit3.get(i).getY() - entityImageRadius / 2));
                orbitEntryImage.setOnClickListener(this);
                orbitEntryImage.setOnLongClickListener(this);
                layoutCustomView.addView(orbitEntryImage);

            } else if (entriesToShowInOrbit3.size() > noOfEntities) {
                orbitEntryImage = new TextView(CometPageActivity.this);
                orbitEntryImage.setLayoutParams(new android.view.ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                orbitEntryImage.setTypeface(ResourcesCompat.getFont(CometPageActivity.this, R.font.teko_medium));
                orbitEntryImage.setTextColor(ContextCompat.getColor(CometPageActivity.this,R.color.colorBlack));
                orbitEntryImage.setPadding(10, entityImageRadius / 4, 10, 1);
                orbitEntryImage.setText(entriesToShowInOrbit3.size() - noOfEntities + "+");
                orbitEntryImage.setTextSize(8f);
                orbitEntryImage.setGravity(Gravity.CENTER_HORIZONTAL);
                orbitEntryImage.setMaxLines(1);
                orbitEntryImage.setBackgroundResource(R.drawable.galaxy_tail);
                orbitEntryImage.setX((float) (coordinatesOfOrbit3.get(i).getX() - entityImageRadius / 2));
                orbitEntryImage.setY((float) (coordinatesOfOrbit3.get(i).getY() - entityImageRadius / 2));
                orbitEntryImage.setId(Integer.parseInt(entriesToShowInOrbit3.get(i).getId()));
                layoutCustomView.addView(orbitEntryImage);
                break;

            }
        }
    }

    private void setDataForOrbit4() {

        TextView orbitEntryImage;
        int noOfEntities = coordinatesOfOrbit4.size();
        int entitiesToShow = entriesToShowInOrbit4.size();
        int v;

        if (entitiesToShow > noOfEntities)
            v = 1;
        else
            v = 0;
        for (int i = 0; i < entriesToShowInOrbit4.size(); i++) {
            if (i < noOfEntities - 1 && v < noOfEntities) {
                v++;
                orbitEntryImage = new TextView(CometPageActivity.this);
                orbitEntryImage.setLayoutParams(new android.view.ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                orbitEntryImage.setTypeface(ResourcesCompat.getFont(CometPageActivity.this, R.font.teko_medium));
                orbitEntryImage.setTextColor(ContextCompat.getColor(CometPageActivity.this,R.color.colorBlack));
                orbitEntryImage.setPadding(20, entityImageRadius / 4, 20, 1);
                orbitEntryImage.setId(Integer.parseInt(entriesToShowInOrbit4.get(i).getId()));
                orbitEntryImage.setText(entriesToShowInOrbit4.get(i).getName());
                //orbitEntryImage.setEllipsize(TextUtils.TruncateAt.END);
                orbitEntryImage.setTextSize(9f);
                orbitEntryImage.setGravity(Gravity.CENTER_HORIZONTAL);
                orbitEntryImage.setMaxLines(1);
                orbitEntryImage.setTag(4);
                setEntityImage(orbitEntryImage, 4, Integer.valueOf(entriesToShowInOrbit4.get(i).getPriority()), entriesToShowInOrbit4.get(i).getStart_date(), entriesToShowInOrbit4.get(i).getFinish_date());
                orbitEntryImage.setX((float) (coordinatesOfOrbit4.get(i).getX() - entityImageRadius / 2));
                orbitEntryImage.setY((float) (coordinatesOfOrbit4.get(i).getY() - entityImageRadius / 2));
                orbitEntryImage.setOnClickListener(this);
                orbitEntryImage.setOnLongClickListener(this);
                layoutCustomView.addView(orbitEntryImage);

            } else if (entriesToShowInOrbit4.size() > noOfEntities) {
                orbitEntryImage = new TextView(CometPageActivity.this);
                orbitEntryImage.setLayoutParams(new android.view.ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                orbitEntryImage.setTypeface(ResourcesCompat.getFont(CometPageActivity.this, R.font.teko_medium));
                orbitEntryImage.setTextColor(ContextCompat.getColor(CometPageActivity.this,R.color.colorBlack));
                orbitEntryImage.setPadding(10, entityImageRadius / 4, 10, 1);
                orbitEntryImage.setText(entriesToShowInOrbit4.size() - noOfEntities + "+");
                orbitEntryImage.setTextSize(8f);
                orbitEntryImage.setGravity(Gravity.CENTER_HORIZONTAL);
                orbitEntryImage.setMaxLines(1);
                orbitEntryImage.setBackgroundResource(R.drawable.galaxy_tail);
                orbitEntryImage.setX((float) (coordinatesOfOrbit4.get(i).getX() - entityImageRadius / 2));
                orbitEntryImage.setY((float) (coordinatesOfOrbit4.get(i).getY() - entityImageRadius / 2));
                orbitEntryImage.setId(Integer.parseInt(entriesToShowInOrbit4.get(i).getId()));
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
                        orbitEntryImage.setBackgroundResource(R.drawable.comet_red_priority_1);
                    else
                        orbitEntryImage.setBackgroundResource(R.drawable.comet_green_priority_1);
                }
                else
                    orbitEntryImage.setBackgroundResource(R.drawable.comet_green_priority_1);

                break;
            case 2:
                if (isDoStars )
                {
                    if (dateStart.after(currentDate))
                        orbitEntryImage.setBackgroundResource(R.drawable.comet_red_priority_2);
                    else
                        orbitEntryImage.setBackgroundResource(R.drawable.comet_green_priority_2);
                }
                else
                    orbitEntryImage.setBackgroundResource(R.drawable.comet_green_priority_2);
                break;
            case 3:
                if (isDoStars)
                {
                    if (dateStart.after(currentDate))
                        orbitEntryImage.setBackgroundResource(R.drawable.comet_red_priority_3);
                    else
                        orbitEntryImage.setBackgroundResource(R.drawable.comet_green_priority_3);
                }
                else
                    orbitEntryImage.setBackgroundResource(R.drawable.comet_green_priority_3);
                break;
            case 4:
                if (isDoStars)
                {
                    if (dateStart.after(currentDate))
                        orbitEntryImage.setBackgroundResource(R.drawable.comet_red_priority_4);
                    else
                        orbitEntryImage.setBackgroundResource(R.drawable.comet_green_priority_4);
                }
                else
                    orbitEntryImage.setBackgroundResource(R.drawable.comet_green_priority_4);
                break;
            case 5:
                if (isDoStars)
                {
                    if (dateStart.after(currentDate))
                        orbitEntryImage.setBackgroundResource(R.drawable.comet_red_priority_5);
                    else
                        orbitEntryImage.setBackgroundResource(R.drawable.comet_green_priority_5);
                }
                else
                    orbitEntryImage.setBackgroundResource(R.drawable.comet_green_priority_5);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK) {
            if (ConnectivityController.isNetworkAvailable(CometPageActivity.this))
                callGetLaunchedCometsApi(false);
            else
                AndroidUtils.showToast(CometPageActivity.this, getResources().getString(R.string.no_internet));
        } else if (requestCode == EDIT_LAUNCH_COMET_RESULT_CODE && resultCode == RESULT_OK) {
            if (infoPopUpFragment != null)
                infoPopUpFragment.dismiss();
            if (data != null && data.getParcelableExtra(ConstantUtils.COMET_RESPONSE_MODEL) != null) {

                launchedCometsList.remove(data.getIntExtra(ConstantUtils.TASK_POSITION, 0));
                launchedCometsList.add(data.getIntExtra(ConstantUtils.TASK_POSITION, 0), (StarResponseModel) data.getParcelableExtra(ConstantUtils.COMET_RESPONSE_MODEL));

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
                if (ConnectivityController.isNetworkAvailable(CometPageActivity.this))
                    callGetLaunchedCometsApi(false);
                else
                    AndroidUtils.showToast(CometPageActivity.this, getResources().getString(R.string.no_internet));
            }
        } /*else if (requestCode == EDIT_UNLAUNCH_COMET_RESULT_CODE && resultCode == RESULT_OK) {
            if (infoPopUpFragment != null)
                infoPopUpFragment.dismiss();
            CometResponseModel cometResponseModel;
            if (data != null && data.getParcelableExtra(ConstantUtils.COMET_RESPONSE_MODEL) != null) {
                cometResponseModel = data.getParcelableExtra(ConstantUtils.COMET_RESPONSE_MODEL);

                if (cometResponseModel != null && cometResponseModel.getStart_date() != null && cometResponseModel.getFinish_date() != null) {
                    unLaunchedCometsList.remove(data.getIntExtra(ConstantUtils.TASK_POSITION, 0));
                    unLaunchedCometsAdapter.notifyDataSetChanged();
                    launchedCometsList.add(cometResponseModel);

                    if (layoutHour.getBackground() != null)
                        layoutHour.performClick();
                    else if (layoutDay.getBackground() != null)
                        layoutDay.performClick();
                    else if (layoutWeek.getBackground() != null)
                        layoutWeek.performClick();
                    else if (layoutMonth.getBackground() != null)
                        layoutMonth.performClick();
                    else layoutYear.performClick();
                } else if (cometResponseModel != null) {
                    unLaunchedCometsList.remove(data.getIntExtra(ConstantUtils.TASK_POSITION, 0));
                    unLaunchedCometsList.add(data.getIntExtra(ConstantUtils.TASK_POSITION, 0), cometResponseModel);
                    unLaunchedCometsAdapter.notifyDataSetChanged();
                }

            } else {
                if (ConnectivityController.isNetworkAvailable(CometPageActivity.this)) {
                    callGetUnLaunchedCometsApi(false);
                    callGetLaunchedCometsApi(false);

                } else
                    AndroidUtils.showToast(CometPageActivity.this, getResources().getString(R.string.no_internet));
            }
        }*/
    }

    @Override
    public void onTaskClick(View view, int position, String source) {
        if (source.equals("archive"))
            quickActionArchive.show(view);
        else
            quickActionUnLaunch.show(view);
        this.cometPosition = position;
    }

    @Override
    public void onItemClick(ActionItem item) {
        switch (item.getActionId()) {
            case 1:

                Intent activityIntent = new Intent(CometPageActivity.this, AddTaskActivity.class);
                activityIntent.putExtra(ConstantUtils.PURPOSE, "update");
                activityIntent.putExtra(ConstantUtils.SOURCE, "comet");
                activityIntent.putExtra(ConstantUtils.TASK_POSITION, cometPosition);
                activityIntent.putExtra(ConstantUtils.COMET_RESPONSE_MODEL, unLaunchedCometsList.get(cometPosition));
                startActivityForResult(activityIntent, EDIT_UNLAUNCH_COMET_RESULT_CODE);
                overridePendingTransition(R.anim.slide_in_up, R.anim.stay);

                break;
            case 2:
                if (ConnectivityController.isNetworkAvailable(CometPageActivity.this)) {
                    if (popupUnLaunched.getVisibility() == View.VISIBLE) {

                        callDeleteCometApi("unLaunch", unLaunchedCometsList.get(cometPosition).getComet_id());
                    } else {
                        callDeleteCometApi("archive", archivedCometsList.get(cometPosition).getComet_id());
                    }

                } else
                    AndroidUtils.showToast(CometPageActivity.this, getResources().getString(R.string.no_internet));
                break;
            case 3:
                Intent previewIntent = new Intent(CometPageActivity.this, AddTaskActivity.class);
                previewIntent.putExtra(ConstantUtils.PURPOSE, "preview");
                previewIntent.putExtra(ConstantUtils.SOURCE, "comet");
                previewIntent.putExtra(ConstantUtils.COMET_RESPONSE_MODEL, archivedCometsList.get(cometPosition));
                startActivity(previewIntent);
                overridePendingTransition(R.anim.slide_in_up, R.anim.stay);
                break;
        }
    }

    private void callDeleteCometApi(final String source, final String comet_id) {
        progressDialog.show();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetMessageModel> deleteCometResponseCall = apiService.deleteTask(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), comet_id, "C");
        deleteCometResponseCall.enqueue(new Callback<GetMessageModel>() {
            @Override
            public void onResponse(Call<GetMessageModel> call, Response<GetMessageModel> response) {
                progressDialog.dismiss();
                if (response.code() == 200 && response.body() != null) {
                    if (response.body().getMessage() != null)
                        AndroidUtils.showToast(CometPageActivity.this, response.body().getMessage());

                    if (source.equals("unLaunch")) {
                        unLaunchedCometsList.remove(cometPosition);
                        unLaunchedCometsAdapter.notifyDataSetChanged();
                    } else if (source.equals("archive")) {
                        archivedCometsList.remove(cometPosition);
                        archivedCometsAdapter.notifyDataSetChanged();
                    } else {
                        launchedCometsList.remove(cometPosition);

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
                    AndroidUtils.showToast(CometPageActivity.this, getResources().getString(R.string.some_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<GetMessageModel> call, Throwable t) {
                progressDialog.dismiss();
                AndroidUtils.showToast(CometPageActivity.this, getResources().getString(R.string.some_went_wrong));
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
        Intent activityIntent = new Intent(CometPageActivity.this, AddTaskActivity.class);
        activityIntent.putExtra(ConstantUtils.PURPOSE, "update");
        activityIntent.putExtra(ConstantUtils.SOURCE, "comet");
        activityIntent.putExtra(ConstantUtils.TASK_POSITION, position);
        activityIntent.putExtra(ConstantUtils.COMET_RESPONSE_MODEL, launchedCometsList.get(position));
        startActivityForResult(activityIntent, EDIT_LAUNCH_COMET_RESULT_CODE);
        overridePendingTransition(R.anim.slide_in_up, R.anim.stay);
    }

    @Override
    public void onDeleteTaskClick(int position, String source) {
        if (infoPopUpFragment != null)
            infoPopUpFragment.dismiss();
        this.cometPosition = position;
        if (ConnectivityController.isNetworkAvailable(CometPageActivity.this))
            callDeleteCometApi("launch", launchedCometsList.get(position).getId());
        else
            AndroidUtils.showToast(CometPageActivity.this, getResources().getString(R.string.no_internet));
    }

    @Override
    public void onStorageTaskClick(int position) {
        Intent intent = new Intent(CometPageActivity.this, StarStorageActivity.class);
        startActivity(intent);
    }

    @Override
    public void onMarkDoneClick(int position, String source) {
        if (infoPopUpFragment != null)
            infoPopUpFragment.dismiss();
        if (ConnectivityController.isNetworkAvailable(CometPageActivity.this))
            callMarkTaskDoneApi(position);
        else
            AndroidUtils.showToast(CometPageActivity.this, getResources().getString(R.string.no_internet));
    }

    @Override
    public void onDetailsClick(int position, String source) {
        Intent intent = new Intent(CometPageActivity.this, DetailPageActivity.class);
        intent.putExtra(ConstantUtils.ENTITY_ID, launchedCometsList.get(position).getType());
        intent.putExtra(ConstantUtils.ENTITY_TYPE, launchedCometsList.get(position).getId());
        startActivity(intent);
    }

    private void callMarkTaskDoneApi(final int position) {
        progressDialog.show();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetMessageModel> markTaskDoneReposneCall = apiService.markTaskDone(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), launchedCometsList.get(position).getId(), "C");
        markTaskDoneReposneCall.enqueue(new Callback<GetMessageModel>() {
            @Override
            public void onResponse(Call<GetMessageModel> call, Response<GetMessageModel> response) {
                progressDialog.dismiss();
                if (response.code() == 200 && response.body() != null) {
                    if (response.body().getMessage() != null)
                        AndroidUtils.showToast(CometPageActivity.this, response.body().getMessage());

                    launchedCometsList.remove(position);

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
                    AndroidUtils.showToast(CometPageActivity.this, getResources().getString(R.string.some_went_wrong));

            }

            @Override
            public void onFailure(Call<GetMessageModel> call, Throwable t) {
                progressDialog.dismiss();
                AndroidUtils.showToast(CometPageActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
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
                Log.e("DragStared", "onDrag: " + event.getX());
                Log.e("DragStared", "onDrag: " + event.getY());
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
/*
                    for (int i = 0; i < launchedCometsList.size(); i++) {
                        if (launchedCometsList.get(i).getId() != null && view.getId() == Integer.parseInt(launchedCometsList.get(i).getId())) {
                            try {
                                if(launchedCometsList.get(i).getFinish_time()!=null)
                                calendar.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(launchedCometsList.get(i).getFinish_date() + " " + launchedCometsList.get(i).getFinish_time()));
                            else    calendar.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(launchedCometsList.get(i).getFinish_date()));

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            CometResponseModel cometResponseModel = launchedCometsList.get(i);
                            CometRequestModel cometRequestModel=new CometRequestModel();
                            cometRequestModel.setComet_id(cometResponseModel.getComet_id());
                            cometRequestModel.setName(cometResponseModel.getName());
                            cometRequestModel.setDescription(cometResponseModel.getDescription());
                            cometRequestModel.setStart_date(cometResponseModel.getStart_date());
                            cometRequestModel.setStart_time(cometResponseModel.getStart_time());
                            cometRequestModel.setAlarm_date(cometResponseModel.getAlarm_date());
                            cometRequestModel.setAlarm_time(cometResponseModel.getAlarm_time());
                            cometRequestModel.setNotes(cometResponseModel.getNotes());
                            cometRequestModel.setPriority(cometResponseModel.getPriority());

                            int diff;
                            if(isDoStars)
                             diff=NewOrbitNo-PrevOrbitNo;
                             else diff=PrevOrbitNo-NewOrbitNo;

                            if (layoutHour.getBackground() != null) {
                                calendar.add(Calendar.HOUR, diff * 3);
                                cometRequestModel.setFinish_date(cometResponseModel.getFinish_date());
                                cometRequestModel.setFinish_time(new SimpleDateFormat("HH:mm").format(calendar.getTime()));

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
                                cometRequestModel.setFinish_date(new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()));
                                cometRequestModel.setFinish_time(cometResponseModel.getFinish_time());
                            }




                            if (ConnectivityController.isNetworkAvailable(CometPageActivity.this))
                                callUpdateCometApi(cometRequestModel);
                            else
                                AndroidUtils.showToast(CometPageActivity.this, getResources().getString(R.string.no_internet));

                        }
                    }*/

                }

            case DragEvent.ACTION_DRAG_ENDED:
                  /*   View view1 = (View) event.getLocalState();
                     view1.setVisibility(View.VISIBLE);*/
                return true;

            default:
                break;
        }
        return false;
    }

    private void callUpdateCometApi(CometRequestModel cometRequestModel) {
        progressDialog.show();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<CometResponseModel> cometResponseModelCall=apiService.updateComet(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN,""),cometRequestModel);
        cometResponseModelCall.enqueue(new Callback<CometResponseModel>() {
            @Override
            public void onResponse(Call<CometResponseModel> call, Response<CometResponseModel> response) {
                progressDialog.dismiss();
                if (response.code() == 200 && response.body() != null) {
                    AndroidUtils.showToast(CometPageActivity.this, "Comet has been updated successfully");
                    if (ConnectivityController.isNetworkAvailable(CometPageActivity.this))
                        callGetLaunchedCometsApi(false);
                    else
                        AndroidUtils.showToast(CometPageActivity.this, getResources().getString(R.string.no_internet));
                } else if (response.code() == 500) {
                    AndroidUtils.showToast(CometPageActivity.this, getResources().getString(R.string.some_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<CometResponseModel> call, Throwable t) {
                progressDialog.dismiss();
                AndroidUtils.showToast(CometPageActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });
    }
}
