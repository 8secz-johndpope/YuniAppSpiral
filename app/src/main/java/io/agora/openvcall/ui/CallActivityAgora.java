package io.agora.openvcall.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.yuniapp.R;
import com.android.yuniapp.activity.HomeActivity;
import com.android.yuniapp.rest.ApiClient;
import com.android.yuniapp.rest.ApiInterface;
import com.android.yuniapp.utils.AndroidUtils;
import com.android.yuniapp.utils.ConnectivityController;
import com.android.yuniapp.utils.ConstantUtils;
import com.android.yuniapp.utils.MyBroadcastReceiver;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import io.agora.openvcall.model.AGEventHandler;
import io.agora.openvcall.model.ConstantApp;
import io.agora.openvcall.model.DuringCallEventHandler;
import io.agora.openvcall.model.Message;
import io.agora.openvcall.ui.layout.GridVideoViewContainer;
import io.agora.openvcall.ui.layout.SmallVideoViewAdapter;
import io.agora.openvcall.ui.layout.SmallVideoViewDecoration;
import io.agora.propeller.Constant;
import io.agora.propeller.UserStatusData;
import io.agora.propeller.VideoInfoData;
import io.agora.propeller.ui.RecyclerItemClickListener;
import io.agora.propeller.ui.RtlLinearLayoutManager;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallActivityAgora extends AgoraBaseActivity implements DuringCallEventHandler {

    public static final int LAYOUT_TYPE_DEFAULT = 0;
    public static final int LAYOUT_TYPE_SMALL = 1;
    private static final int CALL_OPTIONS_REQUEST = 3222;
    // should only be modified under UI thread
    private final HashMap<Integer, SurfaceView> mUidsList = new HashMap<>(); // uid = 0 || uid == EngineConfig.mUid
    private final Handler mUIHandler = new Handler();
    public int mLayoutType = LAYOUT_TYPE_DEFAULT;
    private GridVideoViewContainer mGridVideoViewContainer;
    private RelativeLayout mSmallVideoViewDock;
    private volatile boolean mVideoMuted = false;
    private volatile boolean mAudioMuted = false;
    private volatile boolean mMixingAudio = false;
    private volatile int mAudioRouting = Constants.AUDIO_ROUTE_DEFAULT;
    private volatile boolean mFullScreen = false;
    private boolean mIsLandscape = false;
    private ArrayList<Message> mMsgList;
    private SmallVideoViewAdapter mSmallVideoViewAdapter;
    private String channelName;
    private String objectID;
    private String objectType;
    private BroadcastReceiver mMessageReceiver = new MyBroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            perform action related to the event
            onHangupClicked(null);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        makeActivityContentShownUnderStatusBar();
        setContentView(R.layout.activity_call);
//        checkSelfPermissions();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        onHangupClicked(null);
    }

    @Override
    protected void initUIandEvent() {
        if (!ConnectivityController.isNetworkAvailable(CallActivityAgora.this)) {
            AndroidUtils.showToast(this, getString(R.string.no_internet));
            finish();
        }
        event().addEventHandler(this);

        Intent i = getIntent();

        channelName = i.getStringExtra(ConstantApp.ACTION_KEY_CHANNEL_NAME);

        if (i.getBooleanExtra(ConstantApp.IS_HOST, false)) {

            objectID = i.getStringExtra(ConstantUtils.ENTITY_ID);

            objectType = i.getStringExtra(ConstantUtils.ENTITY_TYPE);
            channelName = objectID + "" + objectType + "_video";
            callVideoCallAPI(objectID, objectType, channelName);
        }

        // programmatically show channel name
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            TextView channelNameView = ((TextView) findViewById(R.id.ovc_page_title));
            channelNameView.setText(channelName);
        }

        // programmatically layout ui below of status bar/action bar
        LinearLayout eopsContainer = findViewById(R.id.extra_ops_container);
        RelativeLayout.MarginLayoutParams eofmp = (RelativeLayout.MarginLayoutParams) eopsContainer.getLayoutParams();
        eofmp.topMargin = getStatusBarHeight() + getActionBarHeight() + getResources().getDimensionPixelOffset(R.dimen.activity_vertical_margin) / 2; // status bar + action bar + divider

        final String encryptionKey = getIntent().getStringExtra(ConstantApp.ACTION_KEY_ENCRYPTION_KEY);
        final String encryptionMode = getIntent().getStringExtra(ConstantApp.ACTION_KEY_ENCRYPTION_MODE);

        doConfigEngine(encryptionKey, encryptionMode);

        mGridVideoViewContainer = (GridVideoViewContainer) findViewById(R.id.grid_video_view_container);
        mGridVideoViewContainer.setItemEventHandler(new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                onBigVideoViewClicked(view, position);
            }

            @Override
            public void onItemLongClick(View view, int position) {
            }

            @Override
            public void onItemDoubleClick(View view, int position) {
                onBigVideoViewDoubleClicked(view, position);
            }
        });

        SurfaceView surfaceV = RtcEngine.CreateRendererView(getApplicationContext());
        rtcEngine().setupLocalVideo(new VideoCanvas(surfaceV, VideoCanvas.RENDER_MODE_HIDDEN, 0));
        surfaceV.setZOrderOnTop(false);
        surfaceV.setZOrderMediaOverlay(false);

        mUidsList.put(0, surfaceV); // get first surface view

        mGridVideoViewContainer.initViewContainer(this, 0, mUidsList, mIsLandscape); // first is now full view
        worker().preview(true, surfaceV, 0);

