package com.android.yuniapp.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

import com.android.yuniapp.R;

public class SpiralView extends View {
    private Paint paint;
    private float centerX, centerY;

    public SpiralView(Context context) {
        super(context);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(14);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(getResources().getColor(R.color.colorAccent));

    }

    @Override
    public void setX(float x) {
        super.setX(x);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        centerX = w / 2;
        centerY = h / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawSpiral(getResources().getDimension(R.dimen._140sdp), 400, 5, canvas, paint);
    }


    public void drawSpiral(double maxRadius, int numSegments, double numOfOrbits, Canvas canvas, Paint paint) {
        double radius = 0;
        double x = centerX;
        double y = centerY;

        double deltaR = maxRadius / numSegments;

        for (int i = 0; i < numSegments; i++) {
            double x1 = x;
            double y1 = y;
            radius += deltaR;
            x = centerX + radius * Math.cos((2 * -Math.PI * i / numSegments) * numOfOrbits);
            y = centerY + radius * Math.sin((2 * -Math.PI * i / numSegments) * numOfOrbits);
            canvas.drawLine((float) x, (float) y, (float) x1, (float) y1, paint);

        /*    if (i > 0 && i <= 160) {
                paint.setColor(getResources().getColor(R.color.colorAccent));
                canvas.drawLine((float) x, (float) y, (float) x1, (float) y1, paint);
            } else if (i > 160 && i <= 240) {
                paint.setColor(Color.parseColor("#FFF200"));
                canvas.drawLine((float) x, (float) y, (float) x1, (float) y1, paint);
            } else if (i > 240 && i <= 320) {
                paint.setColor(Color.parseColor("#FFFDD0"));
                canvas.drawLine((float) x, (float) y, (float) x1, (float) y1, paint);
            } else if (i > 320) {
                paint.setColor(Color.parseColor("#E4CD05"));
                canvas.drawLine((float) x, (float) y, (float) x1, (float) y1, paint);
            }*/

        }
    }
}
