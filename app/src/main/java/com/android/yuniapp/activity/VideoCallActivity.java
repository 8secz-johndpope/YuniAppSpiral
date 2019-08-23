package com.android.yuniapp.activity;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.yuniapp.R;
import com.android.yuniapp.model.VideoCallModel;
import com.android.yuniapp.utils.AndroidUtils;
import com.android.yuniapp.utils.AppUtils;
import com.android.yuniapp.utils.ConstantUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.makeramen.roundedimageview.RoundedImageView;
import com.skyfishjy.library.RippleBackground;

import java.util.ArrayList;
import java.util.List;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

import static com.android.yuniapp.utils.ConstantUtils.VIDEOCALLDURATION;


public class VideoCallActivity extends AppCompatActivity {
    private static final String LOG_TAG = VideoCallActivity.class.getSimpleName();
    // Permissions
    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS = {Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};
    private RtcEngine mRtcEngine;
    private RippleBackground rippleBackground;
    private ViewGroup callingLayout, videoLayout;
    private RoundedImageView doctorIcon, userIcon;
    private VideoCallModel videoCallModel = new VideoCallModel();
    private TextView doctorName;
    private String channelID;
    private long timeNotify;
    private Handler handler = new Handler();
    private boolean isLeaved = false;
    private boolean isConnected = false;
    // Handle SDK Events
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // set first remote user to the main bg video container
                    setupRemoteVideoStream(uid);
                }
            });
        }

        // remote user has left channel
        @Override
        public void onUserOffline(int uid, int reason) { // Tutorial Step 7
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRemoteUserLeft();
                }
            });
        }

        // remote user has toggled their video
        @Override
        public void onUserMuteVideo(final int uid, final boolean toggle) { // Tutorial Step 10
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRemoteUserVideoToggle(uid, toggle);
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);
        try {
            videoCallModel = (VideoCallModel) getIntent().getSerializableExtra(ConstantUtils.VIDEO_CALL_MODEL);
            channelID = /*getIntent().getStringExtra(ConstantUtils.CHANNELID);*/ "something_s";
            timeNotify = getIntent().getLongExtra("TIME", System.currentTimeMillis());
            if (System.currentTimeMillis() - timeNotify >= VIDEOCALLDURATION) {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (channelID == null) {
//            IRXEssentialModelApi.getInstance().sendVideoCallNotification(this, videoCallModel.getDoctorId(), channelID);
        }
        callingLayout = findViewById(R.id.callingLayout);
        videoLayout = findViewById(R.id.videoLayout);
        rippleBackground = findViewById(R.id.ripple);
        doctorName = findViewById(R.id.doctorName);
        checkPermissions();
//        if (!AndroidUtils.hasPermissions(this, REQUESTED_PERMISSIONS)) {
//            // We don't have permission so prompt the user
//            ActivityCompat.requestPermissions(this,
//                    REQUESTED_PERMISSIONS,
//                    PERMISSION_REQ_ID
//            );
//        } else {
//
//            initAgoraEngine();
//
//        }
        findViewById(R.id.joinBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onjoinChannelClicked(view);
            }
        });
        findViewById(R.id.audioBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAudioMuteClicked(view);
            }
        });
        findViewById(R.id.leaveBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onLeaveChannelClicked(view);
            }
        });
        findViewById(R.id.videoBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onVideoMuteClicked(view);
            }
        });
        findViewById(R.id.joinBtn).setVisibility(View.GONE);
        findViewById(R.id.audioBtn).setVisibility(View.VISIBLE); // set the audio button hidden
        findViewById(R.id.leaveBtn).setVisibility(View.VISIBLE); // set the leave button hidden
        findViewById(R.id.videoBtn).setVisibility(View.VISIBLE); // set the video button hidden
        initLoadingScreen();
    }

    private void checkPermissions() {
        String[] PERMISSIONS;
        ArrayList<String> permissionList = new ArrayList<>();
        permissionList.add(Manifest.permission.CAMERA);
        permissionList.add(Manifest.permission.RECORD_AUDIO);
//        permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (AppUtils.checkCameraPermission(this)) {
            permissionList.remove(Manifest.permission.CAMERA);
        }
        if (AppUtils.checkRecordAudio(this)) {
//            if (PERMISSIONS[0]!=null){
//                PERMISSIONS[1] = Manifest.permission.READ_EXTERNAL_STORAGE;
//                PERMISSIONS[2] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
//            }else {
//                PERMISSIONS[0] = Manifest.permission.READ_EXTERNAL_STORAGE;
//                PERMISSIONS[1] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
//            }
            permissionList.remove(Manifest.permission.RECORD_AUDIO);
        }
        PERMISSIONS = new String[permissionList.size()];
        PERMISSIONS = permissionList.toArray(PERMISSIONS);
        if (permissionList.size() == 0) {
            initAgoraEngine();
        } else
            Dexter.withActivity(this)
                    .withPermissions(PERMISSIONS)
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport report) {
                            if (report.areAllPermissionsGranted()) {
                                initAgoraEngine();
                            } else {
                                if (report.getDeniedPermissionResponses().size() > 0) {
                                    AppUtils.showPermissionDeniedToast(VideoCallActivity.this, report.getDeniedPermissionResponses().get(0).getPermissionName());
                                    finish();
                                    return;
                                }
                            }
                            if (report.isAnyPermissionPermanentlyDenied()) {
                                AppUtils.showPermanentPermissionDeniedToast(VideoCallActivity.this, report.getDeniedPermissionResponses().get(0).getPermissionName());
                                finish();
                            }


                        }


                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    })
                    .onSameThread().check();
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacksAndMessages(null);
    }

    private void initLoadingScreen() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(22);
        }
        doctorIcon = findViewById(R.id.doctorIcon);
        userIcon = findViewById(R.id.userIcon);
        rippleBackground.startRippleAnimation();