//        initMessageList();
//
//        notifyMessageChanged(new Message(new User(0, null), "start join " + channelName + " as " + (config().mUid & 0xFFFFFFFFL)));

        worker().joinChannel(channelName, config().mUid);

        optional();
    }

    private void callVideoCallAPI(String objectID, String objectType, String channelName) {
        SharedPreferences sharedPreferences = getSharedPreferences(ConstantUtils.APP_PREF, Context.MODE_PRIVATE);
        Call<JsonElement> starResponseModelCall = ApiClient.getClient().create(ApiInterface.class)
                .sendVideoCallNottification(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), objectID, objectType, channelName);
        starResponseModelCall.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.code() == 200 && response.body() != null) {

                } else if (response.code() == 500) {
                    AndroidUtils.showToast(CallActivityAgora.this, getResources().getString(R.string.some_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                AndroidUtils.showToast(CallActivityAgora.this, getResources().getString(R.string.some_went_wrong));
            }
        });
    }

    private void onBigVideoViewClicked(View view, int position) {

        toggleFullscreen();
    }

    private void onBigVideoViewDoubleClicked(View view, int position) {

        if (mUidsList.size() < 2) {
            return;
        }

        UserStatusData user = mGridVideoViewContainer.getItem(position);
        int uid = (user.mUid == 0) ? config().mUid : user.mUid;

        if (mLayoutType == LAYOUT_TYPE_DEFAULT && mUidsList.size() != 1) {
            switchToSmallVideoView(uid);
        } else {
            switchToDefaultVideoView();
        }
    }

    private void onSmallVideoViewDoubleClicked(View view, int position) {

        switchToDefaultVideoView();
    }

    private void makeActivityContentShownUnderStatusBar() {
        // https://developer.android.com/training/system-ui/status
        // May fail on some kinds of devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

            decorView.setSystemUiVisibility(uiOptions);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
            }
        }
    }

    private void showOrHideStatusBar(boolean hide) {
        // May fail on some kinds of devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            View decorView = getWindow().getDecorView();
            int uiOptions = decorView.getSystemUiVisibility();

            if (hide) {
                uiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
            } else {
                uiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
            }

            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    private void toggleFullscreen() {
        mFullScreen = !mFullScreen;

        showOrHideCtrlViews(mFullScreen);

        mUIHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showOrHideStatusBar(mFullScreen);
            }
        }, 200); // action bar fade duration
    }

    private void showOrHideCtrlViews(boolean hide) {
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            if (hide) {
                ab.hide();
            } else {
                ab.show();
            }
        }

        findViewById(R.id.extra_ops_container).setVisibility(hide ? View.INVISIBLE : View.VISIBLE);
        findViewById(R.id.bottom_action_container).setVisibility(hide ? View.INVISIBLE : View.VISIBLE);
    }

    private void relayoutForVirtualKeyPad(int orientation) {
        int virtualKeyHeight = virtualKeyHeight();

        LinearLayout eopsContainer = findViewById(R.id.extra_ops_container);
        FrameLayout.MarginLayoutParams eofmp = (FrameLayout.MarginLayoutParams) eopsContainer.getLayoutParams();

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            eofmp.rightMargin = virtualKeyHeight;
            eofmp.leftMargin = 0;
        } else {
            eofmp.leftMargin = 0;
            eofmp.rightMargin = 0;
        }

        LinearLayout bottomContainer = findViewById(R.id.bottom_container);
        FrameLayout.MarginLayoutParams fmp = (FrameLayout.MarginLayoutParams) bottomContainer.getLayoutParams();

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            fmp.bottomMargin = 0;
            fmp.rightMargin = virtualKeyHeight;
            fmp.leftMargin = 0;
        } else {
            fmp.bottomMargin = virtualKeyHeight;
            fmp.leftMargin = 0;
            fmp.rightMargin = 0;
        }
    }


