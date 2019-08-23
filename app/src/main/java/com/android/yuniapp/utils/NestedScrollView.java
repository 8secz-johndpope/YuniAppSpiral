package com.android.yuniapp.utils;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.widget.ScrollerCompat;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class NestedScrollView extends FrameLayout implements NestedScrollingParent,NestedScrollingChild  {


    public NestedScrollView( Context context) {
        super(context);
    }
}