//        onjoinChannelClicked(null);

//        callingLayout.setVisibility(View.GONE);
//        videoLayout.setVisibility(View.VISIBLE);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                onjoinChannelClicked(null);
                userNotAvailable();
            }
        }, VIDEOCALLDURATION);
        /*if (videoCallModel.getMiddleName() == null)
            doctorName.setText(videoCallModel.getFirstName() + " " + videoCallModel.getLastName());
        else
            doctorName.setText(videoCallModel.getFirstName() + " " + videoCallModel.getMiddleName() + " " + videoCallModel.getLastName());*/
        doctorName.setText(videoCallModel.getName());
        setUpImage();
    }

    private void userNotAvailable() {
        if (!isConnected) {
            AndroidUtils.showToast(this, "Members is not available for the call.");
            leaveChannel();
        }
    }

    private void setUpImage() {
        doctorIcon.setBackground(null);
        Glide.with(this)
                .load(videoCallModel.getProfile_pic())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                        doctorIcon.setBackground(getResources().getDrawable(R.drawable.profile_placeholder));
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(doctorIcon);

        doctorIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
        doctorIcon.setBorderColor(Color.parseColor("#00000000"));
        doctorIcon.mutateBackground(true);
        doctorIcon.setOval(true);
//      doctorIcon.setPadding(20, 20, 20, 20);
//        doctorIcon.setBackground(getResources().getDrawable(R.drawable.profile_placeholder_inside));
    }

    private void initAgoraEngine() {
        if (System.currentTimeMillis() - timeNotify >= VIDEOCALLDURATION) {
            AndroidUtils.showToast(this, "Sorry, You have missed the call.");
            finish();
        } else {
            initializeAgoraEngine();
            setupSession();
            onjoinChannelClicked(null);
        }
    }

    private void initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
        } catch (Exception e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));

            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    private void setupSession() {
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
        mRtcEngine.setClientRole(IRtcEngineEventHandler.ClientRole.CLIENT_ROLE_BROADCASTER);
        mRtcEngine.enableVideo();
        mRtcEngine.enableDualStreamMode(true);
        mRtcEngine.setVideoEncoderConfiguration(
                new VideoEncoderConfiguration(
                        VideoEncoderConfiguration.VD_320x240, VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_30,
                        VideoEncoderConfiguration.STANDARD_BITRATE,
                        VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT)
        );
    }

    private void setupLocalVideoFeed() {
        // setup the container for the local user
        FrameLayout videoContainer = findViewById(R.id.floating_video_container);
        SurfaceView videoSurface = RtcEngine.CreateRendererView(getBaseContext());
        videoSurface.setZOrderMediaOverlay(true);
        videoContainer.addView(videoSurface);
        mRtcEngine.setupLocalVideo(new VideoCanvas(videoSurface, VideoCanvas.RENDER_MODE_FIT, 0));
    }

    private void setupRemoteVideoStream(int uid) {
        // setup container for the remote user
        isConnected = true;
        callingLayout.setVisibility(View.GONE);
        videoLayout.setVisibility(View.VISIBLE);
        FrameLayout videoContainer = findViewById(R.id.bg_video_container);

        if (videoContainer.getChildCount() >= 1) {
            return;
        }

        SurfaceView videoSurface = RtcEngine.CreateRendererView(getBaseContext());
        videoContainer.addView(videoSurface);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(videoSurface, VideoCanvas.RENDER_MODE_FIT, uid));
        mRtcEngine.setRemoteSubscribeFallbackOption(io.agora.rtc.Constants.STREAM_FALLBACK_OPTION_AUDIO_ONLY);

    }

    // join the channel when user clicks UI button
    public void onjoinChannelClicked(View view) {
        findViewById(R.id.joinBtn).setVisibility(View.GONE);// set the join button hidden
        findViewById(R.id.audioBtn).setVisibility(View.VISIBLE); // set the audio button hidden
        findViewById(R.id.leaveBtn).setVisibility(View.VISIBLE); // set the leave button hidden
        findViewById(R.id.videoBtn).setVisibility(View.VISIBLE); // set the video button hidden

        try {
            mRtcEngine.joinChannel(null, channelID, "Extra Optional Data", 0); // if you do not specify the uid, Agora will assign one.
            setupLocalVideoFeed();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onLeaveChannelClicked(View view) {
        removeVideo(R.id.floating_video_container);
        removeVideo(R.id.bg_video_container);
        findViewById(R.id.joinBtn).setVisibility(View.GONE); // set the join button hidden
        findViewById(R.id.audioBtn).setVisibility(View.GONE); // set the audio button hidden
        findViewById(R.id.leaveBtn).setVisibility(View.GONE); // set the leave button hidden
        findViewById(R.id.videoBtn).setVisibility(View.GONE); // set the video button hidden
        leaveChannel();
    }

    public void onAudioMuteClicked(View view) {
        ImageView btn = (ImageView) view;
        if (btn.isSelected()) {
            btn.setSelected(false);
            btn.setImageResource(R.drawable.mute_icon);
        } else {
            btn.setSelected(true);
            btn.setImageResource(R.drawable.unmute_icon);
        }

        mRtcEngine.muteLocalAudioStream(btn.isSelected());
    }

    public void onVideoMuteClicked(View view) {
        ImageView btn = (ImageView) view;
        if (btn.isSelected()) {
            btn.setSelected(false);
            btn.setImageResource(R.drawable.stop_video_icon);
        } else {
            btn.setSelected(true);
            btn.setImageResource(R.drawable.video_icon);
        }

        mRtcEngine.muteLocalVideoStream(btn.isSelected());

        FrameLayout container = findViewById(R.id.floating_video_container);
        container.setVisibility(btn.isSelected() ? View.GONE : View.VISIBLE);
        SurfaceView videoSurface = (SurfaceView) container.getChildAt(0);
        videoSurface.setZOrderMediaOverlay(!btn.isSelected());
        videoSurface.setVisibility(btn.isSelected() ? View.GONE : View.VISIBLE);
    }

    private void leaveChannel() {
        if (!isLeaved) {
            isLeaved = true;
            mRtcEngine.leaveChannel();
            if (isTaskRoot()) {
                startActivity(new Intent(this, HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            } else
                super.onBackPressed();
            finish();
        }
    }

    private void doBounceAnimation(View targetView) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(targetView, "translationY", 0, 25, 0);
//        animator.setInterpolator(new EasingInterpolator(Ease.BOUNCE_IN));
        animator.setDuration(1500);
        animator.start();
    }

    private void onRemoteUserVideoToggle(int uid, boolean toggle) {
        FrameLayout videoContainer = findViewById(R.id.bg_video_container);

        SurfaceView videoSurface = (SurfaceView) videoContainer.getChildAt(0);
        videoSurface.setVisibility(toggle ? View.GONE : View.VISIBLE);

        // add an icon to let the other user know remote video has been disabled
        if (toggle) {
            ImageView noCamera = new ImageView(this);
            noCamera.setImageResource(R.drawable.video_disabled);
            videoContainer.addView(noCamera);
        } else {
            ImageView noCamera = (ImageView) videoContainer.getChildAt(1);
            if (noCamera != null) {
                videoContainer.removeView(noCamera);
            }
        }
    }

    private void onRemoteUserLeft() {
        removeVideo(R.id.bg_video_container);
        onLeaveChannelClicked(null);
    }

    private void removeVideo(int containerID) {
        FrameLayout videoContainer = findViewById(containerID);
        videoContainer.removeAllViews();
    }

    public boolean checkSelfPermission(String permission, int requestCode) {
        Log.i(LOG_TAG, "checkSelfPermission " + permission + " " + requestCode);
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    REQUESTED_PERMISSIONS,
                    requestCode);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_ID) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                initAgoraEngine();
            } else {
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                showLongToast("Need permissions " + Manifest.permission.RECORD_AUDIO + "/" + Manifest.permission.CAMERA);
                finish();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            leaveChannel();
            RtcEngine.destroy();
            mRtcEngine = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final void showLongToast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }
}