//    private void initMessageList() {
//        mMsgList = new ArrayList<>();
//        RecyclerView msgListView = (RecyclerView) findViewById(R.id.msg_list);
//
//        mMsgAdapter = new InChannelMessageListAdapter(this, mMsgList);
//        mMsgAdapter.setHasStableIds(true);
//        msgListView.setAdapter(mMsgAdapter);
//        msgListView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false));
//        msgListView.addItemDecoration(new MessageListDecoration());
//    }
//
//    private void notifyMessageChanged(Message msg) {
//        mMsgList.add(msg);
//
//        int MAX_MESSAGE_COUNT = 16;
//
//        if (mMsgList.size() > MAX_MESSAGE_COUNT) {
//            int toRemove = mMsgList.size() - MAX_MESSAGE_COUNT;
//            for (int i = 0; i < toRemove; i++) {
//                mMsgList.remove(i);
//            }
//        }
//
//        mMsgAdapter.notifyDataSetChanged();
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CALL_OPTIONS_REQUEST) {
        }
    }

    private void optional() {
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
    }

    private void optionalDestroy() {
    }

    private int getVideoEncResolutionIndex() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int videoEncResolutionIndex = pref.getInt(ConstantApp.PrefManager.PREF_PROPERTY_VIDEO_ENC_RESOLUTION, ConstantApp.DEFAULT_VIDEO_ENC_RESOLUTION_IDX);
        if (videoEncResolutionIndex > ConstantApp.VIDEO_DIMENSIONS.length - 1) {
            videoEncResolutionIndex = ConstantApp.DEFAULT_VIDEO_ENC_RESOLUTION_IDX;

            // save the new value
            SharedPreferences.Editor editor = pref.edit();
            editor.putInt(ConstantApp.PrefManager.PREF_PROPERTY_VIDEO_ENC_RESOLUTION, videoEncResolutionIndex);
            editor.apply();
        }
        return videoEncResolutionIndex;
    }

    private int getVideoEncFpsIndex() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int videoEncFpsIndex = pref.getInt(ConstantApp.PrefManager.PREF_PROPERTY_VIDEO_ENC_FPS, ConstantApp.DEFAULT_VIDEO_ENC_FPS_IDX);
        if (videoEncFpsIndex > ConstantApp.VIDEO_FPS.length - 1) {
            videoEncFpsIndex = ConstantApp.DEFAULT_VIDEO_ENC_FPS_IDX;

            // save the new value
            SharedPreferences.Editor editor = pref.edit();
            editor.putInt(ConstantApp.PrefManager.PREF_PROPERTY_VIDEO_ENC_FPS, videoEncFpsIndex);
            editor.apply();
        }
        return videoEncFpsIndex;
    }

    private void doConfigEngine(String encryptionKey, String encryptionMode) {
        VideoEncoderConfiguration.VideoDimensions videoDimension = ConstantApp.VIDEO_DIMENSIONS[getVideoEncResolutionIndex()];
        VideoEncoderConfiguration.FRAME_RATE videoFps = ConstantApp.VIDEO_FPS[getVideoEncFpsIndex()];

        worker().configEngine(videoDimension, videoFps, encryptionKey, encryptionMode);
    }

    public void onSwitchCameraClicked(View view) {
        RtcEngine rtcEngine = rtcEngine();
        rtcEngine.switchCamera();
    }

    public void onSwitchSpeakerClicked(View view) {
        RtcEngine rtcEngine = rtcEngine();
        rtcEngine.setEnableSpeakerphone(mAudioRouting != Constants.AUDIO_ROUTE_SPEAKERPHONE);
    }

    public void onFilterClicked(View view) {
        Constant.BEAUTY_EFFECT_ENABLED = !Constant.BEAUTY_EFFECT_ENABLED;

        if (Constant.BEAUTY_EFFECT_ENABLED) {
            worker().setBeautyEffectParameters(Constant.BEAUTY_EFFECT_DEFAULT_LIGHTNESS, Constant.BEAUTY_EFFECT_DEFAULT_SMOOTHNESS, Constant.BEAUTY_EFFECT_DEFAULT_REDNESS);
            worker().enablePreProcessor();
        } else {
            worker().disablePreProcessor();
        }

        ImageView iv = (ImageView) view;

        iv.setImageResource(Constant.BEAUTY_EFFECT_ENABLED ? R.drawable.btn_filter : R.drawable.btn_filter_off);
    }

    @Override
    protected void deInitUIandEvent() {
        optionalDestroy();

        doLeaveChannel();
        event().removeEventHandler(this);

        mUidsList.clear();
    }

    private void doLeaveChannel() {
        worker().leaveChannel(config().mChannel);
        worker().preview(false, null, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mMessageReceiver, new IntentFilter(ConstantUtils.DISCONNECT_BROADCAST_KEY));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mMessageReceiver);
    }

    public void onHangupClicked(View view) {
        if (getIntent().getBooleanExtra(ConstantApp.IS_HOST, false)) {
            callDisconnectVideoApi();
        }
        if (isTaskRoot()) {
            startActivity(new Intent(CallActivityAgora.this, HomeActivity.class));
        }
        finish();
    }

    private void callDisconnectVideoApi() {
//        if (!ConnectivityController.isNetworkAvailable(CallActivityAgora.this)) {
//            AndroidUtils.showToast(this, getString(R.string.no_internet));
//            finish();
//        }
        SharedPreferences sharedPreferences = getSharedPreferences(ConstantUtils.APP_PREF, Context.MODE_PRIVATE);
        Call<JsonElement> starResponseModelCall = ApiClient.getClient().create(ApiInterface.class)
                .sendDisconnectVideoCallNottification(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), objectID, objectType, channelName);
        starResponseModelCall.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.code() == 200 && response.body() != null) {

                } else if (response.code() == 500) {
                    AndroidUtils.showToast(CallActivityAgora.this, getResources().getString(R.string.some_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                AndroidUtils.showToast(CallActivityAgora.this, getResources().getString(R.string.some_went_wrong));
            }
        });
    }

    public void onVideoMuteClicked(View view) {
        if (mUidsList.size() == 0) {
            return;
        }

        SurfaceView surfaceV = getLocalView();
        ViewParent parent;
        if (surfaceV == null || (parent = surfaceV.getParent()) == null) {
            return;
        }

        RtcEngine rtcEngine = rtcEngine();
        mVideoMuted = !mVideoMuted;

        if (mVideoMuted) {
            rtcEngine.disableVideo();
        } else {
            rtcEngine.enableVideo();
        }

        ImageView iv = (ImageView) view;

        iv.setImageResource(mVideoMuted ? R.drawable.btn_camera_off : R.drawable.btn_camera);

        hideLocalView(mVideoMuted);
    }

    private SurfaceView getLocalView() {
        for (HashMap.Entry<Integer, SurfaceView> entry : mUidsList.entrySet()) {
            if (entry.getKey() == 0 || entry.getKey() == config().mUid) {
                return entry.getValue();
            }
        }

        return null;
    }

    private void hideLocalView(boolean hide) {
        int uid = config().mUid;
        doHideTargetView(uid, hide);
    }

    private void doHideTargetView(int targetUid, boolean hide) {
        HashMap<Integer, Integer> status = new HashMap<>();
        status.put(targetUid, hide ? UserStatusData.VIDEO_MUTED : UserStatusData.DEFAULT_STATUS);
        if (mLayoutType == LAYOUT_TYPE_DEFAULT) {
            mGridVideoViewContainer.notifyUiChanged(mUidsList, targetUid, status, null);
        } else if (mLayoutType == LAYOUT_TYPE_SMALL) {
            UserStatusData bigBgUser = mGridVideoViewContainer.getItem(0);
            if (bigBgUser.mUid == targetUid) { // big background is target view
                mGridVideoViewContainer.notifyUiChanged(mUidsList, targetUid, status, null);
            } else { // find target view in small video view list
                mSmallVideoViewAdapter.notifyUiChanged(mUidsList, bigBgUser.mUid, status, null);
            }
        }
    }

    public void onVoiceMuteClicked(View view) {
        if (mUidsList.size() == 0) {
            return;
        }

        RtcEngine rtcEngine = rtcEngine();
        rtcEngine.muteLocalAudioStream(mAudioMuted = !mAudioMuted);

        ImageView iv = (ImageView) view;

        iv.setImageResource(mAudioMuted ? R.drawable.btn_microphone_off : R.drawable.btn_microphone);
    }

    public void onMixingAudioClicked(View view) {

        if (mUidsList.size() == 0) {
            return;
        }

        mMixingAudio = !mMixingAudio;

        RtcEngine rtcEngine = rtcEngine();
        if (mMixingAudio) {
            rtcEngine.startAudioMixing(Constant.MIX_FILE_PATH, false, false, -1);
        } else {
            rtcEngine.stopAudioMixing();
        }

        ImageView iv = (ImageView) view;
    }

    @Override
    public void onUserJoined(final int uid) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                notifyMessageChanged(new Message(new User(0, null), "user " + (uid & 0xFFFFFFFFL) + " joined"));
            }
        });
    }

    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {

        doRenderRemoteUi(uid);
    }

    private void doRenderRemoteUi(final int uid) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

                if (mUidsList.containsKey(uid)) {
                    return;
                }

                SurfaceView surfaceV = RtcEngine.CreateRendererView(getApplicationContext());
                mUidsList.put(uid, surfaceV);

                boolean useDefaultLayout = mLayoutType == LAYOUT_TYPE_DEFAULT;

                surfaceV.setZOrderOnTop(true);
                surfaceV.setZOrderMediaOverlay(true);

                rtcEngine().setupRemoteVideo(new VideoCanvas(surfaceV, VideoCanvas.RENDER_MODE_HIDDEN, uid));

                if (false) {
                    switchToDefaultVideoView();
                } else {
                    int bigBgUid = mSmallVideoViewAdapter == null ? uid : mSmallVideoViewAdapter.getExceptedUid();
                    switchToSmallVideoView(bigBgUid);
                }

