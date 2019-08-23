package com.android.yuniapp.application;

import android.app.Application;

import com.android.yuniapp.utils.AndroidUtils;
import com.crashlytics.android.Crashlytics;

import io.agora.openvcall.model.CurrentUserSettings;
import io.agora.openvcall.model.WorkerThread;
import io.fabric.sdk.android.Fabric;

public class YuniApplication extends Application {


    public static final CurrentUserSettings mVideoSettings = new CurrentUserSettings();
    private WorkerThread mWorkerThread;


    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        AndroidUtils.getHashKey(getApplicationContext());

    }

    public synchronized void initWorkerThread() {
        if (mWorkerThread == null) {
            mWorkerThread = new WorkerThread(getApplicationContext());
            mWorkerThread.start();

            mWorkerThread.waitForReady();
        }
    }

    public synchronized WorkerThread getWorkerThread() {
        return mWorkerThread;
    }

    public synchronized void deInitWorkerThread() {
        mWorkerThread.exit();
        try {
            mWorkerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mWorkerThread = null;
    }
}
