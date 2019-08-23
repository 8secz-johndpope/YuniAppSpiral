package com.android.yuniapp.application;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.android.yuniapp.R;
import com.android.yuniapp.fragment.CustomPopup;
import com.android.yuniapp.utils.ConstantUtils;
import com.android.yuniapp.utils.MyBroadcastReceiver;

import pl.droidsonroids.gif.GifImageView;

public class BaseActivity extends AppCompatActivity {

    private BroadcastReceiver mMessageReceiver = new MyBroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            perform action related to the event
            showPopUpForNotifications(intent);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mMessageReceiver, new IntentFilter(ConstantUtils.BROADCAST_KEY));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mMessageReceiver);
    }

    private void showPopUpForNotifications(Intent intent) {
        //region CODE for showing shooting star animation

        final ViewGroup view = findViewById(android.R.id.content);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        final GifImageView gifImageView = new GifImageView(this);
        gifImageView.setBackgroundResource(R.drawable.shooting_star_gif);
        gifImageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        linearLayout.addView(gifImageView);
        view.addView(linearLayout);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                gifImageView.setVisibility(View.GONE);
            }
        }, 4000);

        //endregion

        CustomPopup.showPopup(this,intent);

        cancelNotification();
    }



    private void cancelNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(ConstantUtils.MEMBER_ADDED);
        }
    }

}