//                notifyMessageChanged(new Message(new User(0, null), "video from user " + (uid & 0xFFFFFFFFL) + " decoded"));
            }
        });
    }

    @Override
    public void onJoinChannelSuccess(final String channel, final int uid, final int elapsed) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

//                notifyMessageChanged(new Message(new User(0, null), "join " + channel + " success as " + (uid & 0xFFFFFFFFL) + " in " + elapsed + "ms"));

                SurfaceView local = mUidsList.remove(0);

                if (local == null) {
                    return;
                }

                mUidsList.put(uid, local);
            }
        });
    }

    @Override
    public void onUserOffline(int uid, int reason) {

        doRemoveRemoteUi(uid);
    }

    @Override
    public void onExtraCallback(final int type, final Object... data) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

                doHandleExtraCallback(type, data);
            }
        });
    }

    private void doHandleExtraCallback(int type, Object... data) {
        int peerUid;
        boolean muted;

        switch (type) {
            case AGEventHandler.EVENT_TYPE_ON_USER_AUDIO_MUTED:
                peerUid = (Integer) data[0];
                muted = (boolean) data[1];

                if (mLayoutType == LAYOUT_TYPE_DEFAULT) {
                    HashMap<Integer, Integer> status = new HashMap<>();
                    status.put(peerUid, muted ? UserStatusData.AUDIO_MUTED : UserStatusData.DEFAULT_STATUS);
                    mGridVideoViewContainer.notifyUiChanged(mUidsList, config().mUid, status, null);
                }

                break;

            case AGEventHandler.EVENT_TYPE_ON_USER_VIDEO_MUTED:
                peerUid = (Integer) data[0];
                muted = (boolean) data[1];

                doHideTargetView(peerUid, muted);

                break;

            case AGEventHandler.EVENT_TYPE_ON_USER_VIDEO_STATS:
                IRtcEngineEventHandler.RemoteVideoStats stats = (IRtcEngineEventHandler.RemoteVideoStats) data[0];

                if (Constant.SHOW_VIDEO_INFO) {
                    if (mLayoutType == LAYOUT_TYPE_DEFAULT) {
                        mGridVideoViewContainer.addVideoInfo(stats.uid, new VideoInfoData(stats.width, stats.height, stats.delay, stats.rendererOutputFrameRate, stats.receivedBitrate));
                        int uid = config().mUid;
                        int profileIndex = getVideoEncResolutionIndex();
                        String resolution = getResources().getStringArray(R.array.string_array_resolutions)[profileIndex];
                        String fps = getResources().getStringArray(R.array.string_array_frame_rate)[profileIndex];

                        String[] rwh = resolution.split("x");
                        int width = Integer.valueOf(rwh[0]);
                        int height = Integer.valueOf(rwh[1]);

                        mGridVideoViewContainer.addVideoInfo(uid, new VideoInfoData(width > height ? width : height,
                                width > height ? height : width,
                                0, Integer.valueOf(fps), Integer.valueOf(0)));
                    }
                } else {
                    mGridVideoViewContainer.cleanVideoInfo();
                }

                break;

            case AGEventHandler.EVENT_TYPE_ON_SPEAKER_STATS:
                IRtcEngineEventHandler.AudioVolumeInfo[] infos = (IRtcEngineEventHandler.AudioVolumeInfo[]) data[0];

                if (infos.length == 1 && infos[0].uid == 0) { // local guy, ignore it
                    break;
                }

                if (mLayoutType == LAYOUT_TYPE_DEFAULT) {
                    HashMap<Integer, Integer> volume = new HashMap<>();

                    for (IRtcEngineEventHandler.AudioVolumeInfo each : infos) {
                        peerUid = each.uid;
                        int peerVolume = each.volume;

                        if (peerUid == 0) {
                            continue;
                        }
                        volume.put(peerUid, peerVolume);
                    }
                    mGridVideoViewContainer.notifyUiChanged(mUidsList, config().mUid, null, volume);
                }

                break;

            case AGEventHandler.EVENT_TYPE_ON_APP_ERROR:
                int subType = (int) data[0];

                if (subType == ConstantApp.AppError.NO_CONNECTION_ERROR) {
                    String msg = getString(R.string.msg_connection_error);
//                    notifyMessageChanged(new Message(new User(0, null), msg));
                    showLongToast(msg);
                }

                break;

            case AGEventHandler.EVENT_TYPE_ON_DATA_CHANNEL_MSG:

                peerUid = (Integer) data[0];
                final byte[] content = (byte[]) data[1];
//                notifyMessageChanged(new Message(new User(peerUid, String.valueOf(peerUid)), new String(content)));

                break;

            case AGEventHandler.EVENT_TYPE_ON_AGORA_MEDIA_ERROR: {
                int error = (int) data[0];
                String description = (String) data[1];

//                notifyMessageChanged(new Message(new User(0, null), error + " " + description));

                break;
            }

            case AGEventHandler.EVENT_TYPE_ON_AUDIO_ROUTE_CHANGED:
                notifyHeadsetPlugged((int) data[0]);

                break;

        }
    }

    private void requestRemoteStreamType(final int currentHostCount) {
    }

    private void doRemoveRemoteUi(final int uid) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

                Object target = mUidsList.remove(uid);
                if (target == null) {
                    return;
                }

                int bigBgUid = -1;
                if (mSmallVideoViewAdapter != null) {
                    bigBgUid = mSmallVideoViewAdapter.getExceptedUid();
                }


                if (mLayoutType == LAYOUT_TYPE_DEFAULT || uid == bigBgUid) {
                    switchToDefaultVideoView();
                } else {
                    switchToSmallVideoView(bigBgUid);
                }

