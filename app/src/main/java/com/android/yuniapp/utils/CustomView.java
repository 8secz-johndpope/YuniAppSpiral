package com.android.yuniapp.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.android.yuniapp.R;
import com.android.yuniapp.model.OrbitCoordinatesModel;
import com.android.yuniapp.model.XyCoordinatesModel;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class CustomView extends View {
    private static final int numOrbits = 5;
    private Paint paint;
    private float centerX, centerY;
    private float R0[];
    private Drawable drawableStar, drawableComit;
    private static final int planetRadius = 45;
    private int[] noOfPoints = {0,5, 10, 15, 20};
    private ArrayList<OrbitCoordinatesModel> orbitCoordinatesArrayList;
    private ArrayList<XyCoordinatesModel> xyCoordinatesModelList=new ArrayList<>();

    public CustomView(Context context) {
        super(context);

        orbitCoordinatesArrayList = new ArrayList<>();
        R0 = new float[numOrbits];

        drawableStar = getResources().getDrawable(R.drawable.star_orange_border);
        drawableComit = getResources().getDrawable(R.drawable.comets_element);

        drawableStar.setBounds(0, 0, 2 * planetRadius, 2 * planetRadius);
        drawableComit.setBounds(0, 0, 2 * planetRadius, 2 * planetRadius);


        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(14);
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(getResources().getColor(R.color.colorAccent));
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        centerX = w / 2;
        centerY = h / 2;


        for (int i = 0; i < numOrbits; i++) {
            R0[i] = i * 105;

         /*   xyCoordinatesModelList = new ArrayList<>();
            for (int j = 0; j < noOfPoints[i]; j++) {
                xyCoordinatesModelList.add(new XyCoordinatesModel(centerX + R0[i] * (float) Math.sin(2 * Math.PI * i / noOfPoints[i]) - planetRadius, (centerY) + R0[i] * (float) Math.cos(2 * Math.PI * i / noOfPoints[i]) - planetRadius));
            }
            orbitCoordinatesArrayList.add(new OrbitCoordinatesModel(i, xyCoordinatesModelList));*/

        }
        R0[1] = 130;



    }


    public ArrayList<XyCoordinatesModel> getXyCoordinatesModelList(){
        return xyCoordinatesModelList;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        drawBackground(paint, canvas);
  for (int i = 0; i < 5; i++) {

            xyCoordinatesModelList.add(new XyCoordinatesModel(centerX + R0[2] * (float) Math.sin(2 * Math.PI * i / 5) - planetRadius,(centerY) + R0[2] * (float) Math.cos(2 * Math.PI * i / 5) - planetRadius));

        }


     /*   for (int i = 0; i < 5; i++) {

            canvas.save();
            canvas.translate(centerX + R0[1] * (float) Math.sin(2 * Math.PI * i / 5) - planetRadius, (centerY) + R0[1] * (float) Math.cos(2 * Math.PI * i / 5) - planetRadius);
            drawableStar.draw(canvas);
            canvas.restore();
        }

        for (int i = 0; i < 10; i++) {


            if (i == 4 || i == 9) {
                canvas.save();
                canvas.translate(centerX + R0[2] * (float) Math.sin(2 * Math.PI * i / 10) - planetRadius, (centerY) + R0[2] * (float) Math.cos(2 * Math.PI * i / 10) - planetRadius);
                drawableComit.draw(canvas);
                canvas.restore();
            } else {
                canvas.save();
                canvas.translate(centerX + R0[2] * (float) Math.sin(2 * Math.PI * i / 10) - planetRadius, (centerY) + R0[2] * (float) Math.cos(2 * Math.PI * i / 10) - planetRadius);
                drawableStar.draw(canvas);
                canvas.restore();
            }
        }

        for (int i = 0; i < 15; i++) {
            if (i == 4 || i == 9 || i == 13) {
                canvas.save();
                canvas.translate(centerX + R0[3] * (float) Math.sin(2 * Math.PI * i / 15) - planetRadius, (centerY) + R0[3] * (float) Math.cos(2 * Math.PI * i / 15) - planetRadius);
                drawableComit.draw(canvas);
                canvas.restore();
            } else {
                canvas.save();
                canvas.translate(centerX + R0[3] * (float) Math.sin(2 * Math.PI * i / 15) - planetRadius, (centerY) + R0[3] * (float) Math.cos(2 * Math.PI * i / 15) - planetRadius);
                drawableStar.draw(canvas);
                canvas.restore();
            }
        }


        for (int i = 0; i < 20; i++) {
            if (i == 4 || i == 9) {
                canvas.save();
                canvas.translate(centerX + R0[4] * (float) Math.sin(2 * Math.PI * i / 20) - planetRadius, (centerY) + R0[4] * (float) Math.cos(2 * Math.PI * i / 20) - planetRadius);
                drawableComit.draw(canvas);
                canvas.restore();
            } else {
                canvas.save();
                canvas.translate(centerX + R0[4] * (float) Math.sin(2 * Math.PI * i / 20) - planetRadius, (centerY) + R0[4] * (float) Math.cos(2 * Math.PI * i / 20) - planetRadius);
                drawableStar.draw(canvas);
                canvas.restore();
            }
        }*/
        canvas.restore();
    }

    private void drawBackground(Paint paint, Canvas canvas) {

        for (int i = 0; i < numOrbits; i++) {
            canvas.drawCircle(centerX, centerY, R0[i], paint);

        }
    }
}
