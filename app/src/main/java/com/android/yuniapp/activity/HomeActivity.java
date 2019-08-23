package com.android.yuniapp.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.android.yuniapp.R;
import com.android.yuniapp.adapter.ArchivedStarsAdapter;
import com.android.yuniapp.adapter.GalaxyStarsAdapter;
import com.android.yuniapp.adapter.GridCellAdapter;
import com.android.yuniapp.adapter.UnLaunchedStarsAdapter;
import com.android.yuniapp.application.BaseActivity;
import com.android.yuniapp.fragment.DropEntityPopUpFragment;
import com.android.yuniapp.fragment.InfoPopUpFragment;
import com.android.yuniapp.listener.DropEntityPopUpListener;
import com.android.yuniapp.listener.GalaxyListener;
import com.android.yuniapp.listener.InfoPopUpEventListener;
import com.android.yuniapp.listener.PopupWindowListener;
import com.android.yuniapp.model.CometRequestModel;
import com.android.yuniapp.model.CometResponseModel;
import com.android.yuniapp.model.DeleteTaskReqModel;
import com.android.yuniapp.model.EachDate;
import com.android.yuniapp.model.EntitiesCoordinatesModel;
import com.android.yuniapp.model.EntitiesModel;
import com.android.yuniapp.model.GetLaunchedStarsResponseModel;
import com.android.yuniapp.model.GetMessageModel;
import com.android.yuniapp.model.GetUnLaunchedStarsResponseModel;
import com.android.yuniapp.model.StarRequestModel;
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
import com.google.gson.JsonElement;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HomeActivity extends BaseActivity implements View.OnClickListener, PopupWindowListener, View.OnLongClickListener, QuickAction.OnActionItemClickListener, InfoPopUpEventListener, View.OnDragListener, DropEntityPopUpListener, GalaxyListener, View.OnTouchListener {
    private RoundedImageView imgProfile;
    private ImageView imgBottomMoon, imgAdd, imgNew, imgArchive, imgBack;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private RelativeLayout layoutHour, layoutDay, layoutWeek, layoutMonth, layoutYear;
    private boolean doubleBackToExitPressedOnce = false;
    private RelativeLayout layoutCustomView;
    private TextView txtDoStars, txtExceedStars, txtQuote;
    private View viewDoStars, viewExceedStars;
    private boolean isDoStars = true;
    private ProgressBar progressBar;
    private Dialog progressDialog;
    private SpiralView spiralView;
    private InfoPopUpFragment infoPopUpFragment;
    private int entityRadius;

    //Popup's
    private View viewDim;
    private RecyclerView recyclerViewUnLaunched, recyclerViewArchived, recyclerViewGalaxy;
    private UnLaunchedStarsAdapter unLaunchedStarsAdapter;
    private ArchivedStarsAdapter archivedStarsAdapter;
    private RelativeLayout popupUnLaunched, popupArchived, popupGalaxy;
    private ProgressBar progressBarUnLaunched, progressBarArchived;
    private GalaxyStarsAdapter galaxyStarsAdapter;


    //Action Popup
    private QuickAction quickActionUnLaunch, quickActionArchive;
    private int starPosition;
    private float centerX, centerY;

    //ArrayLists
    private ArrayList<StarResponseModel> unLaunchedStarsList = new ArrayList<>();
    private ArrayList<StarResponseModel> launchedStarsList = new ArrayList<>();
    private ArrayList<StarResponseModel> archivedStarsList = new ArrayList<>();
    private ArrayList<StarResponseModel> entriesToShowInOrbit1 = new ArrayList<>();
    private ArrayList<StarResponseModel> entriesToShowInOrbit2 = new ArrayList<>();
    private ArrayList<StarResponseModel> entriesToShowInOrbit3 = new ArrayList<>();
    private ArrayList<StarResponseModel> entriesToShowInOrbit4 = new ArrayList<>();
    private ArrayList<EntitiesCoordinatesModel> coordinatesOfOrbit1 = new ArrayList<>();
    private ArrayList<EntitiesCoordinatesModel> coordinatesOfOrbit2 = new ArrayList<>();
    private ArrayList<EntitiesCoordinatesModel> coordinatesOfOrbit3 = new ArrayList<>();
    private ArrayList<EntitiesCoordinatesModel> coordinatesOfOrbit4 = new ArrayList<>();

    private ArrayList<EntitiesCoordinatesModel> equidistantPointsOfOrbits = new ArrayList<>();
    private ArrayList<EntitiesModel> entriesToShow = new ArrayList<>();


    //Constants
    private static final int EDIT_LAUNCH_STAR_REQUEST_CODE = 20;
    private static final int EDIT_UNLANUCH_STAR_REQUEST_CODE = 21;
    private static final int EDIT_LAUNCH_COMET_REQUEST_CODE = 23;
    private static final int REFRESH_PAGE = 24;

    private int entityImageRadius;
    private int noOfOrbits = 5, noOfSegments;

    //DoubleClick
    private final int doubleClickTimeout = ViewConfiguration.getDoubleTapTimeout();
    private Handler handler;
    private long firstClickTime;

    private ArrayList<Integer> entityPlaced = new ArrayList<>();


    private ImageView imgPrevMonth, imgNextMonth;
    private TextView txtCurrentMonth;
    private GridView calenderView;
    private Calendar mCalendar;
    private GridCellAdapter gridCellAdapter;
    private int cMonth, cYear, currentMonth, currentYear;
    private float x1, x2;
    private Date currentDateWithTime, currentDateWithoutTime;
    private PopupWindow popupWindow;
    private     Calendar calendar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);
        firstClickTime = 0L;
        handler = new Handler(Looper.getMainLooper());
        AppUtils.setToolbarWithBothIcon(this, "Star Page", "(Projects)", R.drawable.events_star_page_icon, R.drawable.add_comets, 0, R.drawable.to_do_button);

        sharedPreferences = getSharedPreferences(ConstantUtils.APP_PREF, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        initViews();
        setOnClick();

        progressDialog = new Dialog(this);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setContentView(R.layout.fragment_loader);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        QuickAction.setDefaultColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));
        QuickAction.setDefaultTextColor(Color.BLACK);

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

        txtQuote.setText(sharedPreferences.getString(ConstantUtils.OWN_QUOTE, ""));

        spiralView = new SpiralView(this);
        layoutCustomView.addView(spiralView);

        spiralView.setOnDragListener(this);

        unLaunchedStarsAdapter = new UnLaunchedStarsAdapter(HomeActivity.this, HomeActivity.this, unLaunchedStarsList);
        recyclerViewUnLaunched.setAdapter(unLaunchedStarsAdapter);

        archivedStarsAdapter = new ArchivedStarsAdapter(HomeActivity.this, HomeActivity.this, archivedStarsList);
        recyclerViewArchived.setAdapter(archivedStarsAdapter);

        txtQuote.setText(sharedPreferences.getString(ConstantUtils.OWN_QUOTE, ""));

        entityImageRadius = getResources().getDrawable(R.drawable.star_storage_green_priority_1).getIntrinsicHeight();


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
                for (int i = 1; i <= noOfSegments; i++) {
                    radius += deltaR;

                    x = centerX + radius * Math.cos((2 * -Math.PI * i / noOfSegments) * noOfOrbits);
                    y = centerY + radius * Math.sin((2 * -Math.PI * i / noOfSegments) * noOfOrbits);

                    double distance = Math.sqrt((last_x - x) * (last_x - x) + (last_y - y) * (last_y - y));
                    arc_distance += distance;

                    if (arc_distance > entityImageRadius) {
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


                if (ConnectivityController.isNetworkAvailable(HomeActivity.this))
                    callGetLaunchedStarsApi(true);
                else
                    AndroidUtils.showToast(HomeActivity.this, getResources().getString(R.string.no_internet));

            }
        });

        try {
            currentDateWithTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime()));
            currentDateWithoutTime = new SimpleDateFormat("yyyy-MM-dd").parse(new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }


    private void initViews() {
        progressBar = findViewById(R.id.progress_bar);
        layoutCustomView = findViewById(R.id.layout_custom_view);
        imgProfile = findViewById(R.id.img_profile);
        imgBottomMoon = findViewById(R.id.img_bottom_moon);
        AppUtils.rotateAnimation(imgBottomMoon);
        imgAdd = findViewById(R.id.img_add);
        imgNew = findViewById(R.id.img_new);
        imgArchive = findViewById(R.id.img_archive);
        layoutHour = findViewById(R.id.layout_hour);
        layoutDay = findViewById(R.id.layout_day);
        layoutMonth = findViewById(R.id.layout_month);
        layoutWeek = findViewById(R.id.layout_week);
        layoutYear = findViewById(R.id.layout_year);
        txtDoStars = findViewById(R.id.txt_do_stars);
        txtExceedStars = findViewById(R.id.txt_exceed_stars);
        imgBack = findViewById(R.id.img_back);
        viewDoStars = findViewById(R.id.view_do_stars);
        viewExceedStars = findViewById(R.id.view_exceed_stars);
        txtQuote = findViewById(R.id.txt_quote);


        //PopUp Views
        viewDim = findViewById(R.id.view_dim);
        popupGalaxy = findViewById(R.id.popup_galaxy);
        progressBarUnLaunched = findViewById(R.id.progress_bar_unlaunched);
        progressBarArchived = findViewById(R.id.progress_bar_archived);
        popupUnLaunched = findViewById(R.id.popup_unlaunched);
        popupArchived = findViewById(R.id.popup_archived);
        recyclerViewUnLaunched = findViewById(R.id.recycler_unlaunched);
        recyclerViewArchived = findViewById(R.id.recycler_archive_stars);
        recyclerViewGalaxy = findViewById(R.id.recycler_galaxy);
        recyclerViewUnLaunched.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewArchived.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewGalaxy.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setOnClick() {
        imgProfile.setOnClickListener(this);
        imgAdd.setOnClickListener(this);
        imgNew.setOnClickListener(this);
        imgArchive.setOnClickListener(this);
        viewDim.setOnClickListener(this);
        layoutHour.setOnClickListener(this);
        layoutDay.setOnClickListener(this);
        layoutWeek.setOnClickListener(this);
        layoutMonth.setOnClickListener(this);
        layoutYear.setOnClickListener(this);
        txtDoStars.setOnClickListener(this);
        txtExceedStars.setOnClickListener(this);
        imgBack.setOnClickListener(this);
    }

    @Override
    public void onClick(final View v) {
        final Intent intent;
        switch (v.getId()) {
            case R.id.img_profile:
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                break;
            case R.id.img_add:
                Intent activityIntent = new Intent(HomeActivity.this, AddTaskActivity.class);
                activityIntent.putExtra(ConstantUtils.SOURCE, "star");
                startActivityForResult(activityIntent, REFRESH_PAGE);
                overridePendingTransition(R.anim.slide_in_up, R.anim.stay);
                break;
            case R.id.toolbar_lft_img:
           /*     Intent intent1 = new Intent(HomeActivity.this, AddTaskActivity.class);
                intent1.putExtra(ConstantUtils.SOURCE, "comet");
                startActivityForResult(intent1, REFRESH_PAGE);
                overridePendingTransition(R.anim.slide_in_up, R.anim.stay);*/
                break;
            case R.id.toolbar_lft_most_img:
                //startActivityForResult(new Intent(HomeActivity.this, CometPageActivity.class), REFRESH_PAGE);
                break;
            /*case R.id.toolbar_right_img:
                try {
                    currentDateWithTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime()));
                    currentDateWithoutTime = new SimpleDateFormat("yyyy-MM-dd").parse(new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));

                    if (layoutHour.getBackground() != null)
                        layoutHour.performClick();
                    else if (layoutDay.getBackground() != null)
                        layoutDay.performClick();
                    else if (layoutWeek.getBackground() != null)
                        layoutWeek.performClick();
                    else if (layoutMonth.getBackground() != null)
                        layoutMonth.performClick();
                    else layoutYear.performClick();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                break;*/
            case R.id.toolbar_right_most_img:
                int mYear = 0, mMonth = 0, mDay = 0;

                calendar = Calendar.getInstance();
                mYear = calendar.get(Calendar.YEAR);
                mMonth = calendar.get(Calendar.MONTH);
                mDay = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(HomeActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {


                        try {
                            Date selectedDate = new SimpleDateFormat("dd/MM/yyyy").parse(String.valueOf(dayOfMonth) + "/" + String.valueOf(month + 1) + "/" + String.valueOf(year));
                            calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                            calendar.set(Calendar.MONTH,month);
                            calendar.set(Calendar.YEAR,year);
                            calendar.add(Calendar.DAY_OF_YEAR,1);
                            String formattedSelectedDate = new SimpleDateFormat("yyyy-MM-dd").format(selectedDate);
                            Intent intent1 = new Intent(HomeActivity.this, AddCalenderTaskActivity.class);
                            intent1.putExtra(ConstantUtils.SELECTED_DATE, formattedSelectedDate);
                            intent1.putExtra(ConstantUtils.NEXT_DAY_DATE,new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()));
                            startActivity(intent1);


                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }, mYear, mMonth, mDay);
                datePickerDialog.show();



              /*
                TextView txtDate, txtTime;
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.layout_calender_view, null);
                popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                popupWindow.setOutsideTouchable(true);

                imgPrevMonth = popupView.findViewById(R.id.img_prev_month);
                imgNextMonth = popupView.findViewById(R.id.img_next_month);
                txtCurrentMonth = popupView.findViewById(R.id.txt_current_month);
                calenderView = popupView.findViewById(R.id.calendar_view);

                mCalendar = Calendar.getInstance();
                cMonth = mCalendar.get(Calendar.MONTH);
                cYear = mCalendar.get(Calendar.YEAR);
                currentMonth = mCalendar.get(Calendar.MONTH);
                currentYear = mCalendar.get(Calendar.YEAR);

                imgPrevMonth.setOnClickListener(this);
                imgNextMonth.setOnClickListener(this);

                txtCurrentMonth.setText(DateFormat.format("MMMM yyyy", mCalendar.getTime()));

                gridCellAdapter = new GridCellAdapter(this, cMonth, cYear, currentMonth, currentYear);
                calenderView.setAdapter(gridCellAdapter);
                calenderView.setOnTouchListener(this);




             *//*   txtDate = popupView.findViewById(R.id.txt_date);
                txtTime = popupView.findViewById(R.id.txt_time);

                Date dateObject = null;
                try {
                    dateObject = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy").parse(String.valueOf(Calendar.getInstance().getTime()));
                    txtDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(dateObject));
                    txtTime.setText(new SimpleDateFormat("hh:mm a").format(dateObject));

                } catch (ParseException e) {
                    e.printStackTrace();
                }


                if (Build.VERSION.SDK_INT >= 21) {
                    popupWindow.setElevation(10.0f);
                }*//*
                popupWindow.showAsDropDown(findViewById(R.id.toolbar_right_most_img), 0, 0);
*/

                break;
            case R.id.img_prev_month:
                Calendar lCal = Calendar.getInstance();
                int lCurrentMonth = lCal.get(Calendar.MONTH);
                int lCurrentYear = lCal.get(Calendar.YEAR);
                boolean lisAction = false;
                if (cYear > lCurrentYear) {
                    lisAction = true;
                } else if (cYear == lCurrentYear && cMonth > lCurrentMonth) {
                    lisAction = true;
                }
                if (lisAction) {
                    if (cMonth <= 0) {
                        cMonth = 11;
                        cYear--;
                    } else {
                        cMonth--;
                    }
                    setGridCellAdapterToDate(cMonth, cYear);
                }
                break;
            case R.id.img_next_month:
                if (cMonth >= 11) {
                    cMonth = 0;
                    cYear++;
                } else {
                    cMonth++;
                }
                setGridCellAdapterToDate(cMonth, cYear);
                break;

            case R.id.img_new:
                popupUnLaunched.setVisibility(View.VISIBLE);
                popupArchived.setVisibility(View.GONE);
                viewDim.setVisibility(View.VISIBLE);

                if (ConnectivityController.isNetworkAvailable(HomeActivity.this)) {
                    if (unLaunchedStarsList.isEmpty())
                        callGetUnLaunchedStarsApi(true);
                    else callGetUnLaunchedStarsApi(false);
                } else
                    AndroidUtils.showToast(HomeActivity.this, getResources().getString(R.string.no_internet));

                break;
            case R.id.img_archive:
                popupUnLaunched.setVisibility(View.GONE);
                popupArchived.setVisibility(View.VISIBLE);
                viewDim.setVisibility(View.VISIBLE);

                if (archivedStarsList.isEmpty())
                    callGetArchivedStarsApi(true);
                else callGetArchivedStarsApi(false);

                break;
            case R.id.view_dim:
                viewDim.setVisibility(View.GONE);
                popupUnLaunched.setVisibility(View.GONE);
                popupArchived.setVisibility(View.GONE);
                popupGalaxy.setVisibility(View.GONE);
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


                for (int i = 0; i < launchedStarsList.size(); i++) {
                    StarResponseModel dataResponseModel = launchedStarsList.get(i);
                    if (dataResponseModel.getFinish_time() != null) {

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


                setDataForOrbit1("H");
                setDataForOrbit2(4);
                setDataForOrbit3(4);
                setDataForOrbit4(4);
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
/*


                for (int i = 0; i < coordinatesOfOrbit1.size(); i++)
                    entriesToShowInOrbit1.add(new StarResponseModel());
                for (int i = 0; i < coordinatesOfOrbit2.size(); i++)
                    entriesToShowInOrbit2.add(new StarResponseModel());
                for (int i = 0; i < coordinatesOfOrbit3.size(); i++)
                    entriesToShowInOrbit3.add(new StarResponseModel());
                for (int i = 0; i < coordinatesOfOrbit4.size(); i++)
                    entriesToShowInOrbit4.add(new StarResponseModel());
*/

                int daysDiff = -9;
                for (int i = 0; i < launchedStarsList.size(); i++) {
                    StarResponseModel dataResponseModel = launchedStarsList.get(i);
                    Date currentDate = null, finishDate = null, startDate = null;


                    try {


                        if (dataResponseModel.getFinish_time() != null) {
                            finishDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(dataResponseModel.getFinish_date() + " " + dataResponseModel.getFinish_time());
                            currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime()));
                            //currentDate = currentDateWithTime;
                            startDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(dataResponseModel.getStart_date() + " " + dataResponseModel.getStart_time());

                        } else {
                            finishDate = new SimpleDateFormat("yyyy-MM-dd").parse(dataResponseModel.getFinish_date());
                            currentDate = new SimpleDateFormat("yyyy-MM-dd").parse(new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));
                            //currentDate = currentDateWithoutTime;
                            startDate = new SimpleDateFormat("yyyy-MM-dd").parse(dataResponseModel.getStart_date());


                        }

                        if (startDate.after(currentDate))
                            daysDiff = AppUtils.getDayDiffFromStartDate(currentDate, startDate);
                        else daysDiff = AppUtils.getDayDiff(currentDate, finishDate);

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }


                    if (isDoStars) {
                        switch (daysDiff) {
                            case 0:

                                entriesToShowInOrbit1.add(0, dataResponseModel);

/*
                                int x = 10;
                                for (int k = 0; k < entriesToShowInOrbit1.size(); k++) {
                                    int y = 10;
                                    if (entriesToShowInOrbit1.get(k).getOrderings() != null *//*&& entriesToShowInOrbit1.get(k).getOrderings().size() > 0*//*) {
                                        int aiz = entriesToShowInOrbit1.get(k).getOrderings().size();
                                        for (int j = 0; j < aiz; j++) {
                                            if (entriesToShowInOrbit1.get(k).getOrderings() != null && entriesToShowInOrbit1.get(k).getOrderings().get(j).getParameter().equalsIgnoreCase("D")) {
                                                int orderNo = Integer.parseInt(entriesToShowInOrbit1.get(k).getOrderings().get(j).getDisplay_order());
                                                StarResponseModel starResponseModel;
                                                starResponseModel = entriesToShowInOrbit1.get(k);

                                                entriesToShowInOrbit1.remove(k);
                                                entriesToShowInOrbit1.add(orderNo, starResponseModel);
                                                int z = 0;
                                                break;


                                            }
                                        }
                                    }
                                }*/
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
               /* for (int i = 0; i < entriesToShowInOrbit1.size(); i++) {
                    if (entriesToShowInOrbit1.get(i).getOrderings().size() > 0) {
                        for (int j = 0; j < entriesToShowInOrbit1.get(i).getOrderings().size(); j++) {
                            if (entriesToShowInOrbit1.get(i).getOrderings().get(j).getParameter().equalsIgnoreCase("D")) {
                                int orderNo = Integer.parseInt(entriesToShowInOrbit1.get(i).getOrderings().get(j).getDisplay_order());
                                StarResponseModel starResponseModel;
                                starResponseModel = entriesToShowInOrbit1.get(i);

                                entriesToShowInOrbit1.remove(i);
                                entriesToShowInOrbit1.add(orderNo, starResponseModel);


                            }
                        }
                    }
                }*/
                setDataForOrbit1("D");
                setDataForOrbit2(0);
                setDataForOrbit3(0);
                setDataForOrbit4(0);


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


                for (int i = 0; i < launchedStarsList.size(); i++) {
                    StarResponseModel dataResponseModel = launchedStarsList.get(i);
                    daysDiff = 0;
                    Date currentDate = null, finishDate = null, startDate = null;
                    try {


                        if (dataResponseModel.getFinish_time() != null) {
                            finishDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(dataResponseModel.getFinish_date() + " " + dataResponseModel.getFinish_time());
                             currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(Calendar.getInstance().getTime()));
                            //currentDate = currentDateWithTime;
                            startDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(dataResponseModel.getStart_date() + " " + dataResponseModel.getStart_time());

                        } else {
                            finishDate = new SimpleDateFormat("yyyy-MM-dd").parse(dataResponseModel.getFinish_date());
                            currentDate = new SimpleDateFormat("yyyy-MM-dd").parse(new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));
                            //currentDate = currentDateWithoutTime;
                            startDate = new SimpleDateFormat("yyyy-MM-dd").parse(dataResponseModel.getStart_date());

                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }


                    if (currentDate != null && finishDate != null && startDate != null) {
                        long difference;
                        if (startDate.after(currentDate))
                            difference = startDate.getTime() - currentDate.getTime();
                        else difference = finishDate.getTime() - currentDate.getTime();

                        daysDiff = (int) TimeUnit.MILLISECONDS.toDays(difference);
                    }

                    if (isDoStars) {
                        if (AppUtils.isBetween(daysDiff, 0, 7)) {
                            entriesToShowInOrbit1.add(dataResponseModel);
                        } else if (AppUtils.isBetween(daysDiff, 8, 14)) {
                            entriesToShowInOrbit2.add(dataResponseModel);
                        } else if (AppUtils.isBetween(daysDiff, 15, 21)) {
                            entriesToShowInOrbit3.add(dataResponseModel);
                        } else if (AppUtils.isBetween(daysDiff, 22, 31)) {
                            entriesToShowInOrbit4.add(dataResponseModel);
                        }


                    } else {
                        if (AppUtils.isBetweenMinus(daysDiff, -1, -8)) {
                            entriesToShowInOrbit1.add(dataResponseModel);
                        } else if (AppUtils.isBetweenMinus(daysDiff, -9, -16)) {
                            entriesToShowInOrbit2.add(dataResponseModel);
                        } else if (AppUtils.isBetweenMinus(daysDiff, -17, -24)) {
                            entriesToShowInOrbit3.add(dataResponseModel);
                        } else if (AppUtils.isBetweenMinus(daysDiff, -25, -32)) {
                            entriesToShowInOrbit4.add(dataResponseModel);
                        }

                    }

                }
                setDataForOrbit1("W");
                setDataForOrbit2(1);
                setDataForOrbit3(1);
                setDataForOrbit4(1);
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

                for (int i = 0; i < launchedStarsList.size(); i++) {
                    StarResponseModel dataResponseModel = launchedStarsList.get(i);
                    String currentDateString;
                    currentDateString = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());

                    Date currentDate = null, finishDate = null, startDate = null;
                    int monthDiff = -11;
                    try {


                        // finishDate = new SimpleDateFormat("yyyy-MM-dd").parse(dataResponseModel.getFinish_date());
                        //currentDate = currentDateWithoutTime;
                        currentDate = new SimpleDateFormat("yyyy-MM-dd").parse(new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));
                        startDate = new SimpleDateFormat("yyyy-MM-dd").parse(dataResponseModel.getStart_date());


                        if (startDate.after(currentDate))
                            monthDiff = AppUtils.getMonthDiff(currentDateString, dataResponseModel.getStart_date());
                        else
                            monthDiff = AppUtils.getMonthDiff(currentDateString, dataResponseModel.getFinish_date());


                    } catch (ParseException e) {
                        e.printStackTrace();
                    }


                    if (isDoStars) {
                        switch (monthDiff) {
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
                        switch (monthDiff) {
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
                setDataForOrbit1("M");
                setDataForOrbit2(2);
                setDataForOrbit3(2);
                setDataForOrbit4(2);

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

                for (int i = 0; i < launchedStarsList.size(); i++) {
                    StarResponseModel dataResponseModel = launchedStarsList.get(i);
                    String currentDateString;
                    currentDateString = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
                    int yearDiff = -19;

                    Date currentDate = null, finishDate = null, startDate = null;
                    try {


                        //currentDate = currentDateWithoutTime;
                        // finishDate = new SimpleDateFormat("yyyy-MM-dd").parse(dataResponseModel.getFinish_date());
                        currentDate = new SimpleDateFormat("yyyy-MM-dd").parse(new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));
                        startDate = new SimpleDateFormat("yyyy-MM-dd").parse(dataResponseModel.getStart_date());


                        if (startDate.after(currentDate))
                            yearDiff = AppUtils.getYearDiff(currentDateString, dataResponseModel.getStart_date());
                        else
                            yearDiff = AppUtils.getYearDiff(currentDateString, dataResponseModel.getFinish_date());


                    } catch (ParseException e) {
                        e.printStackTrace();
                    }


                    if (isDoStars) {
                        switch (yearDiff) {
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

                        switch (yearDiff) {
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

                setDataForOrbit1("Y");
                setDataForOrbit2(3);
                setDataForOrbit3(3);
                setDataForOrbit4(3);
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
            case R.id.img_back:
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

    private void setGridCellAdapterToDate(int month, int year) {

        gridCellAdapter = new GridCellAdapter(this, month, year, currentMonth, currentYear);
        mCalendar.set(year, month, 1);
        txtCurrentMonth.setText(DateFormat.format("MMMM yyyy", mCalendar.getTime()));
        gridCellAdapter.notifyDataSetChanged();
        calenderView.setAdapter(gridCellAdapter);
    }


    private void onDoubleClick(View v) {
        infoPopUpFragment = new InfoPopUpFragment();
        Bundle bundle = null;
        for (int i = 0; i < launchedStarsList.size(); i++) {
            if (launchedStarsList.get(i).getId() != null && v.getId() == Integer.parseInt(launchedStarsList.get(i).getId())) {
                if (launchedStarsList.get(i).getType().equalsIgnoreCase("S")) {
                    bundle = new Bundle();
                    bundle.putParcelable(ConstantUtils.STAR_RESPONSE_MODEL, launchedStarsList.get(i));
                    bundle.putInt(ConstantUtils.TASK_POSITION, i);
                } else {
                    bundle = new Bundle();
                    bundle.putParcelable(ConstantUtils.STAR_RESPONSE_MODEL, launchedStarsList.get(i));
                    bundle.putInt(ConstantUtils.TASK_POSITION, i);
                }

            } /*else if (launchedStarsList.get(i).getComet_id() != null && v.getId() == Integer.parseInt(launchedStarsList.get(i).getComet_id())) {
                bundle = new Bundle();
                CometResponseModel cometResponseModel = new CometResponseModel();
                cometResponseModel.setComet_id(launchedStarsList.get(i).getComet_id());
                cometResponseModel.setUser_id(launchedStarsList.get(i).getUser_id());
                cometResponseModel.setName(launchedStarsList.get(i).getName());
                cometResponseModel.setDescription(launchedStarsList.get(i).getDescription());
                cometResponseModel.setStart_date(launchedStarsList.get(i).getStart_date());
                cometResponseModel.setFinish_date(launchedStarsList.get(i).getFinish_date());
                cometResponseModel.setAlarm_date(launchedStarsList.get(i).getAlarm_date());
                cometResponseModel.setAlarm_time(launchedStarsList.get(i).getAlarm_time());
                cometResponseModel.setNotes(launchedStarsList.get(i).getNotes());
                cometResponseModel.setPriority(launchedStarsList.get(i).getPriority());
                cometResponseModel.setMembers(launchedStarsList.get(i).getMembers());
                cometResponseModel.setDocuments(launchedStarsList.get(i).getDocuments());

                bundle.putParcelable(ConstantUtils.COMET_RESPONSE_MODEL, cometResponseModel);
                bundle.putInt(ConstantUtils.TASK_POSITION, i);
            }*/
        }
        if (bundle != null && bundle.size() > 0) {
            infoPopUpFragment.setArguments(bundle);
            infoPopUpFragment.setInfoPopUpListener(this);
            infoPopUpFragment.show(getSupportFragmentManager(), infoPopUpFragment.getTag());
            infoPopUpFragment.setCancelable(false);
        }
    }

    private void onSingleClick(View v) {
        for (int i = 0; i < launchedStarsList.size(); i++) {
            if (launchedStarsList.get(i).getId() != null && v.getId() == Integer.parseInt(launchedStarsList.get(i).getId())) {

                Intent intent = new Intent(HomeActivity.this, PlanetPageActivity.class);
                intent.putExtra(ConstantUtils.TASK_POSITION, i);
                intent.putExtra(ConstantUtils.STAR_RESPONSE_MODEL, launchedStarsList.get(i));
                startActivityForResult(intent, EDIT_LAUNCH_STAR_REQUEST_CODE);

            }
        }
    }

    private void callGetLaunchedStarsApi(boolean needLoader) {
        if (needLoader)
            progressBar.setVisibility(View.VISIBLE);
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetLaunchedStarsResponseModel> getAllStarResponseModelCall = apiService.getLaunchedStars(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""));
        getAllStarResponseModelCall.enqueue(new Callback<GetLaunchedStarsResponseModel>() {
            @Override
            public void onResponse(Call<GetLaunchedStarsResponseModel> call, Response<GetLaunchedStarsResponseModel> response) {
                progressBar.setVisibility(View.GONE);
                if (response.code() == 200 && response.body() != null) {
                    if (response.body().getData() != null) {


                        launchedStarsList.clear();
                        launchedStarsList.addAll(response.body().getData());

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
                    AndroidUtils.showToast(HomeActivity.this, getResources().getString(R.string.some_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<GetLaunchedStarsResponseModel> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                AndroidUtils.showToast(HomeActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });
    }

    private void callGetUnLaunchedStarsApi(boolean needToShowProgressbar) {
        if (needToShowProgressbar)
            progressBarUnLaunched.setVisibility(View.VISIBLE);
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetUnLaunchedStarsResponseModel> getUnLaunchedStarsResponseCall = apiService.getUnLaunchedStars(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""));

        getUnLaunchedStarsResponseCall.enqueue(new Callback<GetUnLaunchedStarsResponseModel>() {
            @Override
            public void onResponse(Call<GetUnLaunchedStarsResponseModel> call, Response<GetUnLaunchedStarsResponseModel> response) {
                progressBarUnLaunched.setVisibility(View.GONE);
                if (response.code() == 200 && response.body() != null) {

                    if (response.body().getStars() != null && !response.body().getStars().isEmpty()) {
                        unLaunchedStarsList.clear();
                        unLaunchedStarsList.addAll(response.body().getStars());
                        unLaunchedStarsAdapter.notifyDataSetChanged();
                    } else {
                        popupUnLaunched.setVisibility(View.GONE);
                        viewDim.setVisibility(View.GONE);
                        AndroidUtils.showToast(HomeActivity.this, "No new stars waiting for dates.");
                    }


                } else if (response.code() == 500) {
                    AndroidUtils.showToast(HomeActivity.this, getResources().getString(R.string.some_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<GetUnLaunchedStarsResponseModel> call, Throwable t) {
                progressBarUnLaunched.setVisibility(View.GONE);
                AndroidUtils.showToast(HomeActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });

    }


    private void callGetArchivedStarsApi(boolean needToShowProgressbar) {
        if (needToShowProgressbar)
            progressBarArchived.setVisibility(View.VISIBLE);
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetUnLaunchedStarsResponseModel> archivedStarsResponseCall = apiService.getArchivedStars(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""));
        archivedStarsResponseCall.enqueue(new Callback<GetUnLaunchedStarsResponseModel>() {
            @Override
            public void onResponse(Call<GetUnLaunchedStarsResponseModel> call, Response<GetUnLaunchedStarsResponseModel> response) {
                progressBarArchived.setVisibility(View.GONE);
                if (response.code() == 200 && response.body() != null) {
                    if (response.body().getStars() != null && !response.body().getStars().isEmpty()) {
                        archivedStarsList.clear();
                        archivedStarsList.addAll(response.body().getStars());
                        archivedStarsAdapter.notifyDataSetChanged();
                    } else {
                        popupArchived.setVisibility(View.GONE);
                        viewDim.setVisibility(View.GONE);
                        AndroidUtils.showToast(HomeActivity.this, "No old stars.");
                    }
                }
            }

            @Override
            public void onFailure(Call<GetUnLaunchedStarsResponseModel> call, Throwable t) {
                progressBarArchived.setVisibility(View.GONE);
                AndroidUtils.showToast(HomeActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });

    }


    private void setDataForOrbit1(String parameter) {

        /*TextView orbitEntryImage;
        final int noOfEntities = coordinatesOfOrbit1.size();
        int entitiesToShow = entriesToShowInOrbit1.size();
        ArrayList<StarResponseModel> galaxyList = new ArrayList<>();

        for (int i = 0; i < entriesToShowInOrbit1.size(); i++) {
            if (entriesToShowInOrbit1.get(i).getOrderings().get(type) < noOfEntities - 1) {
                if (entriesToShowInOrbit1.get(i).getStar_id() != null) {
                    orbitEntryImage = new TextView(HomeActivity.this);
                    orbitEntryImage.setLayoutParams(new ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                    orbitEntryImage.setTypeface(ResourcesCompat.getFont(HomeActivity.this, R.font.teko_medium));
                    orbitEntryImage.setTextColor(ContextCompat.getColor(HomeActivity.this, R.color.colorBlack));
                    orbitEntryImage.setPadding(20, 1, 20, 1);
                    orbitEntryImage.setTag(1);
                    orbitEntryImage.setId(Integer.parseInt(entriesToShowInOrbit1.get(i).getStar_id()));
                    orbitEntryImage.setText(entriesToShowInOrbit1.get(i).getName());
                    orbitEntryImage.setTextSize(11f);
                    orbitEntryImage.setGravity(Gravity.CENTER);
                    orbitEntryImage.setMaxLines(1);
                    setEntityImage(orbitEntryImage, 1, Integer.valueOf(entriesToShowInOrbit1.get(i).getPriority()), entriesToShowInOrbit1.get(i).getStart_date(), entriesToShowInOrbit1.get(i).getFinish_date());
                    orbitEntryImage.setX((float) (coordinatesOfOrbit1.get(entriesToShowInOrbit1.get(i).getOrderings().get(type)).getX() - entityImageRadius / 2));
                    orbitEntryImage.setY((float) (coordinatesOfOrbit1.get(entriesToShowInOrbit1.get(i).getOrderings().get(type)).getY() - entityImageRadius / 2));
                    orbitEntryImage.setOnClickListener(this);
                    orbitEntryImage.setOnLongClickListener(this);
                    layoutCustomView.addView(orbitEntryImage);
                }
            } else {
                galaxyList.add(entriesToShowInOrbit1.get(i));
            }
        }


        if (galaxyList.size() > 0) {
            orbitEntryImage = new TextView(HomeActivity.this);
            orbitEntryImage.setLayoutParams(new ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
            orbitEntryImage.setTypeface(ResourcesCompat.getFont(HomeActivity.this, R.font.teko_medium));
            orbitEntryImage.setTextColor(ContextCompat.getColor(HomeActivity.this, R.color.colorBlack));
            orbitEntryImage.setPadding(10, entityImageRadius / 4, 10, 1);
            orbitEntryImage.setText("" + galaxyList.size());
            orbitEntryImage.setTextSize(10f);
            orbitEntryImage.setGravity(Gravity.CENTER_HORIZONTAL);
            orbitEntryImage.setMaxLines(1);
            orbitEntryImage.setBackgroundResource(R.drawable.galaxy_tail);
            orbitEntryImage.setX((float) (coordinatesOfOrbit1.get(noOfEntities - 1).getX() - entityImageRadius / 2));
            orbitEntryImage.setY((float) (coordinatesOfOrbit1.get(noOfEntities - 1).getY() - entityImageRadius / 2));
            orbitEntryImage.setOnClickListener(this);
            layoutCustomView.addView(orbitEntryImage);


        }*/

        TextView orbitEntryImage;
        final int noOfEntities = coordinatesOfOrbit1.size();
        int entitiesToShow = entriesToShowInOrbit1.size();

        int v;

        if (entitiesToShow > noOfEntities)
            v = 1;
        else
            v = 0;

        for (int i = 0; i < entriesToShowInOrbit1.size(); i++) {
            if (i <= noOfEntities - 1 && v < noOfEntities) {

                v++;

                if (entriesToShowInOrbit1.get(i).getType() != null && entriesToShowInOrbit1.get(i).getType().equalsIgnoreCase("S")) {
                    orbitEntryImage = new TextView(HomeActivity.this);
                    orbitEntryImage.setLayoutParams(new ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                    orbitEntryImage.setTypeface(ResourcesCompat.getFont(HomeActivity.this, R.font.teko_medium));
                    orbitEntryImage.setTextColor(ContextCompat.getColor(HomeActivity.this, R.color.colorBlack));
                    orbitEntryImage.setPadding(20, 1, 20, 1);
                    orbitEntryImage.setTag(1);
                    orbitEntryImage.setId(Integer.parseInt(entriesToShowInOrbit1.get(i).getId()));
                    orbitEntryImage.setText(entriesToShowInOrbit1.get(i).getName());
                    //orbitEntryImage.setEllipsize(TextUtils.TruncateAt.END);
                    orbitEntryImage.setTextSize(11f);
                    orbitEntryImage.setGravity(Gravity.CENTER);
                    orbitEntryImage.setMaxLines(1);

                    setEntityImage(orbitEntryImage, 1, Integer.valueOf(entriesToShowInOrbit1.get(i).getPriority()), entriesToShowInOrbit1.get(i).getStart_date(), entriesToShowInOrbit1.get(i).getFinish_date());
                    /*int orderNo = -1;
                    for (int j = 0; j < entriesToShowInOrbit1.get(i).getOrderings().size(); j++) {

                        if (entriesToShowInOrbit1.get(i).getOrderings().get(j).getParameter().equalsIgnoreCase(parameter)) {
                            orderNo = Integer.parseInt(entriesToShowInOrbit1.get(i).getOrderings().get(j).getDisplay_order());

                        }
                    }
                    if (orderNo == -1) {

                        orbitEntryImage.setX((float) (coordinatesOfOrbit1.get(i).getX() - entityImageRadius / 2));
                        orbitEntryImage.setY((float) (coordinatesOfOrbit1.get(i).getY() - entityImageRadius / 2));


                    } else {
                        orbitEntryImage.setX((float) (coordinatesOfOrbit1.get(orderNo).getX() - entityImageRadius / 2));
                        orbitEntryImage.setY((float) (coordinatesOfOrbit1.get(orderNo).getY() - entityImageRadius / 2));
                    }*/
                    orbitEntryImage.setX((float) (coordinatesOfOrbit1.get(i).getX() - entityImageRadius / 2));
                    orbitEntryImage.setY((float) (coordinatesOfOrbit1.get(i).getY() - entityImageRadius / 2));
                    orbitEntryImage.setOnClickListener(this);
                    orbitEntryImage.setOnLongClickListener(this);
                    layoutCustomView.addView(orbitEntryImage);
                } else if (entriesToShowInOrbit1.get(i).getType() != null && entriesToShowInOrbit1.get(i).getType().equalsIgnoreCase("C")) {
                    orbitEntryImage = new TextView(HomeActivity.this);
                    orbitEntryImage.setLayoutParams(new ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                    orbitEntryImage.setTypeface(ResourcesCompat.getFont(HomeActivity.this, R.font.teko_medium));
                    orbitEntryImage.setTextColor(ContextCompat.getColor(HomeActivity.this, R.color.colorBlack));
                    orbitEntryImage.setPadding(20, entityImageRadius / 4, 20, 1);
                    orbitEntryImage.setId(Integer.parseInt(entriesToShowInOrbit1.get(i).getId()));
                    orbitEntryImage.setText(entriesToShowInOrbit1.get(i).getName());
                    orbitEntryImage.setTag(1);
                    // orbitEntryImage.setEllipsize(TextUtils.TruncateAt.END);
                    orbitEntryImage.setTextSize(11f);
                    orbitEntryImage.setGravity(Gravity.CENTER_HORIZONTAL);
                    orbitEntryImage.setMaxLines(1);
                    setCometEntityImage(orbitEntryImage, 1, Integer.valueOf(entriesToShowInOrbit1.get(i).getPriority()), entriesToShowInOrbit1.get(i).getStart_date(), entriesToShowInOrbit1.get(i).getFinish_date());
                    orbitEntryImage.setX((float) (coordinatesOfOrbit1.get(i).getX() - entityImageRadius / 2));
                    orbitEntryImage.setY((float) (coordinatesOfOrbit1.get(i).getY() - entityImageRadius / 2));
                    orbitEntryImage.setOnClickListener(this);
                    orbitEntryImage.setOnLongClickListener(this);
                    layoutCustomView.addView(orbitEntryImage);
                }
            } else if (entriesToShowInOrbit1.size() > noOfEntities) {

                // galaxyListOrbit1.add(entriesToShowInOrbit1.get(i));

                orbitEntryImage = new TextView(HomeActivity.this);
                orbitEntryImage.setLayoutParams(new ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                orbitEntryImage.setTypeface(ResourcesCompat.getFont(HomeActivity.this, R.font.teko_medium));
                orbitEntryImage.setTextColor(ContextCompat.getColor(HomeActivity.this, R.color.colorBlack));
                orbitEntryImage.setPadding(10, entityImageRadius / 4, 10, 1);
                orbitEntryImage.setText(entriesToShowInOrbit1.size() - noOfEntities + "+");
                orbitEntryImage.setTextSize(10f);
                orbitEntryImage.setGravity(Gravity.CENTER_HORIZONTAL);
                orbitEntryImage.setMaxLines(1);
                orbitEntryImage.setBackgroundResource(R.drawable.galaxy_tail);
                orbitEntryImage.setX((float) (coordinatesOfOrbit1.get(i).getX() - entityImageRadius / 2));
                orbitEntryImage.setY((float) (coordinatesOfOrbit1.get(i).getY() - entityImageRadius / 2));


                orbitEntryImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupGalaxy.setVisibility(View.VISIBLE);
                        viewDim.setVisibility(View.VISIBLE);
                        ArrayList<StarResponseModel> galaxyListOrbit1 = new ArrayList<>();

                        for (int l = noOfEntities - 1; l < entriesToShowInOrbit1.size(); l++)
                            galaxyListOrbit1.add(entriesToShowInOrbit1.get(l));

                        GalaxyStarsAdapter galaxyStarsAdapter = new GalaxyStarsAdapter(HomeActivity.this, galaxyListOrbit1, HomeActivity.this);
                        recyclerViewGalaxy.setAdapter(galaxyStarsAdapter);

                    }
                });
                layoutCustomView.addView(orbitEntryImage);
                break;

            }
        }
    }

    private void setDataForOrbit2(int type) {

      /*  TextView orbitEntryImage;
        final int noOfEntities = coordinatesOfOrbit2.size();
        ArrayList<StarResponseModel> galaxyList = new ArrayList<>();

        for (int i = 0; i < entriesToShowInOrbit2.size(); i++) {
            if (entriesToShowInOrbit2.get(i).getOrderings().get(type) < noOfEntities - 1) {
                if (entriesToShowInOrbit2.get(i).getStar_id() != null) {
                    orbitEntryImage = new TextView(HomeActivity.this);
                    orbitEntryImage.setLayoutParams(new ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                    orbitEntryImage.setTypeface(ResourcesCompat.getFont(HomeActivity.this, R.font.teko_medium));
                    orbitEntryImage.setTextColor(ContextCompat.getColor(HomeActivity.this, R.color.colorBlack));
                    orbitEntryImage.setPadding(20, 1, 20, 1);
                    orbitEntryImage.setTag(2);
                    orbitEntryImage.setId(Integer.parseInt(entriesToShowInOrbit2.get(i).getStar_id()));
                    orbitEntryImage.setText(entriesToShowInOrbit2.get(i).getName());
                    orbitEntryImage.setTextSize(11f);
                    orbitEntryImage.setGravity(Gravity.CENTER);
                    orbitEntryImage.setMaxLines(1);
                    setEntityImage(orbitEntryImage, 1, Integer.valueOf(entriesToShowInOrbit2.get(i).getPriority()), entriesToShowInOrbit2.get(i).getStart_date(), entriesToShowInOrbit2.get(i).getFinish_date());
                    orbitEntryImage.setX((float) (coordinatesOfOrbit2.get(entriesToShowInOrbit2.get(i).getOrderings().get(type)).getX() - entityImageRadius / 2));
                    orbitEntryImage.setY((float) (coordinatesOfOrbit2.get(entriesToShowInOrbit2.get(i).getOrderings().get(type)).getY() - entityImageRadius / 2));
                    orbitEntryImage.setOnClickListener(this);
                    orbitEntryImage.setOnLongClickListener(this);
                    layoutCustomView.addView(orbitEntryImage);
                }
            } else {
                galaxyList.add(entriesToShowInOrbit2.get(i));
            }
        }


        if (galaxyList.size() > 0) {
            orbitEntryImage = new TextView(HomeActivity.this);
            orbitEntryImage.setLayoutParams(new ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
            orbitEntryImage.setTypeface(ResourcesCompat.getFont(HomeActivity.this, R.font.teko_medium));
            orbitEntryImage.setTextColor(ContextCompat.getColor(HomeActivity.this, R.color.colorBlack));
            orbitEntryImage.setPadding(10, entityImageRadius / 4, 10, 1);
            orbitEntryImage.setText("" + galaxyList.size());
            orbitEntryImage.setTextSize(10f);
            orbitEntryImage.setGravity(Gravity.CENTER_HORIZONTAL);
            orbitEntryImage.setMaxLines(1);
            orbitEntryImage.setBackgroundResource(R.drawable.galaxy_tail);
            orbitEntryImage.setX((float) (coordinatesOfOrbit2.get(noOfEntities - 1).getX() - entityImageRadius / 2));
            orbitEntryImage.setY((float) (coordinatesOfOrbit2.get(noOfEntities - 1).getY() - entityImageRadius / 2));
            orbitEntryImage.setOnClickListener(this);
            layoutCustomView.addView(orbitEntryImage);
        }*/


        TextView orbitEntryImage;
        final int noOfEntities = coordinatesOfOrbit2.size();
        int entitiesToShow = entriesToShowInOrbit2.size();
        int v;

        if (entitiesToShow > noOfEntities)
            v = 1;
        else
            v = 0;
        for (int i = 0; i < entriesToShowInOrbit2.size(); i++) {
            if (i < noOfEntities - 1 && v < noOfEntities) {
                v++;
                if (entriesToShowInOrbit2.get(i).getType() != null && entriesToShowInOrbit2.get(i).getType().equalsIgnoreCase("S")) {
                    orbitEntryImage = new TextView(HomeActivity.this);
                    orbitEntryImage.setLayoutParams(new ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                    orbitEntryImage.setTypeface(ResourcesCompat.getFont(HomeActivity.this, R.font.teko_medium));
                    orbitEntryImage.setTextColor(ContextCompat.getColor(HomeActivity.this, R.color.colorBlack));
                    orbitEntryImage.setId(Integer.parseInt(entriesToShowInOrbit2.get(i).getId()));
                    orbitEntryImage.setPadding(20, 1, 20, 1);
                    orbitEntryImage.setTag(2);
                    orbitEntryImage.setText(entriesToShowInOrbit2.get(i).getName());
                    //  orbitEntryImage.setEllipsize(TextUtils.TruncateAt.END);
                    orbitEntryImage.setTextSize(11f);
                    orbitEntryImage.setGravity(Gravity.CENTER);
                    orbitEntryImage.setMaxLines(1);
                    setEntityImage(orbitEntryImage, 2, Integer.valueOf(entriesToShowInOrbit2.get(i).getPriority()), entriesToShowInOrbit2.get(i).getStart_date(), entriesToShowInOrbit2.get(i).getFinish_date());
                    orbitEntryImage.setX((float) (coordinatesOfOrbit2.get(i).getX() - entityImageRadius / 2));
                    orbitEntryImage.setY((float) (coordinatesOfOrbit2.get(i).getY() - entityImageRadius / 2));
                    orbitEntryImage.setOnClickListener(this);
                    orbitEntryImage.setOnLongClickListener(this);
                    layoutCustomView.addView(orbitEntryImage);
                } else if (entriesToShowInOrbit2.get(i).getType() != null && entriesToShowInOrbit2.get(i).getType().equalsIgnoreCase("C")) {
                    orbitEntryImage = new TextView(HomeActivity.this);
                    orbitEntryImage.setLayoutParams(new ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                    orbitEntryImage.setTypeface(ResourcesCompat.getFont(HomeActivity.this, R.font.teko_medium));
                    orbitEntryImage.setTextColor(ContextCompat.getColor(HomeActivity.this, R.color.colorBlack));
                    orbitEntryImage.setId(Integer.parseInt(entriesToShowInOrbit2.get(i).getId()));
                    orbitEntryImage.setPadding(20, entityImageRadius / 4, 20, 1);
                    orbitEntryImage.setText(entriesToShowInOrbit2.get(i).getName());
                    orbitEntryImage.setTag(2);
                    //  orbitEntryImage.setEllipsize(TextUtils.TruncateAt.END);
                    orbitEntryImage.setTextSize(11f);
                    orbitEntryImage.setGravity(Gravity.CENTER_HORIZONTAL);
                    orbitEntryImage.setMaxLines(1);
                    setCometEntityImage(orbitEntryImage, 2, Integer.valueOf(entriesToShowInOrbit2.get(i).getPriority()), entriesToShowInOrbit2.get(i).getStart_date(), entriesToShowInOrbit2.get(i).getFinish_date());
                    orbitEntryImage.setX((float) (coordinatesOfOrbit2.get(i).getX() - entityImageRadius / 2));
                    orbitEntryImage.setY((float) (coordinatesOfOrbit2.get(i).getY() - entityImageRadius / 2));
                    orbitEntryImage.setOnLongClickListener(this);
                    orbitEntryImage.setOnClickListener(this);
                    layoutCustomView.addView(orbitEntryImage);
                }
            } else if (entriesToShowInOrbit2.size() > noOfEntities) {

                orbitEntryImage = new TextView(HomeActivity.this);
                orbitEntryImage.setLayoutParams(new ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                orbitEntryImage.setTypeface(ResourcesCompat.getFont(HomeActivity.this, R.font.teko_medium));
                orbitEntryImage.setTextColor(ContextCompat.getColor(HomeActivity.this, R.color.colorBlack));
                orbitEntryImage.setPadding(10, entityImageRadius / 4, 10, 1);
                orbitEntryImage.setText(entriesToShowInOrbit2.size() - noOfEntities + "+");
                orbitEntryImage.setTextSize(10f);
                orbitEntryImage.setGravity(Gravity.CENTER_HORIZONTAL);
                orbitEntryImage.setMaxLines(1);
                orbitEntryImage.setBackgroundResource(R.drawable.galaxy_tail);
                orbitEntryImage.setX((float) (coordinatesOfOrbit2.get(i).getX() - entityImageRadius / 2));
                orbitEntryImage.setY((float) (coordinatesOfOrbit2.get(i).getY() - entityImageRadius / 2));

                orbitEntryImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupGalaxy.setVisibility(View.VISIBLE);
                        viewDim.setVisibility(View.VISIBLE);
                        ArrayList<StarResponseModel> galaxyListOrbit = new ArrayList<>();

                        for (int l = noOfEntities - 1; l < entriesToShowInOrbit2.size(); l++)
                            galaxyListOrbit.add(entriesToShowInOrbit2.get(l));

                        GalaxyStarsAdapter galaxyStarsAdapter = new GalaxyStarsAdapter(HomeActivity.this, galaxyListOrbit, HomeActivity.this);
                        recyclerViewGalaxy.setAdapter(galaxyStarsAdapter);

                    }
                });
                layoutCustomView.addView(orbitEntryImage);
                break;
            }
        }

    }


    private void setDataForOrbit3(int type) {

        /*TextView orbitEntryImage;
        final int noOfEntities = coordinatesOfOrbit3.size();
        ArrayList<StarResponseModel> galaxyList = new ArrayList<>();

        for (int i = 0; i < entriesToShowInOrbit3.size(); i++) {
            if (entriesToShowInOrbit3.get(i).getOrderings().get(type) < noOfEntities - 1) {
                if (entriesToShowInOrbit3.get(i).getStar_id() != null) {
                    orbitEntryImage = new TextView(HomeActivity.this);
                    orbitEntryImage.setLayoutParams(new ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                    orbitEntryImage.setTypeface(ResourcesCompat.getFont(HomeActivity.this, R.font.teko_medium));
                    orbitEntryImage.setTextColor(ContextCompat.getColor(HomeActivity.this, R.color.colorBlack));
                    orbitEntryImage.setPadding(20, 1, 20, 1);
                    orbitEntryImage.setTag(3);
                    orbitEntryImage.setId(Integer.parseInt(entriesToShowInOrbit3.get(i).getStar_id()));
                    orbitEntryImage.setText(entriesToShowInOrbit3.get(i).getName());
                    orbitEntryImage.setTextSize(11f);
                    orbitEntryImage.setGravity(Gravity.CENTER);
                    orbitEntryImage.setMaxLines(1);
                    setEntityImage(orbitEntryImage, 1, Integer.valueOf(entriesToShowInOrbit3.get(i).getPriority()), entriesToShowInOrbit3.get(i).getStart_date(), entriesToShowInOrbit3.get(i).getFinish_date());
                    orbitEntryImage.setX((float) (coordinatesOfOrbit3.get(entriesToShowInOrbit3.get(i).getOrderings().get(type)).getX() - entityImageRadius / 2));
                    orbitEntryImage.setY((float) (coordinatesOfOrbit3.get(entriesToShowInOrbit3.get(i).getOrderings().get(type)).getY() - entityImageRadius / 2));
                    orbitEntryImage.setOnClickListener(this);
                    orbitEntryImage.setOnLongClickListener(this);
                    layoutCustomView.addView(orbitEntryImage);
                }
            } else {
                galaxyList.add(entriesToShowInOrbit3.get(i));
            }
        }


        if (galaxyList.size() > 0) {
            orbitEntryImage = new TextView(HomeActivity.this);
            orbitEntryImage.setLayoutParams(new ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
            orbitEntryImage.setTypeface(ResourcesCompat.getFont(HomeActivity.this, R.font.teko_medium));
            orbitEntryImage.setTextColor(ContextCompat.getColor(HomeActivity.this, R.color.colorBlack));
            orbitEntryImage.setPadding(10, entityImageRadius / 4, 10, 1);
            orbitEntryImage.setText("" + galaxyList.size());
            orbitEntryImage.setTextSize(10f);
            orbitEntryImage.setGravity(Gravity.CENTER_HORIZONTAL);
            orbitEntryImage.setMaxLines(1);
            orbitEntryImage.setBackgroundResource(R.drawable.galaxy_tail);
            orbitEntryImage.setX((float) (coordinatesOfOrbit3.get(noOfEntities - 1).getX() - entityImageRadius / 2));
            orbitEntryImage.setY((float) (coordinatesOfOrbit3.get(noOfEntities - 1).getY() - entityImageRadius / 2));
            orbitEntryImage.setOnClickListener(this);
            layoutCustomView.addView(orbitEntryImage);
        }*/


        TextView orbitEntryImage;
        final int noOfEntities = coordinatesOfOrbit3.size();
        int entitiesToShow = entriesToShowInOrbit3.size();
        int v;

        if (entitiesToShow > noOfEntities)
            v = 1;
        else
            v = 0;
        for (int i = 0; i < entriesToShowInOrbit3.size(); i++) {
            if (i < noOfEntities - 1 && v < noOfEntities) {
                v++;
                if (entriesToShowInOrbit3.get(i).getType() != null && entriesToShowInOrbit3.get(i).getType().equalsIgnoreCase("S")) {

                    orbitEntryImage = new TextView(HomeActivity.this);
                    orbitEntryImage.setLayoutParams(new ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                    orbitEntryImage.setTypeface(ResourcesCompat.getFont(HomeActivity.this, R.font.teko_medium));
                    orbitEntryImage.setTextColor(ContextCompat.getColor(HomeActivity.this, R.color.colorBlack));
                    orbitEntryImage.setId(Integer.parseInt(entriesToShowInOrbit3.get(i).getId()));
                    orbitEntryImage.setPadding(20, 1, 20, 1);
                    orbitEntryImage.setText(entriesToShowInOrbit3.get(i).getName());
                    // orbitEntryImage.setEllipsize(TextUtils.TruncateAt.END);
                    orbitEntryImage.setTextSize(11f);
                    orbitEntryImage.setTag(3);
                    orbitEntryImage.setGravity(Gravity.CENTER);
                    orbitEntryImage.setMaxLines(1);
                    setEntityImage(orbitEntryImage, 3, Integer.valueOf(entriesToShowInOrbit3.get(i).getPriority()), entriesToShowInOrbit3.get(i).getStart_date(), entriesToShowInOrbit3.get(i).getFinish_date());
                    orbitEntryImage.setX((float) (coordinatesOfOrbit3.get(i).getX() - entityImageRadius / 2));
                    orbitEntryImage.setY((float) (coordinatesOfOrbit3.get(i).getY() - entityImageRadius / 2));
                    orbitEntryImage.setOnClickListener(this);
                    orbitEntryImage.setOnLongClickListener(this);
                    layoutCustomView.addView(orbitEntryImage);
                } else if (entriesToShowInOrbit3.get(i).getType() != null && entriesToShowInOrbit2.get(i).getType().equalsIgnoreCase("C")) {
                    orbitEntryImage = new TextView(HomeActivity.this);
                    orbitEntryImage.setLayoutParams(new ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                    orbitEntryImage.setTypeface(ResourcesCompat.getFont(HomeActivity.this, R.font.teko_medium));
                    orbitEntryImage.setTextColor(ContextCompat.getColor(HomeActivity.this, R.color.colorBlack));
                    orbitEntryImage.setId(Integer.parseInt(entriesToShowInOrbit3.get(i).getId()));
                    orbitEntryImage.setPadding(20, entityImageRadius / 4, 20, 1);
                    orbitEntryImage.setText(entriesToShowInOrbit3.get(i).getName());
                    orbitEntryImage.setTag(3);
                    //orbitEntryImage.setEllipsize(TextUtils.TruncateAt.END);
                    orbitEntryImage.setTextSize(11f);
                    orbitEntryImage.setGravity(Gravity.CENTER_HORIZONTAL);
                    orbitEntryImage.setMaxLines(1);
                    setCometEntityImage(orbitEntryImage, 3, Integer.valueOf(entriesToShowInOrbit3.get(i).getPriority()), entriesToShowInOrbit3.get(i).getStart_date(), entriesToShowInOrbit3.get(i).getFinish_date());
                    orbitEntryImage.setX((float) (coordinatesOfOrbit3.get(i).getX() - entityImageRadius / 2));
                    orbitEntryImage.setY((float) (coordinatesOfOrbit3.get(i).getY() - entityImageRadius / 2));
                    orbitEntryImage.setOnLongClickListener(this);
                    orbitEntryImage.setOnClickListener(this);
                    layoutCustomView.addView(orbitEntryImage);
                }
            } else if (entriesToShowInOrbit3.size() > noOfEntities) {

                orbitEntryImage = new TextView(HomeActivity.this);
                orbitEntryImage.setLayoutParams(new ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                orbitEntryImage.setTypeface(ResourcesCompat.getFont(HomeActivity.this, R.font.teko_medium));
                orbitEntryImage.setTextColor(ContextCompat.getColor(HomeActivity.this, R.color.colorBlack));
                orbitEntryImage.setPadding(10, entityImageRadius / 4, 10, 1);
                orbitEntryImage.setText(entriesToShowInOrbit3.size() - noOfEntities + "+");
                orbitEntryImage.setTextSize(10f);
                orbitEntryImage.setGravity(Gravity.CENTER_HORIZONTAL);
                orbitEntryImage.setMaxLines(1);
                orbitEntryImage.setBackgroundResource(R.drawable.galaxy_tail);
                orbitEntryImage.setX((float) (coordinatesOfOrbit3.get(i).getX() - entityImageRadius / 2));
                orbitEntryImage.setY((float) (coordinatesOfOrbit3.get(i).getY() - entityImageRadius / 2));
                orbitEntryImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupGalaxy.setVisibility(View.VISIBLE);
                        viewDim.setVisibility(View.VISIBLE);
                        ArrayList<StarResponseModel> galaxyListOrbit = new ArrayList<>();

                        for (int l = noOfEntities - 1; l < entriesToShowInOrbit3.size(); l++)
                            galaxyListOrbit.add(entriesToShowInOrbit3.get(l));

                        GalaxyStarsAdapter galaxyStarsAdapter = new GalaxyStarsAdapter(HomeActivity.this, galaxyListOrbit, HomeActivity.this);
                        recyclerViewGalaxy.setAdapter(galaxyStarsAdapter);

                    }
                });
                layoutCustomView.addView(orbitEntryImage);
                break;
            }
        }
    }

    private void setDataForOrbit4(int type) {

      /*  TextView orbitEntryImage;
        final int noOfEntities = coordinatesOfOrbit4.size();
        ArrayList<StarResponseModel> galaxyList = new ArrayList<>();

        for (int i = 0; i < entriesToShowInOrbit4.size(); i++) {
            if (entriesToShowInOrbit4.get(i).getOrderings().get(type) < noOfEntities - 1) {
                if (entriesToShowInOrbit4.get(i).getStar_id() != null) {
                    orbitEntryImage = new TextView(HomeActivity.this);
                    orbitEntryImage.setLayoutParams(new ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                    orbitEntryImage.setTypeface(ResourcesCompat.getFont(HomeActivity.this, R.font.teko_medium));
                    orbitEntryImage.setTextColor(ContextCompat.getColor(HomeActivity.this, R.color.colorBlack));
                    orbitEntryImage.setPadding(20, 1, 20, 1);
                    orbitEntryImage.setTag(4);
                    orbitEntryImage.setId(Integer.parseInt(entriesToShowInOrbit4.get(i).getStar_id()));
                    orbitEntryImage.setText(entriesToShowInOrbit4.get(i).getName());
                    orbitEntryImage.setTextSize(11f);
                    orbitEntryImage.setGravity(Gravity.CENTER);
                    orbitEntryImage.setMaxLines(1);
                    setEntityImage(orbitEntryImage, 1, Integer.valueOf(entriesToShowInOrbit4.get(i).getPriority()), entriesToShowInOrbit4.get(i).getStart_date(), entriesToShowInOrbit4.get(i).getFinish_date());
                    orbitEntryImage.setX((float) (coordinatesOfOrbit4.get(entriesToShowInOrbit4.get(i).getOrderings().get(type)).getX() - entityImageRadius / 2));
                    orbitEntryImage.setY((float) (coordinatesOfOrbit4.get(entriesToShowInOrbit4.get(i).getOrderings().get(type)).getY() - entityImageRadius / 2));
                    orbitEntryImage.setOnClickListener(this);
                    orbitEntryImage.setOnLongClickListener(this);
                    layoutCustomView.addView(orbitEntryImage);
                }
            } else {
                galaxyList.add(entriesToShowInOrbit4.get(i));
            }
        }


        if (galaxyList.size() > 0) {
            orbitEntryImage = new TextView(HomeActivity.this);
            orbitEntryImage.setLayoutParams(new ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
            orbitEntryImage.setTypeface(ResourcesCompat.getFont(HomeActivity.this, R.font.teko_medium));
            orbitEntryImage.setTextColor(ContextCompat.getColor(HomeActivity.this, R.color.colorBlack));
            orbitEntryImage.setPadding(10, entityImageRadius / 4, 10, 1);
            orbitEntryImage.setText("" + galaxyList.size());
            orbitEntryImage.setTextSize(10f);
            orbitEntryImage.setGravity(Gravity.CENTER_HORIZONTAL);
            orbitEntryImage.setMaxLines(1);
            orbitEntryImage.setBackgroundResource(R.drawable.galaxy_tail);
            orbitEntryImage.setX((float) (coordinatesOfOrbit4.get(noOfEntities - 1).getX() - entityImageRadius / 2));
            orbitEntryImage.setY((float) (coordinatesOfOrbit4.get(noOfEntities - 1).getY() - entityImageRadius / 2));
            orbitEntryImage.setOnClickListener(this);
            layoutCustomView.addView(orbitEntryImage);
        }*/


        TextView orbitEntryImage;
        final int noOfEntities = coordinatesOfOrbit4.size();
        int entitiesToShow = entriesToShowInOrbit4.size();
        int v;

        if (entitiesToShow > noOfEntities)
            v = 1;
        else
            v = 0;
        for (int i = 0; i < entriesToShowInOrbit4.size(); i++) {
            if (i < noOfEntities - 1 && v < noOfEntities) {
                v++;

                if (entriesToShowInOrbit4.get(i).getType() != null && entriesToShowInOrbit4.get(i).getType().equalsIgnoreCase("S")) {
                    orbitEntryImage = new TextView(HomeActivity.this);
                    orbitEntryImage.setLayoutParams(new ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                    orbitEntryImage.setTypeface(ResourcesCompat.getFont(HomeActivity.this, R.font.teko_medium));
                    orbitEntryImage.setTextColor(ContextCompat.getColor(HomeActivity.this, R.color.colorBlack));
                    orbitEntryImage.setId(Integer.parseInt(entriesToShowInOrbit4.get(i).getId()));
                    orbitEntryImage.setPadding(20, 1, 20, 1);
                    orbitEntryImage.setTag(4);
                    orbitEntryImage.setText(entriesToShowInOrbit4.get(i).getName());
                    // orbitEntryImage.setEllipsize(TextUtils.TruncateAt.END);
                    orbitEntryImage.setTextSize(11f);
                    orbitEntryImage.setGravity(Gravity.CENTER);
                    orbitEntryImage.setMaxLines(1);
                    setEntityImage(orbitEntryImage, 4, Integer.valueOf(entriesToShowInOrbit4.get(i).getPriority()), entriesToShowInOrbit4.get(i).getStart_date(), entriesToShowInOrbit4.get(i).getFinish_date());


                    orbitEntryImage.setX((float) (coordinatesOfOrbit4.get(i).getX() - entityImageRadius / 2));
                    orbitEntryImage.setY((float) (coordinatesOfOrbit4.get(i).getY() - entityImageRadius / 2));
                    orbitEntryImage.setOnClickListener(this);
                    orbitEntryImage.setOnLongClickListener(this);
                    layoutCustomView.addView(orbitEntryImage);
                } else if (entriesToShowInOrbit4.get(i).getType() != null && entriesToShowInOrbit2.get(i).getType().equalsIgnoreCase("C")) {
                    orbitEntryImage = new TextView(HomeActivity.this);
                    orbitEntryImage.setLayoutParams(new ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                    orbitEntryImage.setTypeface(ResourcesCompat.getFont(HomeActivity.this, R.font.teko_medium));
                    orbitEntryImage.setTextColor(ContextCompat.getColor(HomeActivity.this, R.color.colorBlack));
                    orbitEntryImage.setId(Integer.parseInt(entriesToShowInOrbit4.get(i).getId()));
                    orbitEntryImage.setPadding(20, entityImageRadius / 4, 20, 1);
                    orbitEntryImage.setText(entriesToShowInOrbit4.get(i).getName());
                    orbitEntryImage.setTag(4);
                    // orbitEntryImage.setEllipsize(TextUtils.TruncateAt.END);
                    orbitEntryImage.setTextSize(11f);
                    orbitEntryImage.setGravity(Gravity.CENTER_HORIZONTAL);
                    orbitEntryImage.setMaxLines(1);
                    setCometEntityImage(orbitEntryImage, 4, Integer.valueOf(entriesToShowInOrbit4.get(i).getPriority()), entriesToShowInOrbit4.get(i).getStart_date(), entriesToShowInOrbit4.get(i).getFinish_date());

                    orbitEntryImage.setX((float) (coordinatesOfOrbit4.get(i).getX() - entityImageRadius / 2));
                    orbitEntryImage.setY((float) (coordinatesOfOrbit4.get(i).getY() - entityImageRadius / 2));
                    orbitEntryImage.setOnLongClickListener(this);
                    orbitEntryImage.setOnClickListener(this);
                    layoutCustomView.addView(orbitEntryImage);
                }
            } else if (entriesToShowInOrbit4.size() > noOfEntities) {

                orbitEntryImage = new TextView(HomeActivity.this);
                orbitEntryImage.setLayoutParams(new ViewGroup.LayoutParams(entityImageRadius, entityImageRadius));
                orbitEntryImage.setTypeface(ResourcesCompat.getFont(HomeActivity.this, R.font.teko_medium));
                orbitEntryImage.setTextColor(ContextCompat.getColor(HomeActivity.this, R.color.colorBlack));
                orbitEntryImage.setPadding(10, entityImageRadius / 4, 10, 1);
                orbitEntryImage.setText(entriesToShowInOrbit4.size() - noOfEntities + "+");
                orbitEntryImage.setTextSize(10f);
                orbitEntryImage.setGravity(Gravity.CENTER_HORIZONTAL);
                orbitEntryImage.setMaxLines(1);
                orbitEntryImage.setBackgroundResource(R.drawable.galaxy_tail);
                orbitEntryImage.setX((float) (coordinatesOfOrbit4.get(i).getX() - entityImageRadius / 2));
                orbitEntryImage.setY((float) (coordinatesOfOrbit4.get(i).getY() - entityImageRadius / 2));
                orbitEntryImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupGalaxy.setVisibility(View.VISIBLE);
                        viewDim.setVisibility(View.VISIBLE);
                        ArrayList<StarResponseModel> galaxyListOrbit = new ArrayList<>();

                        for (int l = noOfEntities - 1; l < entriesToShowInOrbit4.size(); l++)
                            galaxyListOrbit.add(entriesToShowInOrbit4.get(l));

                        GalaxyStarsAdapter galaxyStarsAdapter = new GalaxyStarsAdapter(HomeActivity.this, galaxyListOrbit, HomeActivity.this);
                        recyclerViewGalaxy.setAdapter(galaxyStarsAdapter);

                    }
                });
                layoutCustomView.addView(orbitEntryImage);
                break;
            }
        }
    }

    private void setEntityImage(View orbitEntryImage, int orbit, int priority, String startDate, String finishDate) {

        Date currentDate = null, dateFinish = null, dateStart = null;
        try {
            dateStart = new SimpleDateFormat("yyyy-MM-dd").parse(startDate);
            dateFinish = new SimpleDateFormat("yyyy-MM-dd").parse(finishDate);
            currentDate = new SimpleDateFormat("yyyy-MM-dd").parse(new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));

        } catch (ParseException e) {
            e.printStackTrace();
        }

        //Red Means towards start date.

        switch (priority) {
            case 1:
                if (isDoStars) {
                    if (dateStart.after(currentDate))
                        orbitEntryImage.setBackgroundResource(R.drawable.star_storage_red_priority_1);
                    else
                        orbitEntryImage.setBackgroundResource(R.drawable.star_storage_green_priority_1);
                } else
                    orbitEntryImage.setBackgroundResource(R.drawable.star_storage_green_priority_1);
                break;
            case 2:
                if (isDoStars) {
                    if (dateStart.before(currentDate))
                        orbitEntryImage.setBackgroundResource(R.drawable.star_storage_red_priority_2);
                    else
                        orbitEntryImage.setBackgroundResource(R.drawable.star_storage_green_priority_2);
                } else
                    orbitEntryImage.setBackgroundResource(R.drawable.star_storage_green_priority_2);
                break;
            case 3:
                if (isDoStars) {
                    if (dateStart.before(currentDate))
                        orbitEntryImage.setBackgroundResource(R.drawable.star_storage_red_priority_3);
                    else
                        orbitEntryImage.setBackgroundResource(R.drawable.star_storage_green_priority_3);
                } else
                    orbitEntryImage.setBackgroundResource(R.drawable.star_storage_green_priority_3);
                break;
            case 4:
                if (isDoStars) {
                    if (dateStart.before(currentDate))
                        orbitEntryImage.setBackgroundResource(R.drawable.star_storage_red_priority_4);
                    else
                        orbitEntryImage.setBackgroundResource(R.drawable.star_storage_green_priority_4);
                } else
                    orbitEntryImage.setBackgroundResource(R.drawable.star_storage_green_priority_4);
                break;
            case 5:
                if (isDoStars) {
                    if (dateStart.before(currentDate))
                        orbitEntryImage.setBackgroundResource(R.drawable.star_5);
                    else
                        orbitEntryImage.setBackgroundResource(R.drawable.star_5);
                } else
                    orbitEntryImage.setBackgroundResource(R.drawable.star_5);
                break;
        }
    }

    private void setCometEntityImage(View orbitEntryImage, int orbit, int priority, String startDate, String finishDate) {
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
                if (isDoStars) {
                    if (dateStart.after(currentDate))
                        orbitEntryImage.setBackgroundResource(R.drawable.comet_red_priority_1);
                    else
                        orbitEntryImage.setBackgroundResource(R.drawable.comet_green_priority_1);
                } else
                    orbitEntryImage.setBackgroundResource(R.drawable.comet_green_priority_1);

                break;
            case 2:
                if (isDoStars) {
                    if (dateStart.after(currentDate))
                        orbitEntryImage.setBackgroundResource(R.drawable.comet_red_priority_2);
                    else
                        orbitEntryImage.setBackgroundResource(R.drawable.comet_green_priority_2);
                } else
                    orbitEntryImage.setBackgroundResource(R.drawable.comet_green_priority_2);
                break;
            case 3:
                if (isDoStars) {
                    if (dateStart.after(currentDate))
                        orbitEntryImage.setBackgroundResource(R.drawable.comet_red_priority_3);
                    else
                        orbitEntryImage.setBackgroundResource(R.drawable.comet_green_priority_3);
                } else
                    orbitEntryImage.setBackgroundResource(R.drawable.comet_green_priority_3);
                break;
            case 4:
                if (isDoStars) {
                    if (dateStart.after(currentDate))
                        orbitEntryImage.setBackgroundResource(R.drawable.comet_red_priority_4);
                    else
                        orbitEntryImage.setBackgroundResource(R.drawable.comet_green_priority_4);
                } else
                    orbitEntryImage.setBackgroundResource(R.drawable.comet_green_priority_4);
                break;
            case 5:
                if (isDoStars) {
                    if (dateStart.after(currentDate))
                        orbitEntryImage.setBackgroundResource(R.drawable.comet_red_priority_5);
                    else
                        orbitEntryImage.setBackgroundResource(R.drawable.comet_green_priority_5);
                } else
                    orbitEntryImage.setBackgroundResource(R.drawable.comet_green_priority_5);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sharedPreferences.getString(ConstantUtils.USER_PROFILE, "") != null && !sharedPreferences.getString(ConstantUtils.USER_PROFILE, "").isEmpty()) {
            Picasso.with(HomeActivity.this).load(sharedPreferences.getString(ConstantUtils.USER_PROFILE, "")).placeholder(R.drawable.profile_placeholder).into(imgProfile);
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        } else {
            this.doubleBackToExitPressedOnce = true;
            AndroidUtils.showToast(HomeActivity.this, getResources().getString(R.string.pls_click_back_again));
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);

        }
    }

    @Override
    public void onTaskClick(View view, int position, String source) {
        if (source.equals("archive"))
            quickActionArchive.show(view);
        else
            quickActionUnLaunch.show(view);
        this.starPosition = position;
    }


    @Override
    public boolean onLongClick(View v) {
        ClipData data = ClipData.newPlainText("", "");
        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                v);
        v.startDrag(data, shadowBuilder, v, 0);

        return true;
    }


    private void callDeleteStarApi(final String source, String id, String type, String finishTime, String finishDate/*, ArrayList<Integer> ordering*/) {
        DeleteTaskReqModel deleteTaskReqModel = new DeleteTaskReqModel(id, type);
        progressDialog.show();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetMessageModel> deleteStarResponseCall = apiService.deleteStar(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), deleteTaskReqModel);

        deleteStarResponseCall.enqueue(new Callback<GetMessageModel>() {
            @Override
            public void onResponse(Call<GetMessageModel> call, Response<GetMessageModel> response) {
                progressDialog.dismiss();
                if (response.code() == 200 && response.body() != null) {
                    if (response.body().getMessage() != null)
                        AndroidUtils.showToast(HomeActivity.this, response.body().getMessage());

                    if (source.equals("unLaunch")) {
                        unLaunchedStarsList.remove(starPosition);
                        unLaunchedStarsAdapter.notifyDataSetChanged();
                    } else if (source.equals("archive")) {
                        archivedStarsList.remove(starPosition);
                        archivedStarsAdapter.notifyDataSetChanged();
                    } else {
                        if (ConnectivityController.isNetworkAvailable(HomeActivity.this))
                            callGetLaunchedStarsApi(false);
                        else
                            AndroidUtils.showToast(HomeActivity.this, getResources().getString(R.string.no_internet));
                        /*launchedStarsList.remove(starPosition);

                        if (layoutHour.getBackground() != null)
                            layoutHour.performClick();
                        else if (layoutDay.getBackground() != null)
                            layoutDay.performClick();
                        else if (layoutWeek.getBackground() != null)
                            layoutWeek.performClick();
                        else if (layoutMonth.getBackground() != null)
                            layoutMonth.performClick();
                        else layoutYear.performClick();*/

                    }

                } else if (response.code() == 500) {
                    AndroidUtils.showToast(HomeActivity.this, getResources().getString(R.string.some_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<GetMessageModel> call, Throwable t) {
                progressDialog.dismiss();
                AndroidUtils.showToast(HomeActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });
    }

    @Override
    public void onItemClick(ActionItem item) {
        switch (item.getActionId()) {
            case 1:
                //if (popupUnLaunched.getVisibility() == View.VISIBLE) {
                Intent activityIntent = new Intent(HomeActivity.this, AddTaskActivity.class);
                activityIntent.putExtra(ConstantUtils.PURPOSE, "update");
                activityIntent.putExtra(ConstantUtils.SOURCE, "star");
                activityIntent.putExtra(ConstantUtils.TASK_POSITION, starPosition);
                activityIntent.putExtra(ConstantUtils.STAR_RESPONSE_MODEL, unLaunchedStarsList.get(starPosition));
                startActivityForResult(activityIntent, EDIT_UNLANUCH_STAR_REQUEST_CODE);
                overridePendingTransition(R.anim.slide_in_up, R.anim.stay);
                /*} else {
                    Intent activityIntent = new Intent(HomeActivity.this, AddTaskActivity.class);
                    activityIntent.putExtra(ConstantUtils.PURPOSE, "update");
                    activityIntent.putExtra(ConstantUtils.SOURCE, "star");
                    activityIntent.putExtra(ConstantUtils.STAR_RESPONSE_MODEL, archivedStarsList.get(starPosition));
                    startActivity(activityIntent);
                    overridePendingTransition(R.anim.slide_in_up, R.anim.stay);
                }*/


                break;
            case 2:
                if (ConnectivityController.isNetworkAvailable(HomeActivity.this)) {
                    if (popupUnLaunched.getVisibility() == View.VISIBLE) {

                        callDeleteStarApi("unLaunch", unLaunchedStarsList.get(starPosition).getId(), "S", unLaunchedStarsList.get(starPosition).getFinish_time(), unLaunchedStarsList.get(starPosition).getFinish_date());
                    } else {
                        callDeleteStarApi("archive", archivedStarsList.get(starPosition).getId(), "S", archivedStarsList.get(starPosition).getFinish_time(), archivedStarsList.get(starPosition).getFinish_date());
                    }

                } else
                    AndroidUtils.showToast(HomeActivity.this, getResources().getString(R.string.no_internet));
                break;
            case 3:
                Intent previewIntent = new Intent(HomeActivity.this, AddTaskActivity.class);
                previewIntent.putExtra(ConstantUtils.PURPOSE, "preview");
                previewIntent.putExtra(ConstantUtils.SOURCE, "star");
                previewIntent.putExtra(ConstantUtils.STAR_RESPONSE_MODEL, archivedStarsList.get(starPosition));
                startActivity(previewIntent);
                overridePendingTransition(R.anim.slide_in_up, R.anim.stay);
                break;
        }
    }

    @Override
    public void onEditTaskClick(int position, String source) {
        Intent activityIntent = new Intent(HomeActivity.this, AddTaskActivity.class);
        activityIntent.putExtra(ConstantUtils.PURPOSE, "update");
        activityIntent.putExtra(ConstantUtils.TASK_POSITION, position);
        activityIntent.putExtra(ConstantUtils.STAR_RESPONSE_MODEL, launchedStarsList.get(position));


        if (launchedStarsList.get(position).getType().equalsIgnoreCase("S")) {
            activityIntent.putExtra(ConstantUtils.SOURCE, "star");
        } else {
            activityIntent.putExtra(ConstantUtils.SOURCE, "comet");
        }

        startActivityForResult(activityIntent, EDIT_LAUNCH_STAR_REQUEST_CODE);
        overridePendingTransition(R.anim.slide_in_up, R.anim.stay);
    }

    @Override
    public void onDeleteTaskClick(int position, String source) {
        if (infoPopUpFragment != null)
            infoPopUpFragment.dismiss();

        popupGalaxy.setVisibility(View.GONE);
        viewDim.setVisibility(View.GONE);

        this.starPosition = position;
        if (ConnectivityController.isNetworkAvailable(HomeActivity.this))
            callDeleteStarApi("launch", launchedStarsList.get(position).getId(), launchedStarsList.get(position).getType(), launchedStarsList.get(position).getFinish_time(), launchedStarsList.get(position).getFinish_date());

        else
            AndroidUtils.showToast(HomeActivity.this, getResources().getString(R.string.no_internet));
    }

    @Override
    public void onStorageTaskClick(int position) {
        Intent intent = new Intent(HomeActivity.this, StarStorageActivity.class);
        intent.putExtra(ConstantUtils.STAR_ID, launchedStarsList.get(position).getId());
        startActivity(intent);
    }

    @Override
    public void onMarkDoneClick(int position, String source) {
        if (infoPopUpFragment != null)
            infoPopUpFragment.dismiss();

        viewDim.setVisibility(View.GONE);
        popupGalaxy.setVisibility(View.GONE);
        if (ConnectivityController.isNetworkAvailable(HomeActivity.this)) {
            callMarkTaskDoneApi(position);


        } else
            AndroidUtils.showToast(HomeActivity.this, getResources().getString(R.string.no_internet));
    }

    @Override
    public void onDetailsClick(int position, String source) {
        Intent intent = new Intent(HomeActivity.this, DetailPageActivity.class);
        intent.putExtra(ConstantUtils.ENTITY_ID, launchedStarsList.get(position).getId());
        intent.putExtra(ConstantUtils.ENTITY_TYPE, "S");
        startActivity(intent);
    }


    private void callMarkTaskDoneApi(final int position) {
        progressDialog.show();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetMessageModel> markTaskDoneReposneCall = null;
        markTaskDoneReposneCall = apiService.markTaskDone(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), launchedStarsList.get(position).getId(), launchedStarsList.get(position).getType());
        markTaskDoneReposneCall.enqueue(new Callback<GetMessageModel>() {
            @Override
            public void onResponse(Call<GetMessageModel> call, Response<GetMessageModel> response) {
                progressDialog.dismiss();
                if (response.code() == 200 && response.body() != null) {
                    if (response.body().getMessage() != null)
                        AndroidUtils.showToast(HomeActivity.this, response.body().getMessage());

                    launchedStarsList.remove(position);


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
                    AndroidUtils.showToast(HomeActivity.this, getResources().getString(R.string.some_went_wrong));

            }

            @Override
            public void onFailure(Call<GetMessageModel> call, Throwable t) {
                progressDialog.dismiss();
                AndroidUtils.showToast(HomeActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        popupGalaxy.setVisibility(View.GONE);
        viewDim.setVisibility(View.GONE);
        switch (requestCode) {
            case REFRESH_PAGE:
                if (resultCode == RESULT_OK) {
                    if (ConnectivityController.isNetworkAvailable(HomeActivity.this))
                        callGetLaunchedStarsApi(false);
                    else
                        AndroidUtils.showToast(HomeActivity.this, getResources().getString(R.string.no_internet));
                }
                break;
            case EDIT_LAUNCH_STAR_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    if (infoPopUpFragment != null)
                        infoPopUpFragment.dismiss();

                    if (ConnectivityController.isNetworkAvailable(HomeActivity.this))
                        callGetLaunchedStarsApi(false);
                    else
                        AndroidUtils.showToast(HomeActivity.this, getResources().getString(R.string.no_internet));

                }
                break;

            case EDIT_UNLANUCH_STAR_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    if (infoPopUpFragment != null)
                        infoPopUpFragment.dismiss();
                    StarResponseModel starResponseModel;
                    if (data != null && data.getParcelableExtra(ConstantUtils.STAR_RESPONSE_MODEL) != null) {
                        starResponseModel = data.getParcelableExtra(ConstantUtils.STAR_RESPONSE_MODEL);

                        if (starResponseModel != null && starResponseModel.getStart_date() != null && starResponseModel.getFinish_date() != null) {
                            unLaunchedStarsList.remove(data.getIntExtra(ConstantUtils.TASK_POSITION, 0));
                            unLaunchedStarsAdapter.notifyDataSetChanged();
                            launchedStarsList.add(starResponseModel);

                            if (layoutHour.getBackground() != null)
                                layoutHour.performClick();
                            else if (layoutDay.getBackground() != null)
                                layoutDay.performClick();
                            else if (layoutWeek.getBackground() != null)
                                layoutWeek.performClick();
                            else if (layoutMonth.getBackground() != null)
                                layoutMonth.performClick();
                            else layoutYear.performClick();
                        } else if (starResponseModel != null) {
                            unLaunchedStarsList.remove(data.getIntExtra(ConstantUtils.TASK_POSITION, 0));
                            unLaunchedStarsList.add(data.getIntExtra(ConstantUtils.TASK_POSITION, 0), starResponseModel);
                            unLaunchedStarsAdapter.notifyDataSetChanged();
                        }

                    } else {
                        if (ConnectivityController.isNetworkAvailable(HomeActivity.this)) {
                            callGetUnLaunchedStarsApi(false);
                            callGetLaunchedStarsApi(false);

                        } else
                            AndroidUtils.showToast(HomeActivity.this, getResources().getString(R.string.no_internet));
                    }
                }
                break;
        }


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
//                Log.e("DragStared", "onDrag: " + event.getX());
//                Log.e("DragStared", "onDrag: " + event.getY());
                return true;

            case DragEvent.ACTION_DRAG_EXITED:
                //  v.setBackgroundColor(Color.TRANSPARENT);
                return true;

            case DragEvent.ACTION_DROP:

           /*     Log.e("Drop", "onDrag: " + event.getX());
                Log.e("Drop", "onDrag: " + event.getY());*/
                double dist = entityImageRadius;
                int NewOrbitNo = -1;
                int orderNo = -1;
                String parameter = "";
                for (int i = 0; i < coordinatesOfOrbit1.size(); i++) {
                    double pointX = coordinatesOfOrbit1.get(i).getX() - event.getX();
                    double pointY = coordinatesOfOrbit1.get(i).getY() - event.getY();
                    double distance = Math.sqrt(pointX * pointX + pointY * pointY);

                    if (distance < dist && distance > 0) {
                        dist = distance;
                        NewOrbitNo = 1;
                        orderNo = i;

                    }

                }

                for (int i = 0; i < coordinatesOfOrbit2.size(); i++) {
                    double pointX = coordinatesOfOrbit2.get(i).getX() - event.getX();
                    double pointY = coordinatesOfOrbit2.get(i).getY() - event.getY();
                    double distance = Math.sqrt(pointX * pointX + pointY * pointY);

                    if (distance < dist && distance > 0) {
                        dist = distance;
                        NewOrbitNo = 2;
                        orderNo = i;
                    }

                }

                for (int i = 0; i < coordinatesOfOrbit3.size(); i++) {
                    double pointX = coordinatesOfOrbit3.get(i).getX() - event.getX();
                    double pointY = coordinatesOfOrbit3.get(i).getY() - event.getY();
                    double distance = Math.sqrt(pointX * pointX + pointY * pointY);

                    if (distance < dist && distance > 0) {
                        dist = distance;
                        NewOrbitNo = 3;
                        orderNo = i;
                    }

                }

                for (int i = 0; i < coordinatesOfOrbit4.size(); i++) {
                    double pointX = coordinatesOfOrbit4.get(i).getX() - event.getX();
                    double pointY = coordinatesOfOrbit4.get(i).getY() - event.getY();
                    double distance = Math.sqrt(pointX * pointX + pointY * pointY);

                    if (distance < dist && distance > 0) {
                        dist = distance;
                        NewOrbitNo = 4;
                        orderNo = i;
                    }

                }


                if (layoutHour.getBackground() != null)
                    parameter = "H";
                else if (layoutDay.getBackground() != null)
                    parameter = "D";
                else if (layoutWeek.getBackground() != null)
                    parameter = "W";
                else if (layoutMonth.getBackground() != null)
                    parameter = "M";
                else parameter = "Y";

                Log.e("Drop", " index " + NewOrbitNo);
                Calendar calendar = Calendar.getInstance();
                if (NewOrbitNo != -1) {
                    View view = (View) event.getLocalState();
                    ViewGroup owner = (ViewGroup) view.getParent();
                    owner.removeView(view);
                    //  view.setX(event.getX() - (view.getWidth() / 2));
                    // view.setY(event.getY() - (view.getWidth() / 2));


                    switch (NewOrbitNo) {
                        case 1:
                            view.setX((float) coordinatesOfOrbit1.get(orderNo).getX() - entityImageRadius / 2);
                            view.setY((float) coordinatesOfOrbit1.get(orderNo).getY() - entityImageRadius / 2);
                            break;
                        case 2:
                            view.setX((float) coordinatesOfOrbit2.get(orderNo).getX() - entityImageRadius / 2);
                            view.setY((float) coordinatesOfOrbit2.get(orderNo).getY() - entityImageRadius / 2);
                            break;
                        case 3:
                            view.setX((float) coordinatesOfOrbit3.get(orderNo).getX() - entityImageRadius / 2);
                            view.setY((float) coordinatesOfOrbit3.get(orderNo).getY() - entityImageRadius / 2);
                            break;
                        case 4:
                            view.setX((float) coordinatesOfOrbit4.get(orderNo).getX() - entityImageRadius / 2);
                            view.setY((float) coordinatesOfOrbit4.get(orderNo).getY() - entityImageRadius / 2);
                            break;
                    }
                    layoutCustomView.addView(view);
                    view.setVisibility(View.VISIBLE);

                    int PrevOrbitNo = (int) view.getTag();

                    for (int i = 0; i < launchedStarsList.size(); i++) {
                        if (launchedStarsList.get(i).getType().equalsIgnoreCase("S") && view.getId() == Integer.parseInt(launchedStarsList.get(i).getId())) {
                            try {
                                if (launchedStarsList.get(i).getFinish_time() != null)
                                    calendar.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(launchedStarsList.get(i).getFinish_date() + " " + launchedStarsList.get(i).getFinish_time()));
                                else
                                    calendar.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(launchedStarsList.get(i).getFinish_date()));

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            StarResponseModel starResponseModel = launchedStarsList.get(i);
                            StarRequestModel starRequestModel = new StarRequestModel();
                            starRequestModel.setStar_id(starResponseModel.getId());
                            starRequestModel.setName(starResponseModel.getName());
                            starRequestModel.setDescription(starResponseModel.getDescription());
                            starRequestModel.setStart_date(starResponseModel.getStart_date());
                            starRequestModel.setStart_time(starResponseModel.getStart_time());
                            starRequestModel.setPriority(starResponseModel.getPriority());
                            starRequestModel.setAlarm_time(starResponseModel.getAlarm_time());
                            starRequestModel.setAlarm_date(starResponseModel.getAlarm_date());
                            starRequestModel.setNotes(starResponseModel.getNotes());

                            int diff;
                            if (isDoStars)
                                diff = NewOrbitNo - PrevOrbitNo;
                            else diff = PrevOrbitNo - NewOrbitNo;

                            if (layoutHour.getBackground() != null) {
                                calendar.add(Calendar.HOUR, diff * 3);
                                starRequestModel.setFinish_date(starResponseModel.getFinish_date());
                                starRequestModel.setFinish_time(new SimpleDateFormat("HH:mm").format(calendar.getTime()));
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


                                starRequestModel.setFinish_date(new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()));
                                starRequestModel.setFinish_time(starResponseModel.getFinish_time());
                            }


                            if (ConnectivityController.isNetworkAvailable(HomeActivity.this)) {
                                callUpdateStarApi(starRequestModel, String.valueOf(orderNo), parameter);
                            } else
                                AndroidUtils.showToast(HomeActivity.this, getResources().getString(R.string.no_internet));

                        } else if (view.getId() == Integer.parseInt(launchedStarsList.get(i).getId())) {

                            try {
                                if (launchedStarsList.get(i).getFinish_time() != null)
                                    calendar.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(launchedStarsList.get(i).getFinish_date() + " " + launchedStarsList.get(i).getFinish_time()));
                                else
                                    calendar.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(launchedStarsList.get(i).getFinish_date()));

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            int diff;
                            if (isDoStars)
                                diff = NewOrbitNo - PrevOrbitNo;
                            else diff = PrevOrbitNo - NewOrbitNo;

                            if (layoutHour.getBackground() != null) {
                                calendar.add(Calendar.HOUR, diff * 3);
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
                            }
                            StarResponseModel starResponseModel = launchedStarsList.get(i);
                            CometRequestModel cometRequestModel = new CometRequestModel();
                            cometRequestModel.setComet_id(starResponseModel.getId());
                            cometRequestModel.setName(starResponseModel.getName());
                            cometRequestModel.setDescription(starResponseModel.getDescription());
                            cometRequestModel.setStart_date(starResponseModel.getStart_date());
                            cometRequestModel.setStart_time(starResponseModel.getStart_time());
                            cometRequestModel.setFinish_date(new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()));
                            cometRequestModel.setFinish_time(starResponseModel.getFinish_time());
                            cometRequestModel.setPriority(starResponseModel.getPriority());
                            cometRequestModel.setAlarm_time(starResponseModel.getAlarm_time());
                            cometRequestModel.setAlarm_date(starResponseModel.getAlarm_date());
                            cometRequestModel.setNotes(starResponseModel.getNotes());


                            if (ConnectivityController.isNetworkAvailable(HomeActivity.this))
                                callUpdateCometApi(cometRequestModel);
                            else
                                AndroidUtils.showToast(HomeActivity.this, getResources().getString(R.string.no_internet));
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

        if (ConnectivityController.isNetworkAvailable(HomeActivity.this))
            callGetLaunchedStarsApi(false);
        else
            AndroidUtils.showToast(HomeActivity.this, getResources().getString(R.string.no_internet));

    }

    public void callUpdateStarApi(final StarRequestModel starRequestModel, final String displayOrder, final String parameter) {
        progressDialog.show();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<StarResponseModel> starResponseModelCall = apiService.updateStar(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), starRequestModel);
        starResponseModelCall.enqueue(new Callback<StarResponseModel>() {
            @Override
            public void onResponse(Call<StarResponseModel> call, Response<StarResponseModel> response) {
                progressDialog.dismiss();
                if (response.code() == 200 && response.body() != null) {
                    //  AndroidUtils.showToast(HomeActivity.this, "Star has been updated successfully");
                    if (ConnectivityController.isNetworkAvailable(HomeActivity.this))
                        callCreateOrderingApi(starRequestModel.getStar_id(), "S", displayOrder, parameter);
                    else
                        AndroidUtils.showToast(HomeActivity.this, getResources().getString(R.string.no_internet));
                } else if (response.code() == 500) {
                    AndroidUtils.showToast(HomeActivity.this, getResources().getString(R.string.some_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<StarResponseModel> call, Throwable t) {
                progressDialog.dismiss();
                AndroidUtils.showToast(HomeActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });
    }

    private void callUpdateCometApi(CometRequestModel cometRequestModel) {
        progressDialog.show();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<CometResponseModel> updateCometResponseCall = apiService.updateComet(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), cometRequestModel);
        updateCometResponseCall.enqueue(new Callback<CometResponseModel>() {
            @Override
            public void onResponse(Call<CometResponseModel> call, Response<CometResponseModel> response) {
                progressDialog.dismiss();
                if (response.code() == 200 && response.body() != null) {
                    AndroidUtils.showToast(HomeActivity.this, "Comet has been updated successfully");
                    if (ConnectivityController.isNetworkAvailable(HomeActivity.this))
                        callGetLaunchedStarsApi(false);
                    else
                        AndroidUtils.showToast(HomeActivity.this, getResources().getString(R.string.no_internet));
                } else if (response.code() == 500) {
                    AndroidUtils.showToast(HomeActivity.this, getResources().getString(R.string.some_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<CometResponseModel> call, Throwable t) {
                progressDialog.dismiss();
                AndroidUtils.showToast(HomeActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });
    }


    private void callCreateOrderingApi(final String objectId, final String objectType, String displayOrder, String parameter) {
        progressDialog.show();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<JsonElement> createOrderResponseCall = apiService.createOrder(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), objectId, objectType, displayOrder, parameter);
        createOrderResponseCall.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                progressDialog.dismiss();
                if (response.code() == 200 && response.body() != null) {
                    if (objectType.equalsIgnoreCase("S"))
                        AndroidUtils.showToast(HomeActivity.this, "Star has been updated successfully");
                    else
                        AndroidUtils.showToast(HomeActivity.this, "Comet has been updated successfully");
                    if (ConnectivityController.isNetworkAvailable(HomeActivity.this))
                        callGetLaunchedStarsApi(false);
                    else
                        AndroidUtils.showToast(HomeActivity.this, getResources().getString(R.string.no_internet));
                } else if (response.code() == 500) {
                    AndroidUtils.showToast(HomeActivity.this, getResources().getString(R.string.some_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                progressDialog.dismiss();
                AndroidUtils.showToast(HomeActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });
    }

    @Override
    public void onGalaxyItemClick(final int position) {
        long now = System.currentTimeMillis();
        if (now - firstClickTime < doubleClickTimeout) {
            handler.removeCallbacksAndMessages(null);
            firstClickTime = 0L;
            onDoubleClickGalaxy(position);
        } else {
            firstClickTime = now;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    onSingleClickGalaxy(position);
                    firstClickTime = 0L;
                }
            }, doubleClickTimeout);
        }
    }

    private void onSingleClickGalaxy(int position) {
        for (int i = 0; i < launchedStarsList.size(); i++) {
            if (launchedStarsList.get(i).getId() != null && position == Integer.parseInt(launchedStarsList.get(i).getId())) {


                Intent intent = new Intent(HomeActivity.this, PlanetPageActivity.class);
                intent.putExtra(ConstantUtils.TASK_POSITION, i);
                intent.putExtra(ConstantUtils.STAR_RESPONSE_MODEL, launchedStarsList.get(i));
                startActivityForResult(intent, EDIT_LAUNCH_STAR_REQUEST_CODE);
                popupGalaxy.setVisibility(View.GONE);
                viewDim.setVisibility(View.GONE);

            }
        }
    }

    private void onDoubleClickGalaxy(int position) {
        infoPopUpFragment = new InfoPopUpFragment();
        Bundle bundle = null;
        for (int i = 0; i < launchedStarsList.size(); i++) {
            if (launchedStarsList.get(i).getId() != null && position == Integer.parseInt(launchedStarsList.get(i).getId())) {
                if (launchedStarsList.get(i).getType().equalsIgnoreCase("S")) {
                    bundle = new Bundle();
                    bundle.putParcelable(ConstantUtils.STAR_RESPONSE_MODEL, launchedStarsList.get(i));
                    bundle.putInt(ConstantUtils.TASK_POSITION, i);
                } else {
                    bundle = new Bundle();
                    bundle.putParcelable(ConstantUtils.STAR_RESPONSE_MODEL, launchedStarsList.get(i));
                    bundle.putInt(ConstantUtils.TASK_POSITION, i);
                }

            }
        }
        if (bundle != null && bundle.size() > 0) {
            infoPopUpFragment.setArguments(bundle);
            infoPopUpFragment.setInfoPopUpListener(this);
            infoPopUpFragment.show(getSupportFragmentManager(), infoPopUpFragment.getTag());
            infoPopUpFragment.setCancelable(false);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int position = -1, oldPosition = -1;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                float deltaX = x2 - x1;

                if (Math.abs(deltaX) < 130) {
                    position = calenderView.pointToPosition((int) event.getX(), (int) event.getY());
                    if (position != -1) {

                        /*EachDate eachDate = gridCellAdapter.getGridItem(oldPosition);
                        if (null != eachDate) {
                            if(!eachDate.m_cColor.equalsIgnoreCase("BLUE")) {
                                eachDate.m_cColor = "WHITE";
                                gridCellAdapter.setGridItem(oldPosition, eachDate);
                            }
                        }*/
                        for (int i = 0; i < gridCellAdapter.getGridSize(); i++) {
                            EachDate lItem = gridCellAdapter.getGridItem(i);
                            if (null != lItem && !lItem.m_cColor.equalsIgnoreCase("BLUE")) {
                                lItem.m_cColor = "WHITE";
                                gridCellAdapter.setGridItem(i, lItem);
                            }
                        }
                        EachDate lItem = gridCellAdapter.getGridItem(position);
                        if (null != lItem) {
                            lItem.m_cColor = "RED";
                            gridCellAdapter.setGridItem(position, lItem);
                            gridCellAdapter.notifyDataSetChanged();
                            oldPosition = position;
                        }
                        Log.e("Date", "" + gridCellAdapter.getGridItem(position).m_cDate);
                        AndroidUtils.showToast(HomeActivity.this, "Selected Date is: " + gridCellAdapter.getGridItem(position).m_cDate);

                        try {
                            currentDateWithTime = new SimpleDateFormat("dd-MMM-yyyy").parse(gridCellAdapter.getGridItem(position).m_cDate);
                            currentDateWithoutTime = currentDateWithTime;
                            if (layoutHour.getBackground() != null)
                                layoutHour.performClick();
                            else if (layoutDay.getBackground() != null)
                                layoutDay.performClick();
                            else if (layoutWeek.getBackground() != null)
                                layoutWeek.performClick();
                            else if (layoutMonth.getBackground() != null)
                                layoutMonth.performClick();
                            else layoutYear.performClick();

                        /*    if(popupWindow!=null)
                                popupWindow.dismiss();*/


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
                break;
        }

        return true;
    }
}
