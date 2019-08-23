package com.android.yuniapp.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.yuniapp.R;
import com.android.yuniapp.utils.ConstantUtils;

import java.util.Calendar;
import java.util.Date;

public class CustomPopup {
    public static void showPopup(Context context, Intent intent) {

        String type = "";
        if (intent.getStringExtra(ConstantUtils.ENTITY_TYPE) != null)
            type = intent.getStringExtra(ConstantUtils.ENTITY_TYPE);
        String name = "";
        if (intent.getStringExtra(ConstantUtils.STAR_NAME) != null)
            name = intent.getStringExtra(ConstantUtils.STAR_NAME);
        int popupWidth = ViewGroup.LayoutParams.WRAP_CONTENT;
        int popupHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
        // Inflate the popup_layout.xml
        LinearLayout viewGroup = ((Activity) context).findViewById(R.id.popup);
        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.popup_layout, viewGroup);
        layout.setAnimation(AnimationUtils.loadAnimation(context, R.anim.popupanimation));
        // Creating the PopupWindow
        final PopupWindow popup = new PopupWindow(context);
//        popup.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.popup_rounded_corner));
        popup.setBackgroundDrawable(new BitmapDrawable(context.getResources()));
        popup.setContentView(layout);
        popup.setWidth(popupWidth);
        popup.setHeight(popupHeight);
        popup.setFocusable(true);
//        popup.showAtLocation(view, Gravity.NO_GRAVITY, p.x+10, p.y+10);
        popup.showAtLocation(new RelativeLayout(context), Gravity.RIGHT, 0, -220);


        ImageView image = layout.findViewById(R.id.img_planet);
        TextView headTxt = layout.findViewById(R.id.messageHeader);
        TextView bodyTxt = layout.findViewById(R.id.bodyTxt);
        TextView timeTxt = layout.findViewById(R.id.txt_time);

        Date date = Calendar.getInstance().getTime();
        timeTxt.setText(DateFormat.format("hh:mm a", date));
        headTxt.setText(name);
        switch (type.toLowerCase()) {
            case "s":
                image.setImageResource(R.drawable.middle_star);
                bodyTxt.setText("You have been added to a Star");
                break;
            case "p":
                image.setImageResource(R.drawable.middle_planet);
                bodyTxt.setText("You have been added to a Planet");
                break;
            case "t":
                image.setImageResource(R.drawable.satellite_green_border);
                bodyTxt.setText("You have been added to a Satellite");
                break;
            case "m":
                image.setImageResource(R.drawable.middle_moon);
                bodyTxt.setText("You have been added to a Moon");
                break;
            case "c":
                image.setImageResource(R.drawable.comet_green_priority_1);
                bodyTxt.setText("You have been added to a Comet");
                break;
        }

        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {

//                clickedMarker.hideInfoWindow();
            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {

                    popup.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 4000);
    }
}
