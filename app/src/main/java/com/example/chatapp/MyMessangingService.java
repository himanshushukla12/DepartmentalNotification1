package com.example.chatapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyMessangingService extends FirebaseMessagingService {
    public MyMessangingService() {

    }
    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Get the notification data
        String title = remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getNotification().getBody();

        // Show the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "0")
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.bitslogo)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, builder.build());
    }


    @Override
    public void onNewToken(String token) {
        // Handle the token received from Firebase
    }
}
