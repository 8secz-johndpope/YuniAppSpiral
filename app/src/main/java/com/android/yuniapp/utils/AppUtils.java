package com.android.yuniapp.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.MonthDisplayHelper;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.yuniapp.R;
import com.android.yuniapp.activity.ProfileActivity;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.Weeks;
import org.joda.time.Years;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Period;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class AppUtils {

    // Toolbar with both button with custom icon
    public static void setFullScreen(Context context) {


        // AndroidBug5497Workaround.assistActivity(((Activity) context));

        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(((Activity) context), WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            ((Activity) context).getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(((Activity) context), WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            ((Activity) context).getWindow().setStatusBarColor(Color.TRANSPARENT);


        }


    }


    public static void setFullScreenWithPadding(Context context) {
        AndroidBug5497Workaround.assistActivity(((Activity) context));
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(((Activity) context), WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            ((Activity) context).getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(((Activity) context), WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            ((Activity) context).getWindow().setStatusBarColor(Color.TRANSPARENT);


        }
        Toolbar toolbar = ((Activity) context).findViewById(R.id.common_toolbar);
        toolbar.setPadding(0, AppUtils.getStatusBarHeight(context), 0, 0);
    }

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static void rotateAnimation(View imageView) {
        RotateAnimation rotate = new RotateAnimation(
                0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotate.setDuration(30000);
        rotate.setInterpolator(new LinearInterpolator());
        rotate.setRepeatCount(RotateAnimation.INFINITE);
        imageView.startAnimation(rotate);
    }

    public static void setToolbarWithBothIcon(Context context, String toolbarTitle, String toolbarSubtitle, int leftMostImageId, int leftImageId, int rightImageId, int rightMostImageId) {
        ImageView imgLeftMost = (ImageView) ((Activity) context).findViewById(R.id.toolbar_lft_most_img);
        ImageView imgLeft = (ImageView) ((Activity) context).findViewById(R.id.toolbar_lft_img);
        ImageView imgRight = (ImageView) ((Activity) context).findViewById(R.id.toolbar_right_img);
        ImageView imgRightMost = (ImageView) ((Activity) context).findViewById(R.id.toolbar_right_most_img);
        TextView txtTitle = (TextView) ((Activity) context).findViewById(R.id.txt_toolbar_title);
        TextView txtSubTitle = (TextView) ((Activity) context).findViewById(R.id.txt_toolbar_subtitle);

        if (leftMostImageId != 0) {
            imgLeftMost.setVisibility(View.VISIBLE);
            Drawable leftMostImage = ContextCompat.getDrawable(context, leftMostImageId);
            imgLeftMost.setImageDrawable(leftMostImage);
            imgLeftMost.setOnClickListener(((View.OnClickListener) context));
        } else {
            imgLeftMost.setVisibility(View.INVISIBLE);

        }

        if (leftImageId != 0) {
            imgLeft.setVisibility(View.VISIBLE);
            Drawable leftImage = ContextCompat.getDrawable(context, leftImageId);
            imgLeft.setImageDrawable(leftImage);
            imgLeft.setOnClickListener(((View.OnClickListener) context));

        } else {
            imgLeft.setVisibility(View.INVISIBLE);
        }

        if (rightImageId != 0) {
            imgRight.setVisibility(View.VISIBLE);
            Drawable rightImage = ContextCompat.getDrawable(context, rightImageId);
            imgRight.setImageDrawable(rightImage);
            imgRight.setOnClickListener(((View.OnClickListener) context));

        } else {
            imgRight.setVisibility(View.INVISIBLE);
        }
        if (rightMostImageId != 0) {
            imgRightMost.setVisibility(View.VISIBLE);
            Drawable rightMostImage = ContextCompat.getDrawable(context, rightMostImageId);
            imgRightMost.setImageDrawable(rightMostImage);
            imgRightMost.setOnClickListener(((View.OnClickListener) context));

        } else {
            imgRightMost.setVisibility(View.INVISIBLE);
        }

        txtTitle.setText(toolbarTitle);

        if (!toolbarSubtitle.isEmpty())
            txtSubTitle.setText(toolbarSubtitle);
        else txtSubTitle.setVisibility(View.GONE);

        setFullScreenWithPadding(context);
    }

    public static void setToolbarWithProfileImage(Context context, String toolbarTitle, String toolbarSubtitle, String profileUrl) {
        RoundedImageView imgProfile = ((Activity) context).findViewById(R.id.img_profile);
        TextView txtTitle = (TextView) ((Activity) context).findViewById(R.id.txt_toolbar_title);
        TextView txtSubTitle = (TextView) ((Activity) context).findViewById(R.id.txt_toolbar_subtitle);
        if (profileUrl != null && !profileUrl.isEmpty())
            Picasso.with(context).load(profileUrl).placeholder(R.drawable.profile_placeholder).into(imgProfile);

        imgProfile.setOnClickListener(((View.OnClickListener) context));

        txtTitle.setText(toolbarTitle);

        if (!toolbarSubtitle.isEmpty())
            txtSubTitle.setText(toolbarSubtitle);
        else txtSubTitle.setVisibility(View.GONE);

        setFullScreenWithPadding(context);
    }

    public static void dimBehind(PopupWindow popupWindow) {
        View container;
        if (popupWindow.getBackground() == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                container = (View) popupWindow.getContentView().getParent();
            } else {
                container = popupWindow.getContentView();
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                container = (View) popupWindow.getContentView().getRootView();
            } else {
                container = (View) popupWindow.getContentView().getParent();
            }
        }
        Context context = popupWindow.getContentView().getContext();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.3f;
        wm.updateViewLayout(container, p);
    }

    public static int getHourDiff(String finishDateTimeString) {
        int hourDiff = 0;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date currentTime = Calendar.getInstance().getTime();


        try {

            Date finishDateTime = simpleDateFormat.parse(finishDateTimeString);
            long diff = finishDateTime.getTime() - currentTime.getTime();
            hourDiff = (int) TimeUnit.MILLISECONDS.toHours(diff);

            if (hourDiff == 0 && finishDateTime.before(currentTime))
                hourDiff = -1;

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return hourDiff;

    }


    public static int getHourDiffInMinutes(int orbit, int selectionType) {
        int hourDiff = 0;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        Calendar currentTimeCalendar = Calendar.getInstance();
        Calendar endTimeCalendar = Calendar.getInstance();
        endTimeCalendar.getFirstDayOfWeek();

        switch (selectionType) {
            case 0:
                break;
            case 1:
                endTimeCalendar.add(Calendar.DAY_OF_MONTH, orbit);
                break;
            case 2:
                endTimeCalendar.add(Calendar.WEEK_OF_MONTH, orbit);
                endTimeCalendar.set(Calendar.DAY_OF_WEEK, endTimeCalendar.getActualMaximum(Calendar.DAY_OF_WEEK));
                break;
            case 3:
                endTimeCalendar.add(Calendar.MONTH, orbit);
                endTimeCalendar.set(Calendar.DAY_OF_MONTH, endTimeCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                break;
            case 4:
                endTimeCalendar.add(Calendar.YEAR, orbit);
                endTimeCalendar.set(Calendar.DAY_OF_YEAR, endTimeCalendar.getActualMaximum(Calendar.DAY_OF_YEAR));
                break;
        }


        try {
            Date date1 = simpleDateFormat.parse(new SimpleDateFormat("dd/MM/yyyy").format(endTimeCalendar.getTime()) + " 12:00:00");
            Date date2 = simpleDateFormat.parse(new SimpleDateFormat("dd/MM/yyyy").format(endTimeCalendar.getTime()) + " 23:59:55");

            if (orbit == 0)
                hourDiff = (int) TimeUnit.MILLISECONDS.toMinutes(date2.getTime() - currentTimeCalendar.getTimeInMillis());
            else
                hourDiff = (int) TimeUnit.MILLISECONDS.toMinutes(date2.getTime() - date1.getTime());

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return hourDiff;

    }

    public static int getDayDiffInMinute(int week) {
        Calendar currentTimeCalendar = Calendar.getInstance();
        Calendar endTimeCalendar = Calendar.getInstance();

        endTimeCalendar.add(Calendar.WEEK_OF_MONTH, week);

        int hourDiff = 0;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");

        try {
            Date date2 = simpleDateFormat.parse(new SimpleDateFormat("dd/MM/yyyy").format(endTimeCalendar.getTime()) + " 23:59:55");
            hourDiff = (int) TimeUnit.MILLISECONDS.toMinutes(date2.getTime() - currentTimeCalendar.getTimeInMillis());

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return hourDiff;
    }


    public static int getHourDiffFromFinishDateTimeInMinutes(String finishDateTimeString, int orbit, int selectionType) {
        int hourDiff = 0;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Calendar currentTimeCalendar = Calendar.getInstance();
        //  currentTimeCalendar.add(Calendar.DAY_OF_MONTH,orbit);
        switch (selectionType) {
            case 0:
                break;
            case 1:
                currentTimeCalendar.add(Calendar.DAY_OF_MONTH, orbit);
                break;
            case 2:
                currentTimeCalendar.add(Calendar.WEEK_OF_MONTH, orbit);
                break;
            case 3:
                currentTimeCalendar.add(Calendar.MONTH, orbit);
                break;
            case 4:
                currentTimeCalendar.add(Calendar.YEAR, orbit);
                break;
        }

        try {
            Date date1 = simpleDateFormat.parse(new SimpleDateFormat("yyyy-MM-dd").format(currentTimeCalendar.getTime()) + " 01:00:00");

            Date finishDateTime = simpleDateFormat.parse(finishDateTimeString);

            if (orbit == 0)
                hourDiff = (int) TimeUnit.MILLISECONDS.toMinutes(finishDateTime.getTime() - currentTimeCalendar.getTimeInMillis());
            else
                hourDiff = (int) TimeUnit.MILLISECONDS.toMinutes(finishDateTime.getTime() - date1.getTime());

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return hourDiff;
    }

    public static int getHourDiff(int day) {
        Calendar currentTimeCalendar = Calendar.getInstance();
        Calendar endTimeCalendar = Calendar.getInstance();

        endTimeCalendar.add(Calendar.DAY_OF_MONTH, day);

        int hourDiff = 0;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");

        try {
            Date date2 = simpleDateFormat.parse(new SimpleDateFormat("dd/MM/yyyy").format(endTimeCalendar.getTime()) + " 23:59:55");
            hourDiff = (int) TimeUnit.MILLISECONDS.toMinutes(date2.getTime() - currentTimeCalendar.getTimeInMillis());

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return hourDiff;
    }


    public static boolean isBetween(int x, int lower, int upper) {
        return lower <= x && x <= upper;
    }

    public static boolean isBetweenMinus(int x, int lower, int upper) {
        return lower >= x && x >= upper;
    }

    public static int getDayDiff(Date currentDate, Date finishDate) {
        int daysBetween = 0;

        long difference = finishDate.getTime() - currentDate.getTime();
        daysBetween = (int) TimeUnit.MILLISECONDS.toDays(difference);

        if (daysBetween == 0 && finishDate.before(currentDate))
            daysBetween = -1;


        return daysBetween;
    }

    public static int getDayDiffFromStartDate(Date currentDate, Date starDate) {
        int daysBetween = 0;

        long difference = starDate.getTime() - currentDate.getTime();
        daysBetween = (int) TimeUnit.MILLISECONDS.toDays(difference);

    /*    if (daysBetween == 0 && finishDate.before(currentDate))
            daysBetween = -1;
*/

        return daysBetween;
    }

    public static int getDayDiff(String currentDate, String finishDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        int daysBetween = 0;
        try {
            Date dateCurrent = simpleDateFormat.parse(currentDate);
            Date dateFinish = simpleDateFormat.parse(finishDate);
            long difference = dateFinish.getTime() - dateCurrent.getTime();
            daysBetween = (int) TimeUnit.MILLISECONDS.toDays(difference);

            if (daysBetween == 0 && dateFinish.before(dateCurrent))
                daysBetween = -1;

            //daysBetween = (int) (difference / (1000 * 60 * 60 * 24));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return daysBetween;
    }

    public static int getWeekDiff(Date currentDate, Date finishDate) {
        int hourDiff = 0, daysDiff = 0, weekDiff = 0;
        /*LocalDate dateCurrent= LocalDate.parse(currentDate);
        LocalDate dateFinish=LocalDate.parse(finishDate);

        weekDiff=  Weeks.weeksBetween(currentDate,finishDate).getWeeks();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");*/


        long difference = finishDate.getTime() - currentDate.getTime();
        hourDiff = (int) TimeUnit.MILLISECONDS.toHours(difference);
        daysDiff = (int) TimeUnit.MILLISECONDS.toDays(difference);

        /*if(weekDiff==-1 || weekDiff==-2 || weekDiff==-3 || weekDiff==-4 || weekDiff==-5 || weekDiff==-6 || weekDiff==-7|| weekDiff ==-8)
        return -1;
        else weekDiff=daysDiff/7;*/


        if (hourDiff < 0 && daysDiff >= -7) {
            return -1;
        } else weekDiff = daysDiff / 7;

       /* if(weekDiff==0)
            return 0;
        else if (weekDiff / 7 == -1 && weekDiff % 7 == 0)
            return -1;
        else if (weekDiff % 7 != 0 && finishDate.before(currentDate))
            return ((weekDiff / 7) - (1));
        else if (weekDiff / 7 == 0)
            return -1;*/


        return weekDiff;

    }

    public static int getWeekDiff(String currentDate, String finishDate) {
        int weekDiff = 0;
       /* LocalDate dateCurrent= LocalDate.parse(currentDate);
        LocalDate dateFinish=LocalDate.parse(finishDate);

        weekDiff=  Weeks.weeksBetween(dateCurrent,dateFinish).getWeeks();*/
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");


        try {
            Date dateCurrent = simpleDateFormat.parse(currentDate);
            Date dateFinish = simpleDateFormat.parse(finishDate);
            long difference = dateFinish.getTime() - dateCurrent.getTime();
            weekDiff = (int) (difference / (1000 * 60 * 60 * 24));
            weekDiff = weekDiff / 7;

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return weekDiff;
    }


    public static int getMonthDiff(String currentDate, String finishDate) {
        int monthDiff = 0, dayDiff = 0;

        LocalDate dateCurrent = LocalDate.parse(currentDate);
        LocalDate dateFinish = LocalDate.parse(finishDate);
        dayDiff = Days.daysBetween(dateCurrent, dateFinish).getDays();

        monthDiff = Months.monthsBetween(dateCurrent, dateFinish).getMonths();
        if (isBetweenMinus(dayDiff, -1, -30))
            monthDiff = -1;


        return monthDiff;
    }

    public static int getYearDiff(String currentDate, String finishDate) {
        int yearDiff = 0, dayDiff = 0;

        LocalDate dateCurrent = LocalDate.parse(currentDate);
        LocalDate dateFinish = LocalDate.parse(finishDate);
        dayDiff = Days.daysBetween(dateCurrent, dateFinish).getDays();

        yearDiff = Years.yearsBetween(dateCurrent, dateFinish).getYears();
        if (isBetweenMinus(dayDiff, -1, -365))
            yearDiff = -1;


        return yearDiff;



        /*SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        int yearDiff = 0;

        Calendar currentCal = new GregorianCalendar();
        Calendar finishCal = new GregorianCalendar();

        try {
            Date dateCurrent = simpleDateFormat.parse(currentDate);
            Date dateFinish = simpleDateFormat.parse(finishDate);

            currentCal.setTime(dateCurrent);
            finishCal.setTime(dateFinish);
            yearDiff = (int) ((dateFinish.getTime() - dateCurrent.getTime()) / (1000l * 60 * 60 * 24 * 365));
            //yearDiff = finishCal.get(Calendar.YEAR) - currentCal.get(Calendar.YEAR);
        } catch (ParseException e) {
            e.printStackTrace();
        }*/

    }

    public static int dateComparision(String currentDate, String finishDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        int yearDiff = 0;

        Calendar currentCal = new GregorianCalendar();
        Calendar finishCal = new GregorianCalendar();

        try {
            Date dateCurrent = simpleDateFormat.parse(currentDate);
            Date dateFinish = simpleDateFormat.parse(finishDate);

            currentCal.setTime(dateCurrent);
            finishCal.setTime(dateFinish);

            if (currentCal.before(finishCal))
                return 1;
            else return 0;

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return yearDiff;
    }

    public static float dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    public static boolean checkRecordAudio(Context context) {
        String permission = Manifest.permission.RECORD_AUDIO;
        int res = context.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public static boolean checkWriteExternalStoragePermission(Context context) {
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int res = context.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }
    public static boolean checkCameraPermission(Context context) {
        String permission = Manifest.permission.CAMERA;
        int res = context.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public static void showPermissionDeniedToast(Context context, String permissionName) {
        if (permissionName.equalsIgnoreCase("android.permission.CAMERA"))
            AndroidUtils.showToast(context, "Permission Denied for Camera.");
        else if (permissionName.equalsIgnoreCase("android.permission.READ_EXTERNAL_STORAGE"))
            AndroidUtils.showToast(context, "Permission Denied for Storage.");
        else if (permissionName.equalsIgnoreCase("android.permission.WRITE_EXTERNAL_STORAGE"))
            AndroidUtils.showToast(context, "Permission Denied for Storage.");
        else if (permissionName.equalsIgnoreCase("android.permission.RECORD_AUDIO"))
            AndroidUtils.showToast(context, "Permission Denied for Audio.");
    }

    public static void showPermanentPermissionDeniedToast(Context context, String permissionName) {
        if (permissionName.equalsIgnoreCase("android.permission.CAMERA"))
            AndroidUtils.showToast(context, "Please allow camera  permission from Settings.");
        else if (permissionName.equalsIgnoreCase("android.permission.READ_EXTERNAL_STORAGE"))
            AndroidUtils.showToast(context, "Please allow storage permission from Settings.");
        else if (permissionName.equalsIgnoreCase("android.permission.WRITE_EXTERNAL_STORAGE"))
            AndroidUtils.showToast(context, "Please allow storage permission from Settings.");
        else if (permissionName.equalsIgnoreCase("android.permission.RECORD_AUDIO"))
            AndroidUtils.showToast(context, "Please allow record audio permission from Settings.");
    }
}
