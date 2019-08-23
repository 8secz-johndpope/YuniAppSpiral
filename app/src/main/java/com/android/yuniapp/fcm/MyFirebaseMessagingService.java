package com.android.yuniapp.fcm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.android.yuniapp.R;
import com.android.yuniapp.activity.ChatActivity;
import com.android.yuniapp.activity.HomeActivity;
import com.android.yuniapp.activity.SplashActivity;
import com.android.yuniapp.utils.ConstantUtils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import io.agora.openvcall.model.ConstantApp;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String NOTIFICATION_TYPE = "notification_type";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Intent intent;
    private String _type, description;
    private PendingIntent resultPendingIntent;
    private NotificationCompat.Builder notification;
    private NotificationManager manager;

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        sharedPreferences = getSharedPreferences(ConstantUtils.APP_PREF, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString(ConstantUtils.DEVICE_TOKEN, s).commit();


    }

    private int generateUniqueId() {
        return (int) System.currentTimeMillis();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String id = "channel" + generateUniqueId();
        if (remoteMessage.getData().get(NOTIFICATION_TYPE) != null &&
                remoteMessage.getData().get(NOTIFICATION_TYPE).equalsIgnoreCase("C")) {
//            request for spot status received
            intent = new Intent(this, ChatActivity.class);
            _type = "C";
            description = "Message Received";
            messageRecievedNotification(remoteMessage, id);
//            return;
        } else if (remoteMessage.getData().get(NOTIFICATION_TYPE) != null &&
                remoteMessage.getData().get(NOTIFICATION_TYPE).equalsIgnoreCase("M")) {
//            Member Added Notification Received

            sendBroadcast(new Intent(ConstantUtils.BROADCAST_KEY)
                    .putExtra(ConstantUtils.ENTITY_TYPE, remoteMessage.getData().get(ConstantUtils.OBJECT_TYPE))
                    .putExtra(ConstantUtils.STAR_NAME, remoteMessage.getData().get(ConstantUtils.OBJECT_NAME)));
            intent = new Intent(this, HomeActivity.class);
            _type = "M";
            description = "Member Added";
            memberAddedNotification(remoteMessage, id);
        } else if (remoteMessage.getData().get(NOTIFICATION_TYPE) != null &&
                remoteMessage.getData().get(NOTIFICATION_TYPE).equalsIgnoreCase("VC")) {
//            Member Added Notification Received

            intent = new Intent(this, SplashActivity.class);
            intent.putExtra(ConstantApp.ACTION_KEY_CHANNEL_NAME, remoteMessage.getData().get("chanel_name"));
            intent.putExtra(ConstantApp.FROM_NOTIFICATION, true);
            _type = "VC";
            description = "Video Call";
            videoCallNotification(remoteMessage, id);
        } else if (remoteMessage.getData().get(NOTIFICATION_TYPE) != null &&
                remoteMessage.getData().get(NOTIFICATION_TYPE).equalsIgnoreCase("VD")) {
//            Disconnect video call notification received
            sendBroadcast(new Intent(ConstantUtils.DISCONNECT_BROADCAST_KEY));

        } /*else
            sampleNotification(remoteMessage, id);*/

    }

    private void disconnectVideoCallNotification(RemoteMessage remoteMessage, String id) {

    }

    private void videoCallNotification(RemoteMessage remoteMessage, String id) {
        String name = remoteMessage.getData().get("title");
        String message = remoteMessage.getData().get("body");
        buildNotificationChannel(id, true);

        buildPendingIntent(id);

//        buildProfileImageBmp(remoteMessage.getData().get("profile_pic"));
        notification.setStyle(new NotificationCompat.BigTextStyle().bigText(message)).setContentTitle(name)
                .setContentText(message);
        try {
            notification.setLights(Color.BLUE, 500, 500);
            long[] pattern = {100, 200, 300, 400};
            notification.setVibrate(pattern);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Notification notifi = notification.build();
        notifi.flags |= Notification.FLAG_AUTO_CANCEL;
        manager.notify(/*notification id*/ConstantUtils.VIDEO_CALL_NOTIFICATION, notifi);
    }

    private void sampleNotification(RemoteMessage remoteMessage, String id) {


        intent = new Intent(this, HomeActivity.class);
        String name = remoteMessage.getData().get("title");
        String message = remoteMessage.getData().get("body");
        buildNotificationChannel(id, true);
        buildPendingIntent(id);
        notification.setStyle(new NotificationCompat.BigTextStyle().bigText(message)).setContentTitle(name)
                .setContentText(message);
        try {
            notification.setLights(Color.BLUE, 500, 500);
            long[] pattern = {100, 200, 300, 400};
            notification.setVibrate(pattern);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Notification notifi = notification.build();
        notifi.flags |= Notification.FLAG_AUTO_CANCEL;
        manager.notify(/*notification id*/1421, notifi);
    }

    private void buildNotificationChannel(String id, boolean ifVibrate) {
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(id,
                    "Yuni",
                    NotificationManager.IMPORTANCE_DEFAULT);
            if (ifVibrate) {
                channel.enableLights(true);
                channel.setLightColor(Color.BLUE);
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{100, 200, 300, 400});
            }
            channel.setDescription(description);
            manager.createNotificationChannel(channel);
        }
    }

    private void messageRecievedNotification(RemoteMessage remoteMessage, String id) {
        String name = remoteMessage.getData().get("title");
        String message = remoteMessage.getData().get("body");

        String ENTITYID = remoteMessage.getData().get("object_id");
        String ENTITYTYPE = remoteMessage.getData().get("object_type");
        intent.putExtra(ConstantUtils.ENTITY_ID, ENTITYID);
        intent.putExtra(ConstantUtils.ENTITY_TYPE, ENTITYTYPE);
        buildNotificationChannel(id, true);

        buildPendingIntent(id);

//        buildProfileImageBmp(remoteMessage.getData().get("profile_pic"));
        notification.setStyle(new NotificationCompat.BigTextStyle().bigText(message)).setContentTitle(name)
                .setContentText(message);
        try {
            notification.setLights(Color.BLUE, 500, 500);
            long[] pattern = {100, 200, 300, 400};
            notification.setVibrate(pattern);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Notification notifi = notification.build();
        notifi.flags |= Notification.FLAG_AUTO_CANCEL;
        manager.notify(/*notification id*/ConstantUtils.MESSAGE_ARRIVED, notifi);
    }

    private void buildPendingIntent(String id) {
        intent.putExtra(ConstantUtils.FROMNOTIFICATION, true);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(intent);
// Get the PendingIntent containing the entire back stack
        resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//        PendingIntent dismissIntent = NotificationActivity.getDismissIntent(22, this);
        notification = new NotificationCompat.Builder(this, id)
                .setColor(getResources().getColor(R.color.colorPrimary))
//                .setContentText(message)
                .setContentIntent(resultPendingIntent)
//                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setSmallIcon(R.mipmap.notification_icon)
//                .setOngoing(true)
                .setAutoCancel(true);
    }

    private void memberAddedNotification(RemoteMessage remoteMessage, String id) {
        String name = remoteMessage.getData().get("title");
        String message = remoteMessage.getData().get("body");
        buildNotificationChannel(id, true);

        buildPendingIntent(id);

//        buildProfileImageBmp(remoteMessage.getData().get("profile_pic"));
        notification.setStyle(new NotificationCompat.BigTextStyle().bigText(message)).setContentTitle(name)
                .setContentText(message);
        try {
            notification.setLights(Color.BLUE, 500, 500);
            long[] pattern = {100, 200, 300, 400};
            notification.setVibrate(pattern);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Notification notifi = notification.build();
        notifi.flags |= Notification.FLAG_AUTO_CANCEL;
        manager.notify(/*notification id*/ConstantUtils.MEMBER_ADDED, notifi);
    }

    private void showNotification(String body, String title) {
        Intent notificationIntent = new Intent(this, HomeActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, "1")
                        .setSmallIcon(R.mipmap.icon)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setAutoCancel(true)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setContentIntent(pendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("1",
                    "Yuniapp",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
    }
}
