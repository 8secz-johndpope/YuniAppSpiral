package com.android.yuniapp.fragment;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.yuniapp.R;
import com.android.yuniapp.activity.ChatActivity;
import com.android.yuniapp.application.YuniApplication;
import com.android.yuniapp.listener.InfoPopUpEventListener;
import com.android.yuniapp.model.CometResponseModel;
import com.android.yuniapp.model.MoonResponseModel;
import com.android.yuniapp.model.PlanetResponseModel;
import com.android.yuniapp.model.SatelliteResponseModel;
import com.android.yuniapp.model.StarResponseModel;
import com.android.yuniapp.utils.AppUtils;
import com.android.yuniapp.utils.ConstantUtils;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import io.agora.openvcall.model.ConstantApp;
import io.agora.openvcall.ui.CallActivityAgora;

public class InfoPopUpFragment extends DialogFragment implements View.OnClickListener {
    private View view;
    private TextView txtTaskName, txtTaskStartDate, txtTaskFinishDate;
    private StarResponseModel starResponseModel;
    private PlanetResponseModel planetResponseModel;
    private MoonResponseModel moonResponseModel;
    private SatelliteResponseModel satelliteResponseModel;
    private CometResponseModel cometResponseModel;
    private InfoPopUpEventListener infoPopUpEventListener;
    private int taskPosition;
    private ImageView imgStorage, imgViewDetails;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_info_popup, container, false);

        txtTaskName = view.findViewById(R.id.txt_task_name);
        txtTaskStartDate = view.findViewById(R.id.txt_task_start_date);
        txtTaskFinishDate = view.findViewById(R.id.txt_task_finish_date);
        view.findViewById(R.id.img_close).setOnClickListener(this);
        view.findViewById(R.id.img_edit).setOnClickListener(this);
        view.findViewById(R.id.img_delete).setOnClickListener(this);
        view.findViewById(R.id.img_video_call).setOnClickListener(this);
        view.findViewById(R.id.img_chat).setOnClickListener(this);
        imgViewDetails = view.findViewById(R.id.img_view_details);
        imgViewDetails.setOnClickListener(this);
        imgStorage = view.findViewById(R.id.img_storage);
        imgStorage.setOnClickListener(this);
        view.findViewById(R.id.txt_done_mark).setOnClickListener(this);

        if (getArguments() != null) {
            taskPosition = getArguments().getInt(ConstantUtils.TASK_POSITION);

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
        imgStorage.setVisibility(View.VISIBLE);
        //imgViewDetails.setVisibility(View.VISIBLE);
        if (starResponseModel.getName() != null && !starResponseModel.getName().isEmpty())
            txtTaskName.setText(starResponseModel.getName());

        if (starResponseModel.getStart_date() != null && !starResponseModel.getStart_date().isEmpty()) {
            try {

                txtTaskStartDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(starResponseModel.getStart_date())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (starResponseModel.getFinish_date() != null && !starResponseModel.getFinish_date().isEmpty()) {
            try {

                txtTaskFinishDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(starResponseModel.getFinish_date())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setPlanetData() {
        imgStorage.setVisibility(View.GONE);
        //imgViewDetails.setVisibility(View.VISIBLE);
        if (planetResponseModel.getName() != null && !planetResponseModel.getName().isEmpty())
            txtTaskName.setText(planetResponseModel.getName());

        if (planetResponseModel.getStart_date() != null && !planetResponseModel.getStart_date().isEmpty()) {
            try {

                txtTaskStartDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(planetResponseModel.getStart_date())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (planetResponseModel.getFinish_date() != null && !planetResponseModel.getFinish_date().isEmpty()) {
            try {

                txtTaskFinishDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(planetResponseModel.getFinish_date())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setMoonData() {
        imgStorage.setVisibility(View.GONE);
        // imgViewDetails.setVisibility(View.VISIBLE);
        if (moonResponseModel.getName() != null && !moonResponseModel.getName().isEmpty())
            txtTaskName.setText(moonResponseModel.getName());

        if (moonResponseModel.getStart_date() != null && !moonResponseModel.getStart_date().isEmpty()) {
            try {

                txtTaskStartDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(moonResponseModel.getStart_date())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (moonResponseModel.getFinish_date() != null && !moonResponseModel.getFinish_date().isEmpty()) {
            try {

                txtTaskFinishDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(moonResponseModel.getFinish_date())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setSatelliteData() {
        imgStorage.setVisibility(View.GONE);
        // imgViewDetails.setVisibility(View.VISIBLE);
        if (satelliteResponseModel.getName() != null && !satelliteResponseModel.getName().isEmpty())
            txtTaskName.setText(satelliteResponseModel.getName());

        if (satelliteResponseModel.getStart_date() != null && !satelliteResponseModel.getStart_date().isEmpty()) {
            try {

                txtTaskStartDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(satelliteResponseModel.getStart_date())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (satelliteResponseModel.getFinish_date() != null && !satelliteResponseModel.getFinish_date().isEmpty()) {
            try {

                txtTaskFinishDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(satelliteResponseModel.getFinish_date())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setCometData() {
        imgStorage.setVisibility(View.GONE);
        //imgViewDetails.setVisibility(View.VISIBLE);
        if (cometResponseModel.getName() != null && !cometResponseModel.getName().isEmpty())
            txtTaskName.setText(cometResponseModel.getName());

        if (cometResponseModel.getStart_date() != null && !cometResponseModel.getStart_date().isEmpty()) {
            try {

                txtTaskStartDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(cometResponseModel.getStart_date())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (cometResponseModel.getFinish_date() != null && !cometResponseModel.getFinish_date().isEmpty()) {
            try {

                txtTaskFinishDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(cometResponseModel.getFinish_date())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_close:
                dismiss();
                break;
            case R.id.img_video_call:
//                VideoCallModel model = new VideoCallModel();
//                model.setChannelId("123");
//                startActivity(new Intent(getActivity(), VideoCallActivity.class)
//                        .putExtra(ConstantUtils.VIDEO_CALL_MODEL, model));

                checkPermissionsForVideoCall();
                break;
            case R.id.img_chat:
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra(ConstantUtils.ENTITY_ID, starResponseModel.getId());
                intent.putExtra(ConstantUtils.ENTITY_TYPE, "S");
                startActivity(intent);
                break;
            case R.id.img_edit:
                infoPopUpEventListener.onEditTaskClick(taskPosition, "");
                break;
            case R.id.img_delete:
                infoPopUpEventListener.onDeleteTaskClick(taskPosition, "");
                break;
            case R.id.img_storage:
                infoPopUpEventListener.onStorageTaskClick(taskPosition);
                break;
            case R.id.img_view_details:
                infoPopUpEventListener.onDetailsClick(taskPosition, "");
                break;
            case R.id.txt_done_mark:
                infoPopUpEventListener.onMarkDoneClick(taskPosition, "");
                break;
        }
    }

    private void checkPermissionsForVideoCall() {
        String[] PERMISSIONS;
        ArrayList<String> permissionList = new ArrayList<>();
        permissionList.add(Manifest.permission.CAMERA);
        permissionList.add(Manifest.permission.RECORD_AUDIO);
        permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (AppUtils.checkWriteExternalStoragePermission(getActivity())) {
            permissionList.remove(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (AppUtils.checkCameraPermission(getActivity())) {
            permissionList.remove(Manifest.permission.CAMERA);
        }
        if (AppUtils.checkRecordAudio(getActivity())) {
            permissionList.remove(Manifest.permission.RECORD_AUDIO);
        }
        PERMISSIONS = new String[permissionList.size()];
        PERMISSIONS = permissionList.toArray(PERMISSIONS);
        if (permissionList.size() == 0) {
            openVideoCallActivity();
        } else
            Dexter.withActivity(getActivity())
                    .withPermissions(PERMISSIONS)
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport report) {
                            if (report.areAllPermissionsGranted()) {
                                openVideoCallActivity();
                            } else {
                                if (report.getDeniedPermissionResponses().size() > 0) {
                                    AppUtils.showPermissionDeniedToast(getActivity(), report.getDeniedPermissionResponses().get(0).getPermissionName());
//                                    finish();
                                    return;
                                }
                            }
                            if (report.isAnyPermissionPermanentlyDenied()) {
                                AppUtils.showPermanentPermissionDeniedToast(getActivity(), report.getDeniedPermissionResponses().get(0).getPermissionName());
//                                finish();
                            }


                        }


                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    })
                    .onSameThread().check();
    }

    private void openVideoCallActivity() {
        Intent i = new Intent(getActivity(), CallActivityAgora.class);
        i.putExtra(ConstantApp.ACTION_KEY_CHANNEL_NAME, starResponseModel.getId() + "" + "S" + "_video");
        i.putExtra(ConstantApp.ACTION_KEY_ENCRYPTION_KEY, "");
        i.putExtra(ConstantApp.IS_HOST, true);
        i.putExtra(ConstantUtils.ENTITY_ID, starResponseModel.getId());
        i.putExtra(ConstantUtils.ENTITY_TYPE, "S");
        i.putExtra(ConstantApp.ACTION_KEY_ENCRYPTION_MODE,
                getResources()
                        .getStringArray(R.array.encryption_mode_values)[YuniApplication.mVideoSettings.mEncryptionModeIndex]);
        startActivity(i);
    }

    public void setInfoPopUpListener(InfoPopUpEventListener infoPopUpListener) {
        this.infoPopUpEventListener = infoPopUpListener;
    }
}
