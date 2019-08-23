package com.android.yuniapp.activity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.android.yuniapp.R;
import com.android.yuniapp.adapter.TaskDocumentAdapter;
import com.android.yuniapp.fragment.DocumentPickerFragment;
import com.android.yuniapp.listener.DocumentClickListener;
import com.android.yuniapp.listener.RecyclerItemClickListener;
import com.android.yuniapp.model.CometRequestModel;
import com.android.yuniapp.model.CometResponseModel;
import com.android.yuniapp.model.DocumentModel;
import com.android.yuniapp.model.DocumentRequestModel;
import com.android.yuniapp.model.DocumentResponseModel;
import com.android.yuniapp.model.GetDoucmentResponse;
import com.android.yuniapp.model.GetMessageModel;
import com.android.yuniapp.model.MemberRequestModel;
import com.android.yuniapp.model.MembersModel;
import com.android.yuniapp.model.MembersResponseModel;
import com.android.yuniapp.model.MoonRequestModel;
import com.android.yuniapp.model.MoonResponseModel;
import com.android.yuniapp.model.PlanetRequestModel;
import com.android.yuniapp.model.PlanetResponseModel;
import com.android.yuniapp.model.SatelliteRequestModel;
import com.android.yuniapp.model.SatelliteResponseModel;
import com.android.yuniapp.model.StarRequestModel;
import com.android.yuniapp.model.StarResponseModel;
import com.android.yuniapp.rest.ApiClient;
import com.android.yuniapp.rest.ApiInterface;
import com.android.yuniapp.utils.AndroidUtils;
import com.android.yuniapp.utils.AppUtils;
import com.android.yuniapp.utils.ConnectivityController;
import com.android.yuniapp.utils.ConstantUtils;
import com.google.gson.Gson;
import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.vincent.filepicker.Constant;
import com.vincent.filepicker.activity.NormalFilePickActivity;
import com.vincent.filepicker.filter.entity.NormalFile;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.vincent.filepicker.activity.BaseActivity.IS_NEED_FOLDER_LIST;

public class AddTaskActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener, DocumentClickListener {
    private ImageView imgAddTask, imgAddPriority, imgLessPriority;
    private String accessNotes = "", source = "", formattedStartDate = "",formattedStartTime="", formattedFinishDate = "",formattedFinishTime="", formattedAlarmDate = "", formattedAlarmTime = "";
    private TextView txtAccessNotes;
    private EditText edtName, edtDesc, edtStartDate,edtStartTime, edtFinishDate,edtFinishTime, edtAlarmDate, edtAlarmTime, edtAddMember;
    private Dialog progressDialog;
    private Date startDateObject,startTimeObject, finishDateObject,finishTimeObject, alarmDateObject, alarmTimeObject;
    private RangeSeekBar prioritySeekBar;
    private int seekbarCounter = 1;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private TextView txtSave, txtAddDocument;
    private RecyclerView recyclerViewDocuments;

    private StarResponseModel starResponseModel;
    private PlanetResponseModel planetResponseModel;
    private MoonResponseModel moonResponseModel;
    private SatelliteResponseModel satelliteResponseModel;

    private String starId = "", planetId = "", moonId = "", satelliteId = "", cometId = "";
    private final int REQUEST_WRITE_STORAGE = 1001;
    private int taskPosition;


    private ArrayList<MemberRequestModel> memberRequestList=new ArrayList<>();
    private ArrayList<MembersResponseModel> memberResponseList = new ArrayList<>();

    private MultipartBody.Part partBodyDocument;