//                notifyMessageChanged(new Message(new User(0, null), "user " + (uid & 0xFFFFFFFFL) + " left"));
            }
        });
    }

    private void switchToDefaultVideoView() {
        if (mSmallVideoViewDock != null) {
            mSmallVideoViewDock.setVisibility(View.GONE);
        }
        mGridVideoViewContainer.initViewContainer(this, config().mUid, mUidsList, mIsLandscape);

        mLayoutType = LAYOUT_TYPE_DEFAULT;
        boolean setRemoteUserPriorityFlag = false;
        int sizeLimit = mUidsList.size();
        if (sizeLimit > ConstantApp.MAX_PEER_COUNT + 1) {
            sizeLimit = ConstantApp.MAX_PEER_COUNT + 1;
        }
        for (int i = 0; i < sizeLimit; i++) {
            int uid = mGridVideoViewContainer.getItem(i).mUid;
            if (config().mUid != uid) {
                if (!setRemoteUserPriorityFlag) {
                    setRemoteUserPriorityFlag = true;
                    rtcEngine().setRemoteUserPriority(uid, Constants.USER_PRIORITY_HIGH);
                } else {
                    rtcEngine().setRemoteUserPriority(uid, Constants.USER_PRIORITY_NORANL);
                }
            }
        }
    }

    private void switchToSmallVideoView(int bigBgUid) {
        HashMap<Integer, SurfaceView> slice = new HashMap<>(1);
        slice.put(bigBgUid, mUidsList.get(bigBgUid));
        Iterator<SurfaceView> iterator = mUidsList.values().iterator();
        while (iterator.hasNext()) {
            SurfaceView s = iterator.next();
            s.setZOrderOnTop(true);
            s.setZOrderMediaOverlay(true);
        }

        mUidsList.get(bigBgUid).setZOrderOnTop(false);
        mUidsList.get(bigBgUid).setZOrderMediaOverlay(false);

        mGridVideoViewContainer.initViewContainer(this, bigBgUid, slice, mIsLandscape);

        bindToSmallVideoView(bigBgUid);

        mLayoutType = LAYOUT_TYPE_SMALL;

        requestRemoteStreamType(mUidsList.size());
    }

    private void bindToSmallVideoView(int exceptUid) {
        if (mSmallVideoViewDock == null) {
            ViewStub stub = (ViewStub) findViewById(R.id.small_video_view_dock);
            mSmallVideoViewDock = (RelativeLayout) stub.inflate();
        }

        boolean twoWayVideoCall = mUidsList.size() == 2;

        RecyclerView recycler = (RecyclerView) findViewById(R.id.small_video_view_container);

        boolean create = false;

        if (mSmallVideoViewAdapter == null) {
            create = true;
            mSmallVideoViewAdapter = new SmallVideoViewAdapter(this, config().mUid, exceptUid, mUidsList);
            mSmallVideoViewAdapter.setHasStableIds(true);
        }
        recycler.setHasFixedSize(true);


        if (twoWayVideoCall) {
            recycler.setLayoutManager(new RtlLinearLayoutManager(getApplicationContext(), RtlLinearLayoutManager.HORIZONTAL, false));
        } else {
            recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
        }
        recycler.addItemDecoration(new SmallVideoViewDecoration());
        recycler.setAdapter(mSmallVideoViewAdapter);
        recycler.addOnItemTouchListener(new RecyclerItemClickListener(getBaseContext(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onItemLongClick(View view, int position) {

            }

            @Override
            public void onItemDoubleClick(View view, int position) {
                onSmallVideoViewDoubleClicked(view, position);
            }
        }));

        recycler.setDrawingCacheEnabled(true);
        recycler.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);

        if (!create) {
            mSmallVideoViewAdapter.setLocalUid(config().mUid);
            mSmallVideoViewAdapter.notifyUiChanged(mUidsList, exceptUid, null, null);
        }
        for (Integer tempUid : mUidsList.keySet()) {
            if (config().mUid != tempUid) {
                if (tempUid == exceptUid) {
                    rtcEngine().setRemoteUserPriority(tempUid, Constants.USER_PRIORITY_HIGH);
                } else {
                    rtcEngine().setRemoteUserPriority(tempUid, Constants.USER_PRIORITY_NORANL);
//                    log.debug("setRemoteUserPriority USER_PRIORITY_NORANL " + mUidsList.size() + " " + (tempUid & 0xFFFFFFFFL));
                }
            }
        }
        recycler.setVisibility(View.VISIBLE);
        mSmallVideoViewDock.setVisibility(View.VISIBLE);
    }

    public void notifyHeadsetPlugged(final int routing) {
//        log.info("notifyHeadsetPlugged " + routing + " " + mVideoMuted);

        mAudioRouting = routing;

        ImageView iv = (ImageView) findViewById(R.id.switch_speaker_id);
        if (mAudioRouting == Constants.AUDIO_ROUTE_SPEAKERPHONE) {
            iv.setImageResource(R.drawable.btn_speaker);
        } else {
            iv.setImageResource(R.drawable.btn_speaker_off);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mIsLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;

        if (mLayoutType == LAYOUT_TYPE_DEFAULT) {
            switchToDefaultVideoView();
        } else if (mSmallVideoViewAdapter != null) {
            switchToSmallVideoView(mSmallVideoViewAdapter.getExceptedUid());
        }
    }
}
