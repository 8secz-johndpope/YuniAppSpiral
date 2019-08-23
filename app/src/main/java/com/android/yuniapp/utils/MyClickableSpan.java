package com.android.yuniapp.utils;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;


public class MyClickableSpan extends ClickableSpan {

    @Override
    public void onClick(View widget) {
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setUnderlineText(false);
    }
}
