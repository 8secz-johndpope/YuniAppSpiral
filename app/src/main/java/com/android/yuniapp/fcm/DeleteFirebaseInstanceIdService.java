package com.android.yuniapp.fcm;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.android.yuniapp.utils.ConstantUtils;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;

/**
 * Created by prashantkumar on 20/12/17.
 */

public class DeleteFirebaseInstanceIdService extends IntentService {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public static final String TAG = DeleteFirebaseInstanceIdService.class.getSimpleName();

    public DeleteFirebaseInstanceIdService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        sharedPreferences = getSharedPreferences(ConstantUtils.APP_PREF, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        try {

            FirebaseInstanceId.getInstance().deleteInstanceId();

            if(FirebaseInstanceId.getInstance().getToken()!=null)
                editor.putString(ConstantUtils.DEVICE_TOKEN, FirebaseInstanceId.getInstance().getToken()).apply();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}