package com.android.yuniapp.activity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.yuniapp.R;
import com.android.yuniapp.adapter.ChatAdapter;
import com.android.yuniapp.application.BaseActivity;
import com.android.yuniapp.model.ChatMessages;
import com.android.yuniapp.model.GetChatMessagingResponse;
import com.android.yuniapp.rest.ApiClient;
import com.android.yuniapp.rest.ApiInterface;
import com.android.yuniapp.utils.AndroidUtils;
import com.android.yuniapp.utils.AppUtils;
import com.android.yuniapp.utils.ConnectivityController;
import com.android.yuniapp.utils.ConstantUtils;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity implements View.OnClickListener {
    private RecyclerView recyclerViewChat;
    private ChatAdapter chatAdapter;
    private EditText edtMessage;
    private ImageView imgSend;
    private ProgressBar progressBar;
    private ArrayList<ChatMessages> chatMessagesArrayList = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String entityType = "", entityId = "";
    private LinearLayoutManager mLayoutManager;

    private Handler getMessageHandler = new Handler();
    private boolean firstTime = true;
    private boolean isApiCallRunning = false;
    private ImageView shootingStarVIew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        initViews();
        setOnClick();
//        if (getIntent().getBooleanExtra(ConstantUtils.FROMNOTIFICATION, false)) {
//            showShootingStar();
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    hideShootingStar();
//                }
//            }, 4000);
//        }
        sharedPreferences = getSharedPreferences(ConstantUtils.APP_PREF, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        if (getIntent() != null) {
            if (getIntent().getStringExtra(ConstantUtils.ENTITY_TYPE) != null) {
                switch (getIntent().getStringExtra(ConstantUtils.ENTITY_TYPE).toUpperCase()) {
                    case "S":
                        AppUtils.setToolbarWithBothIcon(this, "Star Page", "", R.drawable.back_icon, 0, 0, 0);
                        entityId = getIntent().getStringExtra(ConstantUtils.ENTITY_ID);
                        entityType = getIntent().getStringExtra(ConstantUtils.ENTITY_TYPE);
                        break;
                    case "P":
                        AppUtils.setToolbarWithBothIcon(this, "Planet Page", "", R.drawable.back_icon, 0, 0, 0);
                        entityId = getIntent().getStringExtra(ConstantUtils.ENTITY_ID);
                        entityType = getIntent().getStringExtra(ConstantUtils.ENTITY_TYPE);
                        break;
                    case "M":
                        AppUtils.setToolbarWithBothIcon(this, "Moon Page", "", R.drawable.back_icon, 0, 0, 0);
                        entityId = getIntent().getStringExtra(ConstantUtils.ENTITY_ID);
                        entityType = getIntent().getStringExtra(ConstantUtils.ENTITY_TYPE);
                        break;
                    case "T":
                        AppUtils.setToolbarWithBothIcon(this, "Satellite Page", "", R.drawable.back_icon, 0, 0, 0);
                        entityId = getIntent().getStringExtra(ConstantUtils.ENTITY_ID);
                        entityType = getIntent().getStringExtra(ConstantUtils.ENTITY_TYPE);
                        break;
                    case "C":
                        AppUtils.setToolbarWithBothIcon(this, "Comet Page", "", R.drawable.back_icon, 0, 0, 0);
                        entityId = getIntent().getStringExtra(ConstantUtils.ENTITY_ID);
                        entityType = getIntent().getStringExtra(ConstantUtils.ENTITY_TYPE);
                        break;
                }
            }
        }

        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true);
        mLayoutManager.setReverseLayout(true);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true));
        chatAdapter = new ChatAdapter(this, chatMessagesArrayList, sharedPreferences.getString(ConstantUtils.USER_ID, ""));
        recyclerViewChat.setAdapter(chatAdapter);

