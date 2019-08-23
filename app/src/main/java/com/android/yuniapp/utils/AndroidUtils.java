package com.android.yuniapp.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AndroidUtils {

    public static void hideKeyBoard(Context context) {
        InputMethodManager manager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(((Activity) context).getCurrentFocus()
                .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static void showKeyBoard(Context context) {
        ((Activity) context).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    public static void getHashKey(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.e("MY_KEY_HASH:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (NameNotFoundException e) {
        } catch (NoSuchAlgorithmException e) {
        }
    }

    public static String getCountryCodeFromConfiguration(Context context) {
        return context.getResources().getConfiguration().locale.getCountry();
    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        Pattern emailPattern = Pattern
                .compile("^(\\+\\d{1,2}\\s)?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}$");
        Matcher m = emailPattern.matcher(phoneNumber);
        return m.matches();
    }

    public static boolean isValidEmailAddress(String email) {
        Pattern emailPattern = Pattern
                .compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$");
        Matcher m = emailPattern.matcher(email);
        return m.matches();
    }



    public static boolean isValidSSNNo(String ssnNo) {
        Pattern ssnPattern = Pattern.compile("^(?!\\b(\\d)\\1+-(\\d)\\1+-(\\d)\\1+\\b)(?!123-45-6789|219-09-9999|078-05-1120)(?!666|000|9\\d{2})\\d{3}-(?!00)\\d{2}-(?!0{4})\\d{4}$");
        Matcher m = ssnPattern.matcher(ssnNo);
        return m.matches();
    }


    public static boolean isValidEINNo(String ssnNo) {

        Pattern einPattern = Pattern
                .compile( "^([07][1-7]|1[0-6]|2[0-7]|[35][0-9]|[468][0-8]|9[0-589])-?\\d{7}$");
        Matcher m = einPattern.matcher(ssnNo);
        return m.matches();
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showLongToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static String convertSecondsToHMmSs(long seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return String.format("0%d:%02d:%02d", h, m, s);
    }

    public static String getTimeZoneId() {
        TimeZone timeZone = TimeZone.getDefault();
        return timeZone.getID();
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static String getStringFromHtmlString(String htmlString) {

        return Html.fromHtml(htmlString).toString();

    }

    public static String getAppVersionName(Context context)
            throws NameNotFoundException {
        PackageManager manager = context.getPackageManager();
        PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
        return info.versionName;
    }

    public static void OpenDialer(Context context, String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        context.startActivity(Intent.createChooser(intent, "Dial Number"));
    }

    public static void OpenEmailClient(Context context, String email,
                                       String subject, String body) throws UnsupportedEncodingException {

        if (subject == null) {
            subject = "";
        }

        if (body == null) {
            body = "";
        }
        String uriText = "mailto:" + email + "?subject="
                + URLEncoder.encode(subject, "UTF-8") + "&body="
                + URLEncoder.encode(body, "UTF-8");

        Uri uri = Uri.parse(uriText);

        Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
        sendIntent.setData(uri);
        context.startActivity(Intent.createChooser(sendIntent, "Send email"));
    }

    public static void setCustomFont(Context context, TextView view,
                                     String fontName) {
        Typeface custom_font = Typeface.createFromAsset(context.getAssets(),
                fontName);
        view.setTypeface(custom_font);
    }

    public static void openPlayStoreLink(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(intent);
    }


    public static void setLetterBig(TextView textView, int i, int j) {
        Spannable span = new SpannableString(textView.getText());
        span.setSpan(new RelativeSizeSpan(1.4f), i, j,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(span);
    }


    public static String milliToString(long millis) {

        long hrs = TimeUnit.MILLISECONDS.toHours(millis) % 24;
        long min = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long sec = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        // millis = millis - (hrs * 60 * 60 * 1000); //alternative way
        // millis = millis - (min * 60 * 1000);
        // millis = millis - (sec * 1000);
        // long mls = millis ;
        long mls = millis % 1000;
        String toRet = hrs > 0 ? String.format("%02d:%02d:%02d", hrs, min, sec) : String.format("%02d:%02d", min, sec);
        // System.out.println(toRet);
        return toRet;
    }

    public static void shareApp(Context context, String title, String url) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
         share.putExtra(Intent.EXTRA_SUBJECT, title);
        share.putExtra(Intent.EXTRA_TEXT, url);

        context.startActivity(Intent.createChooser(share, "Share Link:"));
    }
    public static String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    public static int getProgressPercentage(long currentDuration, long totalDuration) {
        Double percentage = (double) 0;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        // calculating percentage
        percentage = (((double) currentSeconds) / totalSeconds) * 100;

        // return percentage
        return percentage.intValue();
    }


    public static int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = (int) (totalDuration / 1000);
        currentDuration = (int) ((((double) progress) / 100) * totalDuration);

        // return current duration in milliseconds
        return currentDuration * 1000;
    }


    public static void openWebView(Context context, WebView webView, String url, final ProgressBar progressBar) {
        webView.getSettings().setJavaScriptEnabled(true);
    /*    webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setLoadWithOverviewMode(false);
        webView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);*/
        webView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView webview, String url) {
                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
            }
        });
        webView.loadUrl(url);
    }
    public static String getLocationName(Context context, Location location) {
        try {
            String locationName = "";
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(context, Locale.getDefault());
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            locationName = (addresses.get(0).getSubLocality() == null ? "" : addresses.get(0).getSubLocality() + ", ") +
                    (addresses.get(0).getLocality() == null ? "" : addresses.get(0).getLocality() + ", ")
                    + (addresses.get(0).getAdminArea() == null ? "" : addresses.get(0).getAdminArea() + ", ")
                    + (addresses.get(0).getCountryName() == null ? "" : addresses.get(0).getCountryName());


            return locationName;


        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    public static String getCityName(Context context, Location location) {
        try {
            String cityName = "";
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(context, Locale.getDefault());
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            cityName = (addresses.get(0).getLocality() == null ? "" : addresses.get(0).getLocality() );


            return cityName;


        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getCountryName(Context context, Location location) {
        try {
            String countryName = "";
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(context, Locale.getDefault());
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            countryName = (addresses.get(0).getCountryName() == null ? "" : addresses.get(0).getCountryName() );


            return countryName;


        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    public static String getPostalCode(Context context, Location location) {
        try {
            String postalCode = "";
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(context, Locale.getDefault());
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            postalCode = (addresses.get(0).getPostalCode() == null ? "" : addresses.get(0).getPostalCode() );


            return postalCode;


        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getState(Context context, Location location) {
        try {
            String stateName = "";
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(context, Locale.getDefault());
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            stateName = (addresses.get(0).getAdminArea() == null ? "" : addresses.get(0).getAdminArea() );


            return stateName;


        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    public static Double getDistanceBwTwoLocation(double lat1, double lon1, double lat2, double lon2){
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        DecimalFormat decimalFormat=new DecimalFormat("##.00");
        dist=dist*1000;

        return  Math.round(dist * 100.0) / 100.0;


    }
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    public static  boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }
        return isInBackground;
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension =url.substring(url.lastIndexOf(".")+1); /*MimeTypeMap.getFileExtensionFromUrl(url);*/

        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }
    public static int getScreenWidth(Context context) {
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    public static float convertPixelsToDIP(Context context, int pixels) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return pixels / (displayMetrics.densityDpi / 160f);
    }
    public static int convertDIPToPixels(Context context, float dip) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, displayMetrics);
    }

    public static Calendar getCalendarDateObject(Date date) {


        Calendar calendar = Calendar.getInstance();

        try {
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return calendar;
    }

    public static Calendar getCalendarTimeObject(Date date) {


        Calendar calendar = Calendar.getInstance();

        try {
            calendar.setTime(date);
            calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
            calendar.set(Calendar.MONTH,  Calendar.getInstance().get(Calendar.MONTH));
            calendar.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return calendar;
    }

    public static String getTimeZoneOffset()
    {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"),
                Locale.getDefault());
        Date currentLocalTime = calendar.getTime();
        DateFormat date = new SimpleDateFormat("ZZZZZ",Locale.getDefault());
        String localTime = date.format(currentLocalTime);
        return localTime;
    }


}



