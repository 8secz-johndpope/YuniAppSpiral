package com.android.yuniapp.fragment;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.android.yuniapp.R;
import com.android.yuniapp.activity.AddTaskActivity;
import com.android.yuniapp.activity.HomeActivity;
import com.android.yuniapp.listener.DropEntityPopUpListener;
import com.android.yuniapp.model.CometRequestModel;
import com.android.yuniapp.model.CometResponseModel;
import com.android.yuniapp.model.MoonResponseModel;
import com.android.yuniapp.model.PlanetResponseModel;
import com.android.yuniapp.model.SatelliteResponseModel;
import com.android.yuniapp.model.StarRequestModel;
import com.android.yuniapp.model.StarResponseModel;
import com.android.yuniapp.rest.ApiClient;
import com.android.yuniapp.rest.ApiInterface;
import com.android.yuniapp.utils.AndroidUtils;
import com.android.yuniapp.utils.AppUtils;
import com.android.yuniapp.utils.ConnectivityController;
import com.android.yuniapp.utils.ConstantUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DropEntityPopUpFragment extends DialogFragment implements View.OnClickListener, View.OnTouchListener {
    private View view;
    private TextView txtTaskName, txtTaskCurrentFinishDate, txtTaskNewFinishDate;
    private StarResponseModel starResponseModel;
    private PlanetResponseModel planetResponseModel;
    private MoonResponseModel moonResponseModel;
    private SatelliteResponseModel satelliteResponseModel;
    private CometResponseModel cometResponseModel;
    private int orbitNo;
    private EditText edtFinishTime;
    private String formattedFinishTime, formattedFinishDate;
    private Date finishTimeObject;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private DropEntityPopUpListener dropEntityPopUpListener;
    private Dialog progressDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_drop_entity_popup, container, false);
        progressDialog = new Dialog(getActivity());
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setContentView(R.layout.fragment_loader);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        sharedPreferences = getActivity().getSharedPreferences(ConstantUtils.APP_PREF, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        txtTaskName = view.findViewById(R.id.txt_task_name);
        txtTaskCurrentFinishDate = view.findViewById(R.id.txt_task_current_finish_date);
        txtTaskNewFinishDate = view.findViewById(R.id.txt_task_new_finish_date);
        view.findViewById(R.id.img_close).setOnClickListener(this);
        view.findViewById(R.id.layout_save).setOnClickListener(this);
        edtFinishTime = view.findViewById(R.id.edt_finish_time);
        edtFinishTime.setOnTouchListener(this);
        if (getArguments() != null) {
            //   taskPosition=getArguments().getInt(ConstantUtils.TASK_POSITION);

            orbitNo = getArguments().getInt(ConstantUtils.ORBIT_NO);


            if(getArguments().getString("formattedNewFinishDate")!=null)
            {
                formattedFinishDate=getArguments().getString("formattedNewFinishDate");
                try {

                    txtTaskNewFinishDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(formattedFinishDate)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (getArguments().getParcelable(ConstantUtils.STAR_RESPONSE_MODEL) != null) {
                starResponseModel = getArguments().getParcelable(ConstantUtils.STAR_RESPONSE_MODEL);
                setStarData();
            } else if (getArguments().getParcelable(ConstantUtils.PLANET_RESPONSE_MODEL) != null) {
                planetResponseModel = getArguments().getParcelable(ConstantUtils.PLANET_RESPONSE_MODEL);
                setPlanetData();
            } else if (getArguments().getParcelable(ConstantUtils.MOON_RESPONSE_MODEL) != null) {
                moonResponseModel = getArguments().getParcelable(ConstantUtils.MOON_RESPONSE_MODEL);
                setMoonData();
            } else if (getArguments().getParcelable(ConstantUtils.SATELLITE_RESPONSE_MODEL) != null) {
                satelliteResponseModel = getArguments().getParcelable(ConstantUtils.SATELLITE_RESPONSE_MODEL);
                setSatelliteData();
            } else if (getArguments().getParcelable(ConstantUtils.COMET_RESPONSE_MODEL) != null) {
                cometResponseModel = getArguments().getParcelable(ConstantUtils.COMET_RESPONSE_MODEL);
                setCometData();
            }

        }


        return view;
    }

    private void setStarData() {

        if (starResponseModel.getName() != null && !starResponseModel.getName().isEmpty())
            txtTaskName.setText(starResponseModel.getName());

        if (starResponseModel.getFinish_date() != null && !starResponseModel.getFinish_date().isEmpty()) {
            try {

                txtTaskCurrentFinishDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(starResponseModel.getFinish_date())));
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
    }

    private void setPlanetData() {

        if (planetResponseModel.getName() != null && !planetResponseModel.getName().isEmpty())
            txtTaskName.setText(planetResponseModel.getName());

        if (planetResponseModel.getFinish_date() != null && !planetResponseModel.getFinish_date().isEmpty()) {
            try {

                txtTaskCurrentFinishDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(planetResponseModel.getFinish_date())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setMoonData() {

        if (moonResponseModel.getName() != null && !moonResponseModel.getName().isEmpty())
            txtTaskName.setText(moonResponseModel.getName());


        if (moonResponseModel.getFinish_date() != null && !moonResponseModel.getFinish_date().isEmpty()) {
            try {

                txtTaskCurrentFinishDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(moonResponseModel.getFinish_date())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setSatelliteData() {

        if (satelliteResponseModel.getName() != null && !satelliteResponseModel.getName().isEmpty())
            txtTaskName.setText(satelliteResponseModel.getName());


        if (satelliteResponseModel.getFinish_date() != null && !satelliteResponseModel.getFinish_date().isEmpty()) {
            try {

                txtTaskCurrentFinishDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(satelliteResponseModel.getFinish_date())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setCometData() {
        if (cometResponseModel.getName() != null && !cometResponseModel.getName().isEmpty())
            txtTaskName.setText(cometResponseModel.getName());


        if (cometResponseModel.getFinish_date() != null && !cometResponseModel.getFinish_date().isEmpty()) {
            try {

                txtTaskCurrentFinishDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(cometResponseModel.getFinish_date())));
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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_close:
                dropEntityPopUpListener.onCloseClick();
                dismiss();
                break;
            case R.id.layout_save:
                if (ConnectivityController.isNetworkAvailable(getActivity())) {
                    if (starResponseModel != null) {

                        StarRequestModel starRequestModel = new StarRequestModel();
                        starRequestModel.setStar_id(starResponseModel.getId());
                        starRequestModel.setName(starResponseModel.getName());
                        starRequestModel.setDescription(starResponseModel.getDescription());
                        starRequestModel.setStart_date(starResponseModel.getStart_date());
                        starRequestModel.setStart_time(starResponseModel.getStart_time());
                        starRequestModel.setFinish_date(formattedFinishDate);
                        starRequestModel.setFinish_time(formattedFinishTime);
                        starRequestModel.setPriority(starResponseModel.getPriority());
                        starRequestModel.setAlarm_time(starResponseModel.getAlarm_time());
                        starRequestModel.setAlarm_date(starResponseModel.getAlarm_date());
                        // starRequestModel.setMembers(starResponseModel.getMembers());
                        //starRequestModel.setDocuments(starResponseModel.getDocuments());
                        starRequestModel.setNotes(starResponseModel.getNotes());


                        callUpdateStarApi(starRequestModel);
                    } else if (cometResponseModel != null) {
                        CometRequestModel cometRequestModel = new CometRequestModel();
                        cometRequestModel.setComet_id(cometResponseModel.getComet_id());
                        cometRequestModel.setName(cometResponseModel.getName());
                        cometRequestModel.setDescription(cometResponseModel.getDescription());
                        cometRequestModel.setStart_date(cometResponseModel.getStart_date());
                        cometRequestModel.setStart_time(cometResponseModel.getStart_time());
                        cometRequestModel.setFinish_date(formattedFinishDate);
                        cometRequestModel.setFinish_time(formattedFinishTime);
                        cometRequestModel.setPriority(cometResponseModel.getPriority());
                        cometRequestModel.setAlarm_time(cometResponseModel.getAlarm_time());
                        cometRequestModel.setAlarm_date(cometResponseModel.getAlarm_date());
                        cometRequestModel.setNotes(cometResponseModel.getNotes());
                        // starRequestModel.setMembers(starResponseModel.getMembers());
                        //starRequestModel.setDocuments(starResponseModel.getDocuments());
                            callUpdateCometApi(cometRequestModel);

                    }
                }else
                    AndroidUtils.showToast(getActivity(), getActivity().getResources().getString(R.string.no_internet));
                break;
        }

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.ACTION_UP == event.getAction()) {
            int mHour = 0, mMinute = 0;
            final Calendar calendar;
            calendar = Calendar.getInstance();
            mHour = calendar.get(Calendar.HOUR_OF_DAY);
            mMinute = calendar.get(Calendar.MINUTE);
            TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                            try {

                                finishTimeObject = new SimpleDateFormat("HH:mm").parse(String.valueOf(hourOfDay) + ":" + String.valueOf(minute));
                                formattedFinishTime = new SimpleDateFormat("HH:mm").format(finishTimeObject);
                                edtFinishTime.setText(new SimpleDateFormat("hh:mm a").format(finishTimeObject));


                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }, mHour, mMinute, false);
            timePickerDialog.show();
        }
        return true;
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
                    AndroidUtils.showToast(getActivity(), "Star has been updated successfully");
                    dropEntityPopUpListener.afterUpdate();
                    dismiss();


                } else if (response.code() == 500) {
                    AndroidUtils.showToast(getActivity(), getResources().getString(R.string.some_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<StarResponseModel> call, Throwable t) {
                progressDialog.dismiss();
                AndroidUtils.showToast(getActivity(), getResources().getString(R.string.some_went_wrong));
            }
        });
    }

    public void setDropEntityPopUpListener(DropEntityPopUpListener dropEntityPopUpListener) {
        this.dropEntityPopUpListener = dropEntityPopUpListener;
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
                    AndroidUtils.showToast(getActivity(), "Comet has been updated successfully");
                    dropEntityPopUpListener.afterUpdate();
                    dismiss();
                }  else if (response.code() == 500) {
                    AndroidUtils.showToast(getActivity(), getResources().getString(R.string.some_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<CometResponseModel> call, Throwable t) {
                progressDialog.dismiss();
                AndroidUtils.showToast(getActivity(), getResources().getString(R.string.some_went_wrong));
            }
        });
    }
}