//        callApiForGetMessage();

    }

    @Override
    public void onBackPressed() {
        if (isTaskRoot()) {
            startActivity(new Intent(ChatActivity.this, HomeActivity.class));
            finish();
        } else
            super.onBackPressed();
    }

    private void showShootingStar() {
        shootingStarVIew.setVisibility(View.VISIBLE);
    }

    private void hideShootingStar() {
        shootingStarVIew.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startHandler();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopHandler();
    }

    private void stopHandler() {
        getMessageHandler.removeCallbacksAndMessages(null);
    }

    private void callApiForGetMessage() {
        if (ConnectivityController.isNetworkAvailable(this))
            callGetChatMessagesApi();
        else
            AndroidUtils.showToast(ChatActivity.this, getResources().getString(R.string.no_internet));
    }

    private void startHandler() {
        getMessageHandler.post(new Runnable() {
            @Override
            public void run() {
                callApiForGetMessage();
                getMessageHandler.postDelayed(this, 4000);
            }
        });
    }

    private void initViews() {
//        shootingStarVIew = findViewById(R.id.shootingStarView);
        recyclerViewChat = findViewById(R.id.recycler_chat);
        edtMessage = findViewById(R.id.edt_message);
        imgSend = findViewById(R.id.img_send);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setOnClick() {
        imgSend.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_lft_most_img:
                onBackPressed();
                break;
            case R.id.img_send:
                if (edtMessage.getText().toString().trim().isEmpty())
                    AndroidUtils.showToast(ChatActivity.this, "Message can't be empty.");
                else {

                    if (ConnectivityController.isNetworkAvailable(ChatActivity.this))
                        calSendChatMessageApi();
                    else
                        AndroidUtils.showToast(ChatActivity.this, getResources().getString(R.string.some_went_wrong));
                }
                break;
        }
    }


    private void callGetChatMessagesApi() {
        if (isApiCallRunning)
            return;
        if (firstTime)
            progressBar.setVisibility(View.VISIBLE);
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetChatMessagingResponse> getChatMessagingResponseCall = apiService.callGetChatMessage(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), entityId, entityType);
        getChatMessagingResponseCall.enqueue(new Callback<GetChatMessagingResponse>() {
            @Override
            public void onResponse(Call<GetChatMessagingResponse> call, Response<GetChatMessagingResponse> response) {
                progressBar.setVisibility(View.GONE);
                firstTime = false;
                if (response.code() == 200 && response.body() != null) {
                    cancelNotification();
                    if (response.body().getChat_messages() != null && response.body().getChat_messages().size() > 0) {

                        chatMessagesArrayList.clear();
                        chatMessagesArrayList.addAll(response.body().getChat_messages());
                        chatAdapter.notifyDataSetChanged();
                        recyclerViewChat.scrollToPosition(chatMessagesArrayList.size());
                    }
                } else if (response.code() == 500)
                    AndroidUtils.showToast(ChatActivity.this, getResources().getString(R.string.some_went_wrong));
            }

            @Override
            public void onFailure(Call<GetChatMessagingResponse> call, Throwable t) {
                firstTime = false;
                progressBar.setVisibility(View.GONE);
                AndroidUtils.showToast(ChatActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });

    }

    private void cancelNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(ConstantUtils.MESSAGE_ARRIVED);
        }
    }


    private void calSendChatMessageApi() {
        isApiCallRunning = true;
        showMessageSendingLoader();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetChatMessagingResponse> sendChatMessageResponse = apiService.callSendChatMessage(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), entityId, entityType, sharedPreferences.getString(ConstantUtils.USER_NAME, ""), edtMessage.getText().toString());
        sendChatMessageResponse.enqueue(new Callback<GetChatMessagingResponse>() {
            @Override
            public void onResponse(Call<GetChatMessagingResponse> call, Response<GetChatMessagingResponse> response) {
                hideMessageSendingLoader();
                isApiCallRunning = false;
                if (response.code() == 200 && response.body() != null) {
                    if (response.body().getChat_messages() != null && response.body().getChat_messages().size() > 0) {
                        edtMessage.setText("");
                        chatMessagesArrayList.add(0, response.body().getChat_messages().get(0));
                        chatAdapter.notifyDataSetChanged();
                        //chatAdapter.notifyItemRangeChanged(0,chatMessagesArrayList.size());
                        //  recyclerViewChat.scrollToPosition(chatMessagesArrayList.size());
                    }
                } else {
                    AndroidUtils.showToast(ChatActivity.this, getResources().getString(R.string.some_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<GetChatMessagingResponse> call, Throwable t) {
                hideMessageSendingLoader();
                isApiCallRunning = false;
                AndroidUtils.showToast(ChatActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });

    }

    private void showMessageSendingLoader() {
        findViewById(R.id.progressbar).setVisibility(View.VISIBLE);
        imgSend.setVisibility(View.INVISIBLE);
    }

    private void hideMessageSendingLoader() {
        findViewById(R.id.progressbar).setVisibility(View.GONE);
        imgSend.setVisibility(View.VISIBLE);
    }

}


//