    private ArrayList<DocumentModel> documentModels = new ArrayList<>();
    private ArrayList<DocumentResponseModel> documentList = new ArrayList<>();
    private TaskDocumentAdapter taskDocumentAdapter;
    private static final int MEMBDER_RESULT_CODE = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);


        AppUtils.setFullScreen(this);
        sharedPreferences = getSharedPreferences(ConstantUtils.APP_PREF, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();


        progressDialog = new Dialog(this);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setContentView(R.layout.fragment_loader);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);


        initViews();
        setOnClick();
        setOnTouch();
        recyclerViewDocuments.setLayoutManager(new LinearLayoutManager(this));
        taskDocumentAdapter = new TaskDocumentAdapter(this, documentList,this);
        recyclerViewDocuments.setAdapter(taskDocumentAdapter);

        prioritySeekBar.setIndicatorTextDecimalFormat("0");

        source = getIntent().getStringExtra(ConstantUtils.SOURCE);


        if (getIntent() != null) {
            if (getIntent().getParcelableExtra(ConstantUtils.STAR_RESPONSE_MODEL) != null)
                starResponseModel = getIntent().getParcelableExtra(ConstantUtils.STAR_RESPONSE_MODEL);

            if (getIntent().getParcelableExtra(ConstantUtils.PLANET_RESPONSE_MODEL) != null)
                planetResponseModel = getIntent().getParcelableExtra(ConstantUtils.PLANET_RESPONSE_MODEL);

            if (getIntent().getParcelableExtra(ConstantUtils.MOON_RESPONSE_MODEL) != null)
                moonResponseModel = getIntent().getParcelableExtra(ConstantUtils.MOON_RESPONSE_MODEL);

            if (getIntent().getParcelableExtra(ConstantUtils.SATELLITE_RESPONSE_MODEL) != null)
                satelliteResponseModel = getIntent().getParcelableExtra(ConstantUtils.SATELLITE_RESPONSE_MODEL);


            if (getIntent().getStringExtra(ConstantUtils.STAR_ID) != null)
                starId = getIntent().getStringExtra(ConstantUtils.STAR_ID);

            if (getIntent().getStringExtra(ConstantUtils.PLANET_ID) != null)
                planetId = getIntent().getStringExtra(ConstantUtils.PLANET_ID);

            if (getIntent().getStringExtra(ConstantUtils.MOON_ID) != null)
                moonId = getIntent().getStringExtra(ConstantUtils.MOON_ID);

            if (getIntent().getStringExtra(ConstantUtils.SATELLITE_ID) != null)
                satelliteId = getIntent().getStringExtra(ConstantUtils.SATELLITE_ID);

        }

        if (source.equals("star")) {
            edtName.setHint("Star Name");
            imgAddTask.setImageResource(R.drawable.add_planet);

            if (getIntent().getStringExtra(ConstantUtils.PURPOSE) != null && getIntent().getStringExtra(ConstantUtils.PURPOSE).equals("update")) {
                imgAddTask.setVisibility(View.GONE);

                setDataForStar();
                taskPosition = getIntent().getIntExtra(ConstantUtils.TASK_POSITION, -1);
            } else if (getIntent().getStringExtra(ConstantUtils.PURPOSE) != null && getIntent().getStringExtra(ConstantUtils.PURPOSE).equals("preview")) {
                disableAllFields();
                imgAddTask.setVisibility(View.GONE);
                txtAddDocument.setVisibility(View.INVISIBLE);
                setDataForStar();
            } else {
                imgAddTask.setVisibility(View.VISIBLE);
            }
        } else if (source.equals("planet")) {
            edtName.setHint("Planet Name");
            imgAddTask.setImageResource(R.drawable.add_moon);

            if (getIntent().getStringExtra(ConstantUtils.PURPOSE) != null && getIntent().getStringExtra(ConstantUtils.PURPOSE).equals("update")) {
                imgAddTask.setVisibility(View.GONE);
                setDataForPlanet();
                taskPosition = getIntent().getIntExtra(ConstantUtils.TASK_POSITION, -1);
            } else if (getIntent().getStringExtra(ConstantUtils.PURPOSE) != null && getIntent().getStringExtra(ConstantUtils.PURPOSE).equals("preview")) {
                disableAllFields();
                txtAddDocument.setVisibility(View.INVISIBLE);
                imgAddTask.setVisibility(View.GONE);
                setDataForPlanet();
            } else {
                imgAddTask.setVisibility(View.VISIBLE);
            }
        } else if (source.equals("moon")) {
            edtName.setHint("Moon Name");
            imgAddTask.setImageResource(R.drawable.add_satellite);

            if (getIntent().getStringExtra(ConstantUtils.PURPOSE) != null && getIntent().getStringExtra(ConstantUtils.PURPOSE).equals("update")) {
                imgAddTask.setVisibility(View.GONE);
                setDataForMoon();
                taskPosition = getIntent().getIntExtra(ConstantUtils.TASK_POSITION, -1);
            } else if (getIntent().getStringExtra(ConstantUtils.PURPOSE) != null && getIntent().getStringExtra(ConstantUtils.PURPOSE).equals("preview")) {
                disableAllFields();
                txtAddDocument.setVisibility(View.INVISIBLE);
                imgAddTask.setVisibility(View.GONE);
                setDataForMoon();
            } else {
                imgAddTask.setVisibility(View.VISIBLE);
            }
        } else if (source.equals("satellite")) {
            edtName.setHint("Satellite Name");
            imgAddTask.setVisibility(View.GONE);

            if (getIntent().getStringExtra(ConstantUtils.PURPOSE) != null && getIntent().getStringExtra(ConstantUtils.PURPOSE).equals("update")) {
                imgAddTask.setVisibility(View.GONE);
                setDataForSatellite();
                taskPosition = getIntent().getIntExtra(ConstantUtils.TASK_POSITION, -1);
            } else if (getIntent().getStringExtra(ConstantUtils.PURPOSE) != null && getIntent().getStringExtra(ConstantUtils.PURPOSE).equals("preview")) {
                disableAllFields();
                txtAddDocument.setVisibility(View.INVISIBLE);
                imgAddTask.setVisibility(View.GONE);
                setDataForSatellite();

            } else {
                imgAddTask.setVisibility(View.VISIBLE);
            }
        } else if (source.equals("comet")) {
            edtName.setHint("Comet Name");
            imgAddTask.setVisibility(View.GONE);

            if (getIntent().getStringExtra(ConstantUtils.PURPOSE) != null && getIntent().getStringExtra(ConstantUtils.PURPOSE).equals("update")) {
                imgAddTask.setVisibility(View.GONE);
                setDataForStar();
                taskPosition = getIntent().getIntExtra(ConstantUtils.TASK_POSITION, -1);
            } else if (getIntent().getStringExtra(ConstantUtils.PURPOSE) != null && getIntent().getStringExtra(ConstantUtils.PURPOSE).equals("preview")) {
                disableAllFields();
                txtAddDocument.setVisibility(View.INVISIBLE);
                imgAddTask.setVisibility(View.GONE);
                setDataForStar();
            }
        }
        prioritySeekBar.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
                seekbarCounter = (int) leftValue;
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }
        });
    }

    private void disableAllFields() {
        edtName.setEnabled(false);
        edtDesc.setEnabled(false);
        edtStartDate.setEnabled(false);
        edtFinishDate.setEnabled(false);
        edtAlarmDate.setEnabled(false);
        edtAlarmTime.setEnabled(false);
        edtAddMember.setEnabled(false);
        txtAccessNotes.setOnClickListener(null);
        prioritySeekBar.setEnabled(false);
        txtSave.setVisibility(View.INVISIBLE);
        imgLessPriority.setOnClickListener(null);
        imgAddPriority.setOnClickListener(null);
    }


    private void initViews() {
        imgAddTask = findViewById(R.id.img_add_task);
        edtName = findViewById(R.id.edt_name);
        edtDesc = findViewById(R.id.edt_desc);
        edtStartDate = findViewById(R.id.edt_start_date);
        edtStartTime=findViewById(R.id.edt_start_time);
        edtFinishDate = findViewById(R.id.edt_finish_date);
        edtFinishTime=findViewById(R.id.edt_finish_time);
        edtAlarmDate = findViewById(R.id.edt_alarm_date);
        edtAlarmTime = findViewById(R.id.edt_alarm_time);
        edtAddMember = findViewById(R.id.edt_add_member);
        txtAccessNotes = findViewById(R.id.txt_access_notes);
        prioritySeekBar = findViewById(R.id.seekbar_priority);
        txtSave = findViewById(R.id.txt_save);
        imgAddPriority = findViewById(R.id.img_add_priority);
        imgLessPriority = findViewById(R.id.img_less_priority);
        txtAddDocument = findViewById(R.id.txt_add_document);
        recyclerViewDocuments = findViewById(R.id.recycler_documents);
    }

    private void setOnTouch() {

        edtStartDate.setOnTouchListener(this);
        edtStartTime.setOnTouchListener(this);
        edtFinishDate.setOnTouchListener(this);
        edtFinishTime.setOnTouchListener(this);
        edtAlarmDate.setOnTouchListener(this);
        edtAlarmTime.setOnTouchListener(this);
        edtAddMember.setOnTouchListener(this);
        txtAddDocument.setOnClickListener(this);
    }

    private void setOnClick() {
        findViewById(R.id.view_transparent).setOnClickListener(this);
        txtSave.setOnClickListener(this);
        findViewById(R.id.txt_cancel).setOnClickListener(this);
        imgAddPriority.setOnClickListener(this);
        imgLessPriority.setOnClickListener(this);
        txtAccessNotes.setOnClickListener(this);
        imgAddTask.setOnClickListener(this);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.stay, R.anim.slide_down);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.view_transparent:
                onBackPressed();
                break;
            case R.id.img_add_task:
                if (edtName.getText().toString().trim().isEmpty())
                    AndroidUtils.showToast(AddTaskActivity.this, edtName.getHint().toString() + getResources().getString(R.string.cant_empty));
                else if (edtDesc.getText().toString().trim().isEmpty())
                    AndroidUtils.showToast(AddTaskActivity.this, edtDesc.getHint().toString() + getResources().getString(R.string.cant_empty));
                else if (!edtStartDate.getText().toString().trim().isEmpty() && edtFinishDate.getText().toString().trim().isEmpty())
                    AndroidUtils.showToast(AddTaskActivity.this, "Finish Date can not be empty");
                else if (!edtFinishDate.getText().toString().trim().isEmpty() && edtStartDate.getText().toString().trim().isEmpty())
                    AndroidUtils.showToast(AddTaskActivity.this, "Start Date can not be empty");

                else if (!edtStartTime.getText().toString().trim().isEmpty() && edtFinishTime.getText().toString().trim().isEmpty())
                    AndroidUtils.showToast(AddTaskActivity.this, "Finish Time can not be empty");
                else if (!edtFinishTime.getText().toString().trim().isEmpty() && edtStartTime.getText().toString().trim().isEmpty())
                    AndroidUtils.showToast(AddTaskActivity.this, "Start Time can not be empty");

                else if (!edtAlarmTime.getText().toString().trim().isEmpty() && edtAlarmDate.getText().toString().trim().isEmpty())
                    AndroidUtils.showToast(AddTaskActivity.this, "Alarm Date can not be empty");
                else if (!edtAlarmDate.getText().toString().trim().isEmpty() && edtAlarmTime.getText().toString().trim().isEmpty())
                    AndroidUtils.showToast(AddTaskActivity.this, "Alarm Time can not be empty");
                else if(!edtStartDate.getText().toString().trim().isEmpty() && !edtFinishDate.getText().toString().trim().isEmpty())
                {
                    Date dateFinish = null, dateStart = null;
                    try {
                        dateStart = new SimpleDateFormat("yyyy-MM-dd").parse(formattedStartDate);
                        dateFinish = new SimpleDateFormat("yyyy-MM-dd").parse(formattedFinishDate);

                        if (dateStart.after(dateFinish))
                            AndroidUtils.showToast(AddTaskActivity.this, "Finish Date must be greater than Start Date.");
                        else {
                            if (ConnectivityController.isNetworkAvailable(AddTaskActivity.this)) {
                                switch (source) {
                                    case "star":
                                        StarRequestModel starRequestModel = new StarRequestModel();
                                        starRequestModel.setName(edtName.getText().toString());
                                        starRequestModel.setDescription(edtDesc.getText().toString());
                                        starRequestModel.setStart_date(formattedStartDate);
                                        starRequestModel.setStart_time(formattedStartTime);
                                        starRequestModel.setFinish_date(formattedFinishDate);
                                        starRequestModel.setFinish_time(formattedFinishTime);
                                        starRequestModel.setAlarm_date(formattedAlarmDate);
                                        starRequestModel.setAlarm_time(formattedAlarmTime);
                                        starRequestModel.setNotes(accessNotes);
                                        starRequestModel.setPriority(String.valueOf(seekbarCounter));
                                        starRequestModel.setDocuments(documentModels);
                                        starRequestModel.setMembers(memberRequestList);
                                        callCreateStarApi(false, starRequestModel);
                                        break;
                                    case "planet":
                                        PlanetRequestModel planetRequestModel=new PlanetRequestModel();
                                        planetRequestModel.setStar_id(starId);
                                        planetRequestModel.setName(edtName.getText().toString());
                                        planetRequestModel.setDescription(edtDesc.getText().toString());
                                        planetRequestModel.setStart_date(formattedStartDate);
                                        planetRequestModel.setStart_time(formattedStartTime);
                                        planetRequestModel.setFinish_date(formattedFinishDate);
                                        planetRequestModel.setFinish_time(formattedFinishTime);
                                        planetRequestModel.setAlarm_date(formattedAlarmDate);
                                        planetRequestModel.setAlarm_time(formattedAlarmTime);
                                        planetRequestModel.setNotes(accessNotes);
                                        planetRequestModel.setPriority(String.valueOf(seekbarCounter));
                                        planetRequestModel.setDocuments(documentModels);
                                        planetRequestModel.setMembers(memberRequestList);
                                        callCreatePlanetApi(false,planetRequestModel);
                                        break;
                                    case "moon":
                                        MoonRequestModel moonRequestModel=new MoonRequestModel();
                                        moonRequestModel.setPlanet_id(planetId);
                                        moonRequestModel.setName(edtName.getText().toString());
                                        moonRequestModel.setDescription(edtDesc.getText().toString());
                                        moonRequestModel.setStart_date(formattedStartDate);
                                        moonRequestModel.setStart_time(formattedStartTime);
                                        moonRequestModel.setFinish_date(formattedFinishDate);
                                        moonRequestModel.setFinish_time(formattedFinishTime);
                                        moonRequestModel.setAlarm_date(formattedAlarmDate);
                                        moonRequestModel.setAlarm_time(formattedAlarmTime);
                                        moonRequestModel.setNotes(accessNotes);
                                        moonRequestModel.setPriority(String.valueOf(seekbarCounter));
                                        moonRequestModel.setDocuments(documentModels);
                                        moonRequestModel.setMembers(memberRequestList);
                                        callCreateMoonApi(false,moonRequestModel);
                                        break;
                                    case "satellite":
                                        SatelliteRequestModel satelliteRequestModel=new SatelliteRequestModel();
                                        satelliteRequestModel.setMoon_id(moonId);
                                        satelliteRequestModel.setName(edtName.getText().toString());
                                        satelliteRequestModel.setDescription(edtDesc.getText().toString());
                                        satelliteRequestModel.setStart_date(formattedStartDate);
                                        satelliteRequestModel.setStart_time(formattedStartTime);
                                        satelliteRequestModel.setFinish_date(formattedFinishDate);
                                        satelliteRequestModel.setFinish_time(formattedFinishTime);
                                        satelliteRequestModel.setAlarm_date(formattedAlarmDate);
                                        satelliteRequestModel.setAlarm_time(formattedAlarmTime);
                                        satelliteRequestModel.setNotes(accessNotes);
                                        satelliteRequestModel.setPriority(String.valueOf(seekbarCounter));
                                        satelliteRequestModel.setDocuments(documentModels);
                                        satelliteRequestModel.setMembers(memberRequestList);
                                        callCreateSatelliteApi(satelliteRequestModel);
                                        break;

                                }
                            } else
                                AndroidUtils.showToast(AddTaskActivity.this, getResources().getString(R.string.no_internet));
                        }
                    }
                        catch (ParseException e) {
                            e.printStackTrace();
                        }
                }



                break;
            case R.id.txt_add_document:
                DocumentPickerFragment documentPickerFragment = new DocumentPickerFragment();
                documentPickerFragment.show(getSupportFragmentManager(), documentPickerFragment.getTag());
                documentPickerFragment.setCancelable(false);
                break;
            case R.id.txt_access_notes:

                Intent intent = new Intent(AddTaskActivity.this, AccessNotesActivity.class);
                intent.putExtra("accessNote", accessNotes);
                startActivityForResult(intent, 1);
                break;
            case R.id.txt_save:
                if (edtName.getText().toString().trim().isEmpty())
                    AndroidUtils.showToast(AddTaskActivity.this, edtName.getHint().toString() + getResources().getString(R.string.cant_empty));
                else if (edtDesc.getText().toString().trim().isEmpty())
                    AndroidUtils.showToast(AddTaskActivity.this, edtDesc.getHint().toString() + getResources().getString(R.string.cant_empty));
                else if (!edtStartDate.getText().toString().trim().isEmpty() && edtFinishDate.getText().toString().trim().isEmpty())
                    AndroidUtils.showToast(AddTaskActivity.this, "Finish Date can not be empty");
                else if (!edtFinishDate.getText().toString().trim().isEmpty() && edtStartDate.getText().toString().trim().isEmpty())
                    AndroidUtils.showToast(AddTaskActivity.this, "Start Date can not be empty");

                else if (!edtStartTime.getText().toString().trim().isEmpty() && edtFinishTime.getText().toString().trim().isEmpty())
                    AndroidUtils.showToast(AddTaskActivity.this, "Finish Time can not be empty");
                else if (!edtFinishTime.getText().toString().trim().isEmpty() && edtStartTime.getText().toString().trim().isEmpty())
                    AndroidUtils.showToast(AddTaskActivity.this, "Start Time can not be empty");

                else if (!edtAlarmTime.getText().toString().trim().isEmpty() && edtAlarmDate.getText().toString().trim().isEmpty())
                    AndroidUtils.showToast(AddTaskActivity.this, "Alarm Date can not be empty");
                else if (!edtAlarmDate.getText().toString().trim().isEmpty() && edtAlarmTime.getText().toString().trim().isEmpty())
                    AndroidUtils.showToast(AddTaskActivity.this, "Alarm Time can not be empty");
                else if(!edtStartDate.getText().toString().trim().isEmpty() && !edtFinishDate.getText().toString().trim().isEmpty())
                {
                    Date dateFinish = null, dateStart = null;
                    try {
                        dateStart = new SimpleDateFormat("yyyy-MM-dd").parse(formattedStartDate);
                        dateFinish = new SimpleDateFormat("yyyy-MM-dd").parse(formattedFinishDate);

                        if(dateStart.after(dateFinish))
                            AndroidUtils.showToast(AddTaskActivity.this,"Finish Date must be greater than Start Date.");
                        else {
                            if (ConnectivityController.isNetworkAvailable(AddTaskActivity.this)) {

                                switch (source) {
                                    case "star":
                                        StarRequestModel starRequestModel = new StarRequestModel();
                                        starRequestModel.setName(edtName.getText().toString());
                                        starRequestModel.setDescription(edtDesc.getText().toString());
                                        starRequestModel.setStart_date(formattedStartDate);
                                        starRequestModel.setStart_time(formattedStartTime);
                                        starRequestModel.setFinish_date(formattedFinishDate);
                                        starRequestModel.setFinish_time(formattedFinishTime);
                                        starRequestModel.setAlarm_date(formattedAlarmDate);
                                        starRequestModel.setAlarm_time(formattedAlarmTime);
                                        starRequestModel.setNotes(accessNotes);
                                        starRequestModel.setPriority(String.valueOf(seekbarCounter));
                                        starRequestModel.setDocuments(documentModels);
                                        starRequestModel.setMembers(memberRequestList);
                                        //starRequestModel.setType("S");
                                        if (getIntent().getStringExtra(ConstantUtils.PURPOSE) != null && getIntent().getStringExtra(ConstantUtils.PURPOSE).equals("update")) {
                                            starRequestModel.setStar_id(starResponseModel.getId());
                                            callUpdateStarApi(starRequestModel);
                                        } else
                                            callCreateStarApi(true, starRequestModel);
                                        break;
                                    case "planet":
                                        PlanetRequestModel planetRequestModel=new PlanetRequestModel();
                                        planetRequestModel.setStar_id(starId);
                                        planetRequestModel.setName(edtName.getText().toString());
                                        planetRequestModel.setDescription(edtDesc.getText().toString());
                                        planetRequestModel.setStart_date(formattedStartDate);
                                        planetRequestModel.setStart_time(formattedStartTime);
                                        planetRequestModel.setFinish_date(formattedFinishDate);
                                        planetRequestModel.setFinish_time(formattedFinishTime);
                                        planetRequestModel.setAlarm_date(formattedAlarmDate);
                                        planetRequestModel.setAlarm_time(formattedAlarmTime);
                                        planetRequestModel.setNotes(accessNotes);
                                        planetRequestModel.setPriority(String.valueOf(seekbarCounter));
                                        planetRequestModel.setDocuments(documentModels);
                                        planetRequestModel.setMembers(memberRequestList);
                                        if (getIntent().getStringExtra(ConstantUtils.PURPOSE) != null && getIntent().getStringExtra(ConstantUtils.PURPOSE).equals("update"))
                                        {
                                            planetRequestModel.setPlanet_id(planetResponseModel.getPlanet_id());
                                            callUpdatePlanetApi(planetRequestModel);
                                        }
                                        else
                                            callCreatePlanetApi(true,planetRequestModel);
                                        break;
                                    case "moon":
                                        MoonRequestModel moonRequestModel=new MoonRequestModel();
                                        moonRequestModel.setPlanet_id(planetId);
                                        moonRequestModel.setName(edtName.getText().toString());
                                        moonRequestModel.setDescription(edtDesc.getText().toString());
                                        moonRequestModel.setStart_date(formattedStartDate);
                                        moonRequestModel.setStart_time(formattedStartTime);
                                        moonRequestModel.setFinish_date(formattedFinishDate);
                                        moonRequestModel.setFinish_time(formattedFinishTime);
                                        moonRequestModel.setAlarm_date(formattedAlarmDate);
                                        moonRequestModel.setAlarm_time(formattedAlarmTime);
                                        moonRequestModel.setNotes(accessNotes);
                                        moonRequestModel.setPriority(String.valueOf(seekbarCounter));
                                        moonRequestModel.setDocuments(documentModels);
                                        moonRequestModel.setMembers(memberRequestList);
                                        if (getIntent().getStringExtra(ConstantUtils.PURPOSE) != null && getIntent().getStringExtra(ConstantUtils.PURPOSE).equals("update"))
                                        {
                                            moonRequestModel.setMoon_id(moonResponseModel.getMoon_id());
                                            callUpdateMoonApi(moonRequestModel);
                                        }
                                        else
                                            callCreateMoonApi(true,moonRequestModel);
                                        break;
                                    case "satellite":
                                        SatelliteRequestModel satelliteRequestModel=new SatelliteRequestModel();
                                        satelliteRequestModel.setMoon_id(moonId);
                                        satelliteRequestModel.setName(edtName.getText().toString());
                                        satelliteRequestModel.setDescription(edtDesc.getText().toString());
                                        satelliteRequestModel.setStart_date(formattedStartDate);
                                        satelliteRequestModel.setStart_time(formattedStartTime);
                                        satelliteRequestModel.setFinish_date(formattedFinishDate);
                                        satelliteRequestModel.setFinish_time(formattedFinishTime);
                                        satelliteRequestModel.setAlarm_date(formattedAlarmDate);
                                        satelliteRequestModel.setAlarm_time(formattedAlarmTime);
                                        satelliteRequestModel.setNotes(accessNotes);
                                        satelliteRequestModel.setPriority(String.valueOf(seekbarCounter));
                                        satelliteRequestModel.setDocuments(documentModels);
                                        satelliteRequestModel.setMembers(memberRequestList);
                                        if (getIntent().getStringExtra(ConstantUtils.PURPOSE) != null && getIntent().getStringExtra(ConstantUtils.PURPOSE).equals("update"))
                                        {
                                            satelliteRequestModel.setSatellite_id(satelliteResponseModel.getSatellite_id());
                                            callUpdateSatelliteApi(satelliteRequestModel);
                                        }
                                        else
                                            callCreateSatelliteApi(satelliteRequestModel);
                                        break;
                                    case "comet":
                                        CometRequestModel cometRequestModel=new CometRequestModel();
                                        cometRequestModel.setName(edtName.getText().toString());
                                        cometRequestModel.setDescription(edtDesc.getText().toString());
                                        cometRequestModel.setStart_date(formattedStartDate);
                                        cometRequestModel.setStart_time(formattedStartTime);
                                        cometRequestModel.setFinish_date(formattedFinishDate);
                                        cometRequestModel.setFinish_time(formattedFinishTime);
                                        cometRequestModel.setAlarm_date(formattedAlarmDate);
                                        cometRequestModel.setAlarm_time(formattedAlarmTime);
                                        cometRequestModel.setNotes(accessNotes);
                                        cometRequestModel.setPriority(String.valueOf(seekbarCounter));
                                        cometRequestModel.setDocuments(documentModels);
                                        cometRequestModel.setMembers(memberRequestList);
                                        if (getIntent().getStringExtra(ConstantUtils.PURPOSE) != null && getIntent().getStringExtra(ConstantUtils.PURPOSE).equals("update"))
                                        {
                                            cometRequestModel.setComet_id(starResponseModel.getId());
                                            callUpdateCometApi(cometRequestModel);
                                        }
                                        else
                                            callCreateCometApi(cometRequestModel);

                                        break;
                                }

                            } else
                                AndroidUtils.showToast(AddTaskActivity.this, getResources().getString(R.string.no_internet));
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }


                break;
            case R.id.txt_cancel:
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.img_add_priority:
                if (seekbarCounter != 10) {
                    seekbarCounter++;
                    prioritySeekBar.setValue(seekbarCounter);
                }
                break;
            case R.id.img_less_priority:
                if (seekbarCounter != 1) {
                    seekbarCounter--;
                    prioritySeekBar.setValue(seekbarCounter);
                }
                break;
        }
    }


    public void callCreateStarApi(final boolean isSave, StarRequestModel starRequestModel) {
        progressDialog.show();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<StarResponseModel> starResponseModelCall = apiService.createStar(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), starRequestModel);
        starResponseModelCall.enqueue(new Callback<StarResponseModel>() {
            @Override
            public void onResponse(Call<StarResponseModel> call, Response<StarResponseModel> response) {
                progressDialog.dismiss();
                if (response.code() == 200 && response.body() != null) {
                    AndroidUtils.showToast(AddTaskActivity.this, "Star has been added successfully");

                    starResponseModel = response.body();

                    if (!isSave) {
                        Intent activityIntent = new Intent(AddTaskActivity.this, AddTaskActivity.class);
                        activityIntent.putExtra(ConstantUtils.STAR_RESPONSE_MODEL, starResponseModel);
                        activityIntent.putExtra(ConstantUtils.SOURCE, "planet");
                        activityIntent.putExtra(ConstantUtils.STAR_ID, String.valueOf(starResponseModel.getId()));
                        startActivity(activityIntent);
                        overridePendingTransition(R.anim.slide_in_up, R.anim.stay);
                    }
                    setResult(RESULT_OK);
                    finish();

                } else if (response.code() == 500) {
                    AndroidUtils.showToast(AddTaskActivity.this, getResources().getString(R.string.some_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<StarResponseModel> call, Throwable t) {
                progressDialog.dismiss();
                AndroidUtils.showToast(AddTaskActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });

    }


    public void callCreatePlanetApi(final boolean isSave,PlanetRequestModel planetRequestModel) {
        progressDialog.show();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        final Call<PlanetResponseModel> planetResponseModelCall = apiService.createPlanet(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""),planetRequestModel);
        planetResponseModelCall.enqueue(new Callback<PlanetResponseModel>() {
            @Override
            public void onResponse(Call<PlanetResponseModel> call, Response<PlanetResponseModel> response) {
                progressDialog.dismiss();
                if (response.code() == 200 && response.body() != null) {
                    AndroidUtils.showToast(AddTaskActivity.this, "Planet has been added successfully");

                    planetResponseModel = response.body();
                    if (!isSave) {
                        Intent activityIntent = new Intent(AddTaskActivity.this, AddTaskActivity.class);
                        activityIntent.putExtra(ConstantUtils.PLANET_RESPONSE_MODEL, planetResponseModel);
                        activityIntent.putExtra(ConstantUtils.PLANET_ID, String.valueOf(planetResponseModel.getPlanet_id()));
                        activityIntent.putExtra(ConstantUtils.SOURCE, "moon");
                        startActivity(activityIntent);
                        overridePendingTransition(R.anim.slide_in_up, R.anim.stay);
                    }
                    setResult(RESULT_OK);
                    finish();
                } else if (response.code() == 500) {
                    AndroidUtils.showToast(AddTaskActivity.this, getResources().getString(R.string.some_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<PlanetResponseModel> call, Throwable t) {
                progressDialog.dismiss();
                AndroidUtils.showToast(AddTaskActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });

    }

    public void callCreateMoonApi(final boolean isSave,MoonRequestModel moonRequestModel) {
        progressDialog.show();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        final Call<MoonResponseModel> moonResponseModelCall = apiService.createMoon(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), moonRequestModel);
        moonResponseModelCall.enqueue(new Callback<MoonResponseModel>() {
            @Override
            public void onResponse(Call<MoonResponseModel> call, Response<MoonResponseModel> response) {
                progressDialog.dismiss();
                if (response.code() == 200 && response.body() != null) {
                    AndroidUtils.showToast(AddTaskActivity.this, "Moon has been added successfully");
                    moonResponseModel = response.body();
                    if (!isSave) {
                        Intent activityIntent = new Intent(AddTaskActivity.this, AddTaskActivity.class);
                        activityIntent.putExtra(ConstantUtils.MOON_RESPONSE_MODEL, moonResponseModel);
                        activityIntent.putExtra(ConstantUtils.MOON_ID, String.valueOf(moonResponseModel.getMoon_id()));
                        activityIntent.putExtra(ConstantUtils.SOURCE, "satellite");
                        startActivity(activityIntent);
                        overridePendingTransition(R.anim.slide_in_up, R.anim.stay);
                    }
                    setResult(RESULT_OK);
                    finish();
                } else if (response.code() == 500) {
                    AndroidUtils.showToast(AddTaskActivity.this, getResources().getString(R.string.some_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<MoonResponseModel> call, Throwable t) {
                progressDialog.dismiss();
                AndroidUtils.showToast(AddTaskActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });
    }

    public void callCreateSatelliteApi(SatelliteRequestModel satelliteRequestModel) {
        progressDialog.show();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<SatelliteResponseModel> satelliteResponseModelCall = apiService.createSatellite(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), satelliteRequestModel);
        satelliteResponseModelCall.enqueue(new Callback<SatelliteResponseModel>() {
            @Override
            public void onResponse(Call<SatelliteResponseModel> call, Response<SatelliteResponseModel> response) {
                progressDialog.dismiss();
                if (response.code() == 200 && response.body() != null) {
                    AndroidUtils.showToast(AddTaskActivity.this, "Satellite has been added successfully");
                    setResult(RESULT_OK);
                    finish();
                } else if (response.code() == 500) {
                    AndroidUtils.showToast(AddTaskActivity.this, getResources().getString(R.string.some_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<SatelliteResponseModel> call, Throwable t) {
                progressDialog.dismiss();
                AndroidUtils.showToast(AddTaskActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });
    }


    public void callCreateCometApi(CometRequestModel cometRequestModel) {
        progressDialog.show();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<CometResponseModel> cometResponseModelCall = apiService.createComet(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), cometRequestModel);
        cometResponseModelCall.enqueue(new Callback<CometResponseModel>() {
            @Override
            public void onResponse(Call<CometResponseModel> call, Response<CometResponseModel> response) {
                progressDialog.dismiss();
                if (response.code() == 200 && response.body() != null) {
                    AndroidUtils.showToast(AddTaskActivity.this, "Comet has been added successfully");
                    setResult(RESULT_OK);
                    finish();
                } else if (response.code() == 500) {
                    AndroidUtils.showToast(AddTaskActivity.this, getResources().getString(R.string.some_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<CometResponseModel> call, Throwable t) {
                progressDialog.dismiss();
                AndroidUtils.showToast(AddTaskActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });
    }


    public void callUpdateStarApi(StarRequestModel starRequestModel) {
        progressDialog.show();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<StarResponseModel> starResponseModelCall = apiService.updateStar(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), starRequestModel);
        starResponseModelCall.enqueue(new Callback<StarResponseModel>() {
            @Override
            public void onResponse(Call<StarResponseModel> call, Response<StarResponseModel> response) {
                progressDialog.dismiss();
                if (response.code() == 200 && response.body() != null) {
                    AndroidUtils.showToast(AddTaskActivity.this, "Star has been updated successfully");
                    Intent intent = getIntent();
                    intent.putExtra(ConstantUtils.TASK_POSITION, taskPosition);
                    intent.putExtra(ConstantUtils.STAR_RESPONSE_MODEL, response.body());
                    setResult(RESULT_OK, intent);
                    finish();
                } else if (response.code() == 500) {
                    AndroidUtils.showToast(AddTaskActivity.this, getResources().getString(R.string.some_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<StarResponseModel> call, Throwable t) {
                progressDialog.dismiss();
                AndroidUtils.showToast(AddTaskActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });
    }

    private void callUpdatePlanetApi(PlanetRequestModel planetRequestModel) {
        progressDialog.show();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<PlanetResponseModel> updatePlanetResponseCall = apiService.updatePlanet(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), planetRequestModel);
        updatePlanetResponseCall.enqueue(new Callback<PlanetResponseModel>() {
            @Override
            public void onResponse(Call<PlanetResponseModel> call, Response<PlanetResponseModel> response) {
                progressDialog.dismiss();
                if (response.code() == 200 && response.body() != null) {
                    AndroidUtils.showToast(AddTaskActivity.this, "Planet has been updated successfully");
                    Intent intent = getIntent();
                    intent.putExtra(ConstantUtils.TASK_POSITION, taskPosition);
                    intent.putExtra(ConstantUtils.PLANET_RESPONSE_MODEL, response.body());
                    setResult(RESULT_OK, intent);
                    finish();
                } else if (response.code() == 500) {
                    AndroidUtils.showToast(AddTaskActivity.this, getResources().getString(R.string.some_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<PlanetResponseModel> call, Throwable t) {
                progressDialog.dismiss();
                AndroidUtils.showToast(AddTaskActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });
    }

    private void callUpdateMoonApi(MoonRequestModel moonRequestModel) {
        progressDialog.show();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<MoonResponseModel> updateMoonResponseCall = apiService.updateMoon(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), moonRequestModel);
        updateMoonResponseCall.enqueue(new Callback<MoonResponseModel>() {
            @Override
            public void onResponse(Call<MoonResponseModel> call, Response<MoonResponseModel> response) {
                progressDialog.dismiss();
                if (response.code() == 200 && response.body() != null) {
                    AndroidUtils.showToast(AddTaskActivity.this, "Moon has been updated successfully");
                    Intent intent = getIntent();
                    intent.putExtra(ConstantUtils.TASK_POSITION, taskPosition);
                    intent.putExtra(ConstantUtils.MOON_RESPONSE_MODEL, response.body());
                    setResult(RESULT_OK, intent);
                    finish();
                } else if (response.code() == 500) {
                    AndroidUtils.showToast(AddTaskActivity.this, getResources().getString(R.string.some_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<MoonResponseModel> call, Throwable t) {
                progressDialog.dismiss();
                AndroidUtils.showToast(AddTaskActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });
    }

    private void callUpdateSatelliteApi(SatelliteRequestModel satelliteRequestModel) {
        progressDialog.show();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<SatelliteResponseModel> updateSatelliteResponseCall = apiService.updateSatellite(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), satelliteRequestModel);
        updateSatelliteResponseCall.enqueue(new Callback<SatelliteResponseModel>() {
            @Override
            public void onResponse(Call<SatelliteResponseModel> call, Response<SatelliteResponseModel> response) {
                progressDialog.dismiss();
                if (response.code() == 200 && response.body() != null) {
                    AndroidUtils.showToast(AddTaskActivity.this, "Satellite has been updated successfully");
                    Intent intent = getIntent();
                    intent.putExtra(ConstantUtils.TASK_POSITION, taskPosition);
                    intent.putExtra(ConstantUtils.SATELLITE_RESPONSE_MODEL, response.body());
                    setResult(RESULT_OK, intent);
                    finish();
                } else if (response.code() == 500) {
                    AndroidUtils.showToast(AddTaskActivity.this, getResources().getString(R.string.some_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<SatelliteResponseModel> call, Throwable t) {
                progressDialog.dismiss();
                AndroidUtils.showToast(AddTaskActivity.this, getResources().getString(R.string.some_went_wrong));
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
                    AndroidUtils.showToast(AddTaskActivity.this, "Comet has been updated successfully");
                    Intent intent = getIntent();
                    intent.putExtra(ConstantUtils.TASK_POSITION, taskPosition);
                    intent.putExtra(ConstantUtils.COMET_RESPONSE_MODEL, response.body());
                    setResult(RESULT_OK, intent);
                    finish();
                } else if (response.code() == 500) {
                    AndroidUtils.showToast(AddTaskActivity.this, getResources().getString(R.string.some_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<CometResponseModel> call, Throwable t) {
                progressDialog.dismiss();
                AndroidUtils.showToast(AddTaskActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.txt_access_notes:
                if (event.ACTION_UP == event.getAction()) {
                    Intent intent = new Intent(AddTaskActivity.this, AccessNotesActivity.class);
                    startActivityForResult(intent, 1);
                }
                break;
            case R.id.edt_start_date:
                if (event.ACTION_UP == event.getAction()) {
                    openDatePicker("startDate");
                }
                break;
            case R.id.edt_start_time:
                if (event.ACTION_UP == event.getAction()) {
                    openTimePicker("startTime");
                }
                break;
            case R.id.edt_finish_date:
                if (event.ACTION_UP == event.getAction()) {
                    openDatePicker("finishDate");
                }
                break;
            case R.id.edt_finish_time:
                if (event.ACTION_UP == event.getAction()) {
                    openTimePicker("finishTime");
                }
                break;
            case R.id.edt_alarm_date:
                if (event.ACTION_UP == event.getAction()) {
                    openDatePicker("alarmDate");
                }
                break;
            case R.id.edt_alarm_time:
                if (event.ACTION_UP == event.getAction()) {
                  openTimePicker("alarmTime");
                }
                break;
            case R.id.edt_add_member:
                if (event.ACTION_UP == event.getAction()) {
                    Intent intent = new Intent(AddTaskActivity.this, AddMembersActivity.class);
                    intent.putParcelableArrayListExtra(ConstantUtils.MEMBERS_REQUEST_ARRAY_LIST, memberRequestList);
                    startActivityForResult(intent, MEMBDER_RESULT_CODE);
                }
                break;


        }
        return true;
    }

    public void  openTimePicker(final  String source)
    {
        int mHour=0, mMinute=0;
        final Calendar calendar;

        if (source.equals("startTime")) {
            if (startTimeObject != null) {
                calendar = AndroidUtils.getCalendarTimeObject(startTimeObject);
                mHour = calendar.get(Calendar.HOUR_OF_DAY);
                mMinute = calendar.get(Calendar.MINUTE);

            } else {
                calendar = Calendar.getInstance();
                mHour = calendar.get(Calendar.HOUR_OF_DAY);
                mMinute = calendar.get(Calendar.MINUTE);
            }
        } else if (source.equals("finishTime")) {
            if (finishTimeObject != null) {
                calendar = AndroidUtils.getCalendarTimeObject(finishTimeObject);
                mHour = calendar.get(Calendar.HOUR_OF_DAY);
                mMinute = calendar.get(Calendar.MINUTE);

            } else {
                calendar = Calendar.getInstance();
                mHour = calendar.get(Calendar.HOUR_OF_DAY);
                mMinute = calendar.get(Calendar.MINUTE);
            }
        } else if (source.equals("alarmTime")) {
            if (alarmTimeObject != null) {
                calendar = AndroidUtils.getCalendarTimeObject(alarmTimeObject);
                mHour = calendar.get(Calendar.HOUR_OF_DAY);
                mMinute = calendar.get(Calendar.MINUTE);
            } else {
                calendar = Calendar.getInstance();
                mHour = calendar.get(Calendar.HOUR_OF_DAY);
                mMinute = calendar.get(Calendar.MINUTE);
            }
        }

        TimePickerDialog timePickerDialog = new TimePickerDialog(AddTaskActivity.this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        try {

                            if (source.equals("startTime")) {
                                startTimeObject = new SimpleDateFormat("HH:mm").parse(String.valueOf(hourOfDay) + ":" + String.valueOf(minute));
                                formattedStartTime = new SimpleDateFormat("HH:mm").format(startTimeObject);
                                edtStartTime.setText(new SimpleDateFormat("hh:mm a").format(startTimeObject));
                            } else if (source.equals("finishTime")) {
                                finishTimeObject= new SimpleDateFormat("HH:mm").parse(String.valueOf(hourOfDay) + ":" + String.valueOf(minute));
                                formattedFinishTime = new SimpleDateFormat("HH:mm").format(finishTimeObject);
                                edtFinishTime.setText(new SimpleDateFormat("hh:mm a").format(finishTimeObject));
                            } else if (source.equals("alarmTime")) {
                                alarmTimeObject = new SimpleDateFormat("HH:mm").parse(String.valueOf(hourOfDay) + ":" + String.valueOf(minute));
                                formattedAlarmTime = new SimpleDateFormat("HH:mm").format(alarmTimeObject);
                                edtAlarmTime.setText(new SimpleDateFormat("hh:mm a").format(alarmTimeObject));
                            }




                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    public void openDatePicker(final String source) {
        int mYear = 0, mMonth = 0, mDay = 0;
        Calendar c;

        if (source.equals("startDate")) {
            if (startDateObject != null) {
                c = AndroidUtils.getCalendarDateObject(startDateObject);
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

            } else {
                c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);
            }
        } else if (source.equals("finishDate")) {
            if (finishDateObject != null) {
                c = AndroidUtils.getCalendarDateObject(finishDateObject);
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

            } else {
                c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);
            }
        } else if (source.equals("alarmDate")) {
            if (alarmDateObject != null) {
                c = AndroidUtils.getCalendarDateObject(alarmDateObject);
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

            } else {
                c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);
            }
        }


        DatePickerDialog datePickerDialog = new DatePickerDialog(AddTaskActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                try {

                    if (source.equals("startDate")) {
                        startDateObject = new SimpleDateFormat("dd/MM/yyyy").parse(String.valueOf(dayOfMonth) + "/" + String.valueOf(month + 1) + "/" + String.valueOf(year));
                        formattedStartDate = new SimpleDateFormat("yyyy-MM-dd").format(startDateObject);
                        edtStartDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(startDateObject));
                    } else if (source.equals("finishDate")) {
                        finishDateObject = new SimpleDateFormat("dd/MM/yyyy").parse(String.valueOf(dayOfMonth) + "/" + String.valueOf(month + 1) + "/" + String.valueOf(year));
                        formattedFinishDate = new SimpleDateFormat("yyyy-MM-dd").format(finishDateObject);
                        edtFinishDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(finishDateObject));
                    } else if (source.equals("alarmDate")) {
                        alarmDateObject = new SimpleDateFormat("dd/MM/yyyy").parse(String.valueOf(dayOfMonth) + "/" + String.valueOf(month + 1) + "/" + String.valueOf(year));
                        formattedAlarmDate = new SimpleDateFormat("yyyy-MM-dd").format(alarmDateObject);
                        edtAlarmDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(alarmDateObject));
                    }


                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    if (data.getStringExtra("accessNote") != null)
                        accessNotes = data.getStringExtra("accessNote");
                }
                break;
            case MEMBDER_RESULT_CODE:
                if (resultCode == RESULT_OK && data!=null) {

                    memberRequestList.clear();
                    memberRequestList.addAll(data.<MemberRequestModel>getParcelableArrayListExtra(ConstantUtils.MEMBERS_REQUEST_ARRAY_LIST));

                    if(memberRequestList!=null && memberRequestList.size()>0) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 0; i < memberRequestList.size(); i++) {

                            stringBuilder.append(memberRequestList.get(i).getMember_name());
                            stringBuilder.append(",");
                        }
                        edtAddMember.setText(String.valueOf(stringBuilder).substring(0, String.valueOf(stringBuilder).length() - 1));
                    }else edtAddMember.setText("");
                }
                break;
            case Constant.REQUEST_CODE_PICK_FILE:
                if (resultCode == RESULT_OK) {
                    ArrayList<NormalFile> list = data.getParcelableArrayListExtra(Constant.RESULT_PICK_FILE);
                    if (list.size() > 0) {
                        list.get(0).getPath();
                        if (list.get(0).getPath() != null && !list.get(0).getPath().isEmpty()) {
                            RequestBody requestFile = RequestBody.create(MediaType.parse(AndroidUtils.getMimeType(list.get(0).getPath())), new File(list.get(0).getPath()));
                            partBodyDocument = MultipartBody.Part.createFormData("document", new File(list.get(0).getPath()).getName(), requestFile);

                            if (ConnectivityController.isNetworkAvailable(AddTaskActivity.this))
                                callUploadDocumentApi("P");
                            else
                                AndroidUtils.showToast(AddTaskActivity.this, getResources().getString(R.string.no_internet));
                        }
                    }

                }
                break;
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    Uri resultUri = result.getUri();
                    if (resultUri != null && resultUri.getPath() != null && !resultUri.getPath().isEmpty()) {

                        RequestBody requestFile = RequestBody.create(MediaType.parse(AndroidUtils.getMimeType(resultUri.getPath())), new File(resultUri.getPath()));
                        partBodyDocument = MultipartBody.Part.createFormData("document", new File(resultUri.getPath()).getName(), requestFile);

                        if (ConnectivityController.isNetworkAvailable(AddTaskActivity.this))
                            callUploadDocumentApi("I");
                        else
                            AndroidUtils.showToast(AddTaskActivity.this, getResources().getString(R.string.no_internet));
                    }
                }
                break;
        }

    }

    private void callUploadDocumentApi(final String documentType) {
        progressDialog.show();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetDoucmentResponse> getDoucmentResponseCall = apiService.uploadDocument(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), partBodyDocument);
        getDoucmentResponseCall.enqueue(new Callback<GetDoucmentResponse>() {
            @Override
            public void onResponse(Call<GetDoucmentResponse> call, Response<GetDoucmentResponse> response) {
                progressDialog.dismiss();
                if (response.code() == 200 && response.body() != null) {
                    if (response.body().getDocument_url() != null) {
                        AndroidUtils.showToast(AddTaskActivity.this, "Uploaded");
                        documentModels.add(new DocumentModel(response.body().getDocument_url(), documentType));

                        documentList.add(new DocumentResponseModel("1", response.body().getDocument_url(), documentType));
                        taskDocumentAdapter.notifyDataSetChanged();
                    }
                } else if (response.code() == 500)
                    AndroidUtils.showToast(AddTaskActivity.this, getResources().getString(R.string.some_went_wrong));

            }

            @Override
            public void onFailure(Call<GetDoucmentResponse> call, Throwable t) {
                progressDialog.dismiss();
                AndroidUtils.showToast(AddTaskActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent4 = new Intent(this, NormalFilePickActivity.class);
                intent4.putExtra(Constant.MAX_NUMBER, 1);
                intent4.putExtra(IS_NEED_FOLDER_LIST, false);
                intent4.putExtra(NormalFilePickActivity.SUFFIX,
                        new String[]{/*"doc", "docX",*/"pdf"});
                startActivityForResult(intent4, Constant.REQUEST_CODE_PICK_FILE);
            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // user rejected the permission
                if (!ActivityCompat.shouldShowRequestPermissionRationale(AddTaskActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    AndroidUtils.showLongToast(AddTaskActivity.this, "Go to Settings and Grant the permission to use this feature.");

                    // user also CHECKED "never ask again"
                    // you can either enable some fall back,
                    // disable features of your app
                    // or open another dialog explaining
                    // again the permission and directing to
                    // the app setting
                } else {

                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_WRITE_STORAGE);

                    // user did NOT check "never ask again"
                    // this is a good place to explain the user
                    // why you need the permission and ask if he wants
                    // to accept it (the rationale)
                }
            }
        }
    }

    private void setDataForStar() {


        if (starResponseModel.getName() != null && !starResponseModel.getName().isEmpty())
            edtName.setText(starResponseModel.getName());

        if (starResponseModel.getDescription() != null && !starResponseModel.getDescription().isEmpty())
            edtDesc.setText(starResponseModel.getDescription());

        if (starResponseModel.getStart_date() != null && !starResponseModel.getStart_date().isEmpty()) {
            try {
                startDateObject = new SimpleDateFormat("yyyy-MM-dd").parse(starResponseModel.getStart_date());
                formattedStartDate = new SimpleDateFormat("yyyy-MM-dd").format(startDateObject);
                edtStartDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(startDateObject));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (starResponseModel.getStart_time() != null && !starResponseModel.getStart_time().isEmpty()) {
            try {
                startTimeObject = new SimpleDateFormat("HH:mm").parse(starResponseModel.getStart_time());
                formattedStartTime = new SimpleDateFormat("HH:mm").format(startTimeObject);
                edtStartTime.setText(new SimpleDateFormat("hh:mm a").format(startTimeObject));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (starResponseModel.getFinish_date() != null && !starResponseModel.getFinish_date().isEmpty()) {
            try {
                finishDateObject = new SimpleDateFormat("yyyy-MM-dd").parse(starResponseModel.getFinish_date());
                formattedFinishDate = new SimpleDateFormat("yyyy-MM-dd").format(finishDateObject);
                edtFinishDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(finishDateObject));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (starResponseModel.getFinish_time() != null && !starResponseModel.getFinish_time().isEmpty()) {
            try {
                finishTimeObject = new SimpleDateFormat("HH:mm").parse(starResponseModel.getFinish_time());
                formattedFinishTime = new SimpleDateFormat("HH:mm").format(finishTimeObject);
                edtFinishTime.setText(new SimpleDateFormat("hh:mm a").format(finishTimeObject));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (starResponseModel.getAlarm_date() != null && !starResponseModel.getAlarm_date().isEmpty()) {
            try {
                alarmDateObject = new SimpleDateFormat("yyyy-MM-dd").parse(starResponseModel.getAlarm_date());
                formattedAlarmDate = new SimpleDateFormat("yyyy-MM-dd").format(alarmDateObject);
                edtAlarmDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(alarmDateObject));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (starResponseModel.getAlarm_time() != null && !starResponseModel.getAlarm_time().isEmpty()) {
            try {
                alarmTimeObject = new SimpleDateFormat("HH:mm").parse(starResponseModel.getAlarm_time());
                formattedAlarmTime = new SimpleDateFormat("HH:mm").format(alarmTimeObject);
                edtAlarmTime.setText(new SimpleDateFormat("hh:mm a").format(alarmTimeObject));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (starResponseModel.getPriority() != null && !starResponseModel.getPriority().isEmpty())
            prioritySeekBar.setValue(Float.parseFloat(starResponseModel.getPriority()));


        if (starResponseModel.getNotes() != null && !starResponseModel.getNotes().isEmpty())
            accessNotes = starResponseModel.getNotes();

        if (starResponseModel.getDocuments() != null && starResponseModel.getDocuments().size() > 0) {
            documentList.clear();
            documentList.addAll(starResponseModel.getDocuments());
            taskDocumentAdapter.notifyDataSetChanged();

            documentModels.clear();
            for(int i=0;i<documentList.size();i++)
            documentModels.add(new DocumentModel(documentList.get(i).getDocument_url(),documentList.get(i).getDocument_type()));
        }

        if (starResponseModel.getMembers() != null && starResponseModel.getMembers().size() > 0) {
            memberResponseList.clear();
            memberResponseList.addAll(starResponseModel.getMembers());


            if(memberResponseList!=null && memberResponseList.size()>0) {
                StringBuilder stringBuilder = new StringBuilder();
                memberRequestList.clear();
                for (int i = 0; i < memberResponseList.size(); i++) {
                     memberRequestList.add(new MemberRequestModel(memberResponseList.get(i).getMember_name(),memberResponseList.get(i).getTo_email_id()));
                    stringBuilder.append(memberResponseList.get(i).getMember_name());
                    stringBuilder.append(",");
                }
                edtAddMember.setText(String.valueOf(stringBuilder).substring(0, String.valueOf(stringBuilder).length() - 1));
            }
        }
    }


    private void setDataForPlanet() {
        if (planetResponseModel.getName() != null && !planetResponseModel.getName().isEmpty())
            edtName.setText(planetResponseModel.getName());

        if (planetResponseModel.getDescription() != null && !planetResponseModel.getDescription().isEmpty())
            edtDesc.setText(planetResponseModel.getDescription());

        if (planetResponseModel.getStart_date() != null && !planetResponseModel.getStart_date().isEmpty()) {
            try {
                startDateObject = new SimpleDateFormat("yyyy-MM-dd").parse(planetResponseModel.getStart_date());
                formattedStartDate = new SimpleDateFormat("yyyy-MM-dd").format(startDateObject);
                edtStartDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(startDateObject));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (planetResponseModel.getStart_time() != null && !planetResponseModel.getStart_time().isEmpty()) {
            try {
                startTimeObject = new SimpleDateFormat("HH:mm").parse(planetResponseModel.getStart_time());
                formattedStartTime = new SimpleDateFormat("HH:mm").format(startTimeObject);
                edtStartTime.setText(new SimpleDateFormat("hh:mm a").format(startTimeObject));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (planetResponseModel.getFinish_date() != null && !planetResponseModel.getFinish_date().isEmpty()) {
            try {
                finishDateObject = new SimpleDateFormat("yyyy-MM-dd").parse(planetResponseModel.getFinish_date());
                formattedFinishDate = new SimpleDateFormat("yyyy-MM-dd").format(finishDateObject);
                edtFinishDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(finishDateObject));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (planetResponseModel.getFinish_time() != null && !planetResponseModel.getFinish_time().isEmpty()) {
            try {
                finishTimeObject = new SimpleDateFormat("HH:mm").parse(planetResponseModel.getFinish_time());
                formattedFinishTime = new SimpleDateFormat("HH:mm").format(finishTimeObject);
                edtFinishTime.setText(new SimpleDateFormat("hh:mm a").format(finishTimeObject));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (planetResponseModel.getAlarm_date() != null && !planetResponseModel.getAlarm_date().isEmpty()) {
            try {
                alarmDateObject = new SimpleDateFormat("yyyy-MM-dd").parse(planetResponseModel.getAlarm_date());
                formattedAlarmDate = new SimpleDateFormat("yyyy-MM-dd").format(alarmDateObject);
                edtAlarmDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(alarmDateObject));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (planetResponseModel.getAlarm_time() != null && !planetResponseModel.getAlarm_time().isEmpty()) {
            try {
                alarmTimeObject = new SimpleDateFormat("HH:mm").parse(planetResponseModel.getAlarm_time());
                formattedAlarmTime = new SimpleDateFormat("HH:mm").format(alarmTimeObject);
                edtAlarmTime.setText(new SimpleDateFormat("hh:mm a").format(alarmTimeObject));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (planetResponseModel.getPriority() != null && !planetResponseModel.getPriority().isEmpty())
            prioritySeekBar.setValue(Float.parseFloat(planetResponseModel.getPriority()));


        if (planetResponseModel.getNotes() != null && !planetResponseModel.getNotes().isEmpty())
            accessNotes = planetResponseModel.getNotes();

        if (planetResponseModel.getDocuments() != null && planetResponseModel.getDocuments().size() > 0) {
            documentList.clear();
            documentList.addAll(planetResponseModel.getDocuments());
            taskDocumentAdapter.notifyDataSetChanged();

            documentModels.clear();
            for(int i=0;i<documentList.size();i++)
                documentModels.add(new DocumentModel(documentList.get(i).getDocument_url(),documentList.get(i).getDocument_type()));
        }

        if (planetResponseModel.getMembers() != null && planetResponseModel.getMembers().size() > 0) {
            memberResponseList.clear();
            memberResponseList.addAll(planetResponseModel.getMembers());

            if(memberResponseList!=null && memberResponseList.size()>0) {
                StringBuilder stringBuilder = new StringBuilder();
                memberRequestList.clear();
                for (int i = 0; i < memberResponseList.size(); i++) {
                    memberRequestList.add(new MemberRequestModel(memberResponseList.get(i).getMember_name(),memberResponseList.get(i).getTo_email_id()));
                    stringBuilder.append(memberResponseList.get(i).getMember_name());
                    stringBuilder.append(",");
                }
                edtAddMember.setText(String.valueOf(stringBuilder).substring(0, String.valueOf(stringBuilder).length() - 1));
            }
        }

    }

    private void setDataForMoon() {
        if (moonResponseModel.getName() != null && !moonResponseModel.getName().isEmpty())
            edtName.setText(moonResponseModel.getName());

        if (moonResponseModel.getDescription() != null && !moonResponseModel.getDescription().isEmpty())
            edtDesc.setText(moonResponseModel.getDescription());

        if (moonResponseModel.getStart_date() != null && !moonResponseModel.getStart_date().isEmpty()) {
            try {
                startDateObject = new SimpleDateFormat("yyyy-MM-dd").parse(moonResponseModel.getStart_date());
                formattedStartDate = new SimpleDateFormat("yyyy-MM-dd").format(startDateObject);
                edtStartDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(startDateObject));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (moonResponseModel.getStart_time() != null && !moonResponseModel.getStart_time().isEmpty()) {
            try {
                startTimeObject = new SimpleDateFormat("HH:mm").parse(moonResponseModel.getStart_time());
                formattedStartTime = new SimpleDateFormat("HH:mm").format(startTimeObject);
                edtStartTime.setText(new SimpleDateFormat("hh:mm a").format(startTimeObject));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (moonResponseModel.getFinish_date() != null && !moonResponseModel.getFinish_date().isEmpty()) {
            try {
                finishDateObject = new SimpleDateFormat("yyyy-MM-dd").parse(moonResponseModel.getFinish_date());
                formattedFinishDate = new SimpleDateFormat("yyyy-MM-dd").format(finishDateObject);
                edtFinishDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(finishDateObject));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (moonResponseModel.getFinish_time() != null && !moonResponseModel.getFinish_time().isEmpty()) {
            try {
                finishTimeObject = new SimpleDateFormat("HH:mm").parse(moonResponseModel.getFinish_time());
                formattedFinishTime = new SimpleDateFormat("HH:mm").format(finishTimeObject);
                edtFinishTime.setText(new SimpleDateFormat("hh:mm a").format(finishTimeObject));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (moonResponseModel.getAlarm_date() != null && !moonResponseModel.getAlarm_date().isEmpty()) {
            try {
                alarmDateObject = new SimpleDateFormat("yyyy-MM-dd").parse(moonResponseModel.getAlarm_date());
                formattedAlarmDate = new SimpleDateFormat("yyyy-MM-dd").format(alarmDateObject);
                edtAlarmDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(alarmDateObject));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (moonResponseModel.getAlarm_time() != null && !moonResponseModel.getAlarm_time().isEmpty()) {
            try {

                alarmTimeObject = new SimpleDateFormat("HH:mm").parse(moonResponseModel.getAlarm_time());
                formattedAlarmTime = new SimpleDateFormat("HH:mm").format(alarmTimeObject);
                edtAlarmTime.setText(new SimpleDateFormat("hh:mm a").format(alarmTimeObject));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (moonResponseModel.getPriority() != null && !moonResponseModel.getPriority().isEmpty())
            prioritySeekBar.setValue(Float.parseFloat(moonResponseModel.getPriority()));


        if (moonResponseModel.getNotes() != null && !moonResponseModel.getNotes().isEmpty())
            accessNotes = moonResponseModel.getNotes();

        if (moonResponseModel.getDocuments() != null && moonResponseModel.getDocuments().size() > 0) {
            documentList.clear();
            documentList.addAll(moonResponseModel.getDocuments());
            taskDocumentAdapter.notifyDataSetChanged();

            documentModels.clear();
            for(int i=0;i<documentList.size();i++)
                documentModels.add(new DocumentModel(documentList.get(i).getDocument_url(),documentList.get(i).getDocument_type()));
        }

        if (moonResponseModel.getMembers() != null && moonResponseModel.getMembers().size() > 0) {
            memberResponseList.clear();
            memberResponseList.addAll(moonResponseModel.getMembers());

            if(memberResponseList!=null && memberResponseList.size()>0) {
                StringBuilder stringBuilder = new StringBuilder();
                memberRequestList.clear();
                for (int i = 0; i < memberResponseList.size(); i++) {
                    memberRequestList.add(new MemberRequestModel(memberResponseList.get(i).getMember_name(),memberResponseList.get(i).getTo_email_id()));
                    stringBuilder.append(memberResponseList.get(i).getMember_name());
                    stringBuilder.append(",");
                }
                edtAddMember.setText(String.valueOf(stringBuilder).substring(0, String.valueOf(stringBuilder).length() - 1));
            }
        }
    }

    private void setDataForSatellite() {
        if (satelliteResponseModel.getName() != null && !satelliteResponseModel.getName().isEmpty())
            edtName.setText(satelliteResponseModel.getName());

        if (satelliteResponseModel.getDescription() != null && !satelliteResponseModel.getDescription().isEmpty())
            edtDesc.setText(satelliteResponseModel.getDescription());

        if (satelliteResponseModel.getStart_date() != null && !satelliteResponseModel.getStart_date().isEmpty()) {
            try {
                startDateObject = new SimpleDateFormat("yyyy-MM-dd").parse(satelliteResponseModel.getStart_date());
                formattedStartDate = new SimpleDateFormat("yyyy-MM-dd").format(startDateObject);
                edtStartDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(startDateObject));
                edtStartDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(satelliteResponseModel.getStart_date())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (satelliteResponseModel.getStart_time() != null && !satelliteResponseModel.getStart_time().isEmpty()) {
            try {
                startTimeObject = new SimpleDateFormat("HH:mm").parse(satelliteResponseModel.getStart_time());
                formattedStartTime = new SimpleDateFormat("HH:mm").format(startTimeObject);
                edtStartTime.setText(new SimpleDateFormat("hh:mm a").format(startTimeObject));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (satelliteResponseModel.getFinish_date() != null && !satelliteResponseModel.getFinish_date().isEmpty()) {
            try {
                finishDateObject = new SimpleDateFormat("yyyy-MM-dd").parse(satelliteResponseModel.getFinish_date());
                formattedFinishDate = new SimpleDateFormat("yyyy-MM-dd").format(finishDateObject);
                edtFinishDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(finishDateObject));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (satelliteResponseModel.getFinish_time() != null && !satelliteResponseModel.getFinish_time().isEmpty()) {
            try {
                finishTimeObject = new SimpleDateFormat("HH:mm").parse(satelliteResponseModel.getFinish_time());
                formattedFinishTime = new SimpleDateFormat("HH:mm").format(finishTimeObject);
                edtFinishTime.setText(new SimpleDateFormat("hh:mm a").format(finishTimeObject));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        if (satelliteResponseModel.getAlarm_date() != null && !satelliteResponseModel.getAlarm_date().isEmpty()) {
            try {
                alarmDateObject = new SimpleDateFormat("yyyy-MM-dd").parse(satelliteResponseModel.getAlarm_date());
                formattedAlarmDate = new SimpleDateFormat("yyyy-MM-dd").format(alarmDateObject);
                edtAlarmDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(alarmDateObject));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (satelliteResponseModel.getAlarm_time() != null && !satelliteResponseModel.getAlarm_time().isEmpty()) {
            try {
                alarmTimeObject = new SimpleDateFormat("HH:mm").parse(satelliteResponseModel.getAlarm_time());
                formattedAlarmTime = new SimpleDateFormat("HH:mm").format(alarmTimeObject);
                edtAlarmTime.setText(new SimpleDateFormat("hh:mm a").format(alarmTimeObject));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (satelliteResponseModel.getPriority() != null && !satelliteResponseModel.getPriority().isEmpty())
            prioritySeekBar.setValue(Float.parseFloat(satelliteResponseModel.getPriority()));


        if (satelliteResponseModel.getNotes() != null && !satelliteResponseModel.getNotes().isEmpty())
            accessNotes = starResponseModel.getNotes();

        if (satelliteResponseModel.getDocuments() != null && satelliteResponseModel.getDocuments().size() > 0) {
            documentList.clear();
            documentList.addAll(satelliteResponseModel.getDocuments());
            taskDocumentAdapter.notifyDataSetChanged();

            documentModels.clear();
            for(int i=0;i<documentList.size();i++)
                documentModels.add(new DocumentModel(documentList.get(i).getDocument_url(),documentList.get(i).getDocument_type()));
        }

        if (satelliteResponseModel.getMembers() != null && satelliteResponseModel.getMembers().size() > 0) {
            memberResponseList.clear();
            memberResponseList.addAll(satelliteResponseModel.getMembers());

            if(memberResponseList!=null && memberResponseList.size()>0) {
                StringBuilder stringBuilder = new StringBuilder();
                memberRequestList.clear();
                for (int i = 0; i < memberResponseList.size(); i++) {
                    memberRequestList.add(new MemberRequestModel(memberResponseList.get(i).getMember_name(),memberResponseList.get(i).getTo_email_id()));
                    stringBuilder.append(memberResponseList.get(i).getMember_name());
                    stringBuilder.append(",");
                }
                edtAddMember.setText(String.valueOf(stringBuilder).substring(0, String.valueOf(stringBuilder).length() - 1));
            }
        }

    }

   /* private void setDataForComet() {
        if (cometResponseModel.getName() != null && !cometResponseModel.getName().isEmpty())
            edtName.setText(cometResponseModel.getName());

        if (cometResponseModel.getDescription() != null && !cometResponseModel.getDescription().isEmpty())
            edtDesc.setText(cometResponseModel.getDescription());

        if (cometResponseModel.getStart_date() != null && !cometResponseModel.getStart_date().isEmpty()) {
            try {
                startDateObject = new SimpleDateFormat("yyyy-MM-dd").parse(cometResponseModel.getStart_date());
                formattedStartDate = new SimpleDateFormat("yyyy-MM-dd").format(startDateObject);
                edtStartDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(startDateObject));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (cometResponseModel.getStart_time() != null && !cometResponseModel.getStart_time().isEmpty()) {
            try {
                startTimeObject = new SimpleDateFormat("HH:mm").parse(cometResponseModel.getStart_time());
                formattedStartTime = new SimpleDateFormat("HH:mm").format(startTimeObject);
                edtStartTime.setText(new SimpleDateFormat("hh:mm a").format(startTimeObject));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (cometResponseModel.getFinish_date() != null && !cometResponseModel.getFinish_date().isEmpty()) {
            try {
                finishDateObject = new SimpleDateFormat("yyyy-MM-dd").parse(cometResponseModel.getFinish_date());
                formattedFinishDate = new SimpleDateFormat("yyyy-MM-dd").format(finishDateObject);
                edtFinishDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(finishDateObject));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (cometResponseModel.getFinish_time() != null && !cometResponseModel.getFinish_time().isEmpty()) {
            try {
                finishTimeObject = new SimpleDateFormat("HH:mm").parse(cometResponseModel.getFinish_time());
                formattedFinishTime = new SimpleDateFormat("HH:mm").format(finishTimeObject);
                edtFinishTime.setText(new SimpleDateFormat("hh:mm a").format(finishTimeObject));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (cometResponseModel.getAlarm_date() != null && !cometResponseModel.getAlarm_date().isEmpty()) {
            try {
                alarmDateObject = new SimpleDateFormat("yyyy-MM-dd").parse(cometResponseModel.getAlarm_date());
                formattedAlarmDate = new SimpleDateFormat("yyyy-MM-dd").format(alarmDateObject);
                edtAlarmDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(alarmDateObject));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (cometResponseModel.getAlarm_time() != null && !cometResponseModel.getAlarm_time().isEmpty()) {
            try {
                alarmTimeObject = new SimpleDateFormat("HH:mm").parse(cometResponseModel.getAlarm_time());
                formattedAlarmTime = new SimpleDateFormat("HH:mm").format(alarmTimeObject);
                edtAlarmTime.setText(new SimpleDateFormat("hh:mm a").format(alarmTimeObject));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (cometResponseModel.getPriority() != null && !cometResponseModel.getPriority().isEmpty())
            prioritySeekBar.setValue(Float.parseFloat(cometResponseModel.getPriority()));


        if (cometResponseModel.getNotes() != null && !cometResponseModel.getNotes().isEmpty())
            accessNotes = cometResponseModel.getNotes();

        if (cometResponseModel.getDocuments() != null && cometResponseModel.getDocuments().size() > 0) {
            documentList.clear();
            documentList.addAll(cometResponseModel.getDocuments());
            taskDocumentAdapter.notifyDataSetChanged();

            documentModels.clear();
            for(int i=0;i<documentList.size();i++)
                documentModels.add(new DocumentModel(documentList.get(i).getDocument_url(),documentList.get(i).getDocument_type()));
        }

        if (cometResponseModel.getMembers() != null && cometResponseModel.getMembers().size() > 0) {
            memberResponseList.clear();
            memberResponseList.addAll(cometResponseModel.getMembers());

            if(memberResponseList!=null && memberResponseList.size()>0) {
                StringBuilder stringBuilder = new StringBuilder();
                memberRequestList.clear();
                for (int i = 0; i < memberResponseList.size(); i++) {
                    memberRequestList.add(new MemberRequestModel(memberResponseList.get(i).getMember_name(),memberResponseList.get(i).getTo_email_id()));
                    stringBuilder.append(memberResponseList.get(i).getMember_name());
                    stringBuilder.append(",");
                }
                edtAddMember.setText(String.valueOf(stringBuilder).substring(0, String.valueOf(stringBuilder).length() - 1));
            }
        }
    }*/


    private void callDeleteDocumentApi(final int position, String documentId, String documentUrl) {
      progressDialog.show();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetMessageModel> deleteDocumentResponseCall=apiService.callDeleteDocument(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN,""),documentId,documentUrl);
        deleteDocumentResponseCall.enqueue(new Callback<GetMessageModel>() {
            @Override
            public void onResponse(Call<GetMessageModel> call, Response<GetMessageModel> response) {
                progressDialog.dismiss();
                if(response.code()==200 && response.body()!=null)
                {
                    if(response.body().getMessage()!=null)
                        AndroidUtils.showToast(AddTaskActivity.this,response.body().getMessage());

                    documentList.remove(position);
                    taskDocumentAdapter.notifyDataSetChanged();
                    documentModels.remove(position);
                }else if(response.code()==500)
                {
                    AndroidUtils.showToast(AddTaskActivity.this,getResources().getString(R.string.some_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<GetMessageModel> call, Throwable t) {
               progressDialog.dismiss();
                AndroidUtils.showToast(AddTaskActivity.this,getResources().getString(R.string.some_went_wrong));
            }
        });

    }

    @Override
    public void onDocumentClick(int position) {
     Intent intent=new Intent(AddTaskActivity.this,DocumentPreviewActivity.class);
     intent.putExtra(ConstantUtils.DOCUMENT_MODEL,documentList.get(position));
     startActivity(intent);
    }

    @Override
    public void onRemoveClick(int position) {
        if(ConnectivityController.isNetworkAvailable(AddTaskActivity.this))
            callDeleteDocumentApi(position,documentList.get(position).getDocument_id(),documentList.get(position).getDocument_url());
        else AndroidUtils.showToast(AddTaskActivity.this,getResources().getString(R.string.no_internet));
    }
}

