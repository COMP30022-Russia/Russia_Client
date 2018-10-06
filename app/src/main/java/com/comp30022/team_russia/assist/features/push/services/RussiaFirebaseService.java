package com.comp30022.team_russia.assist.features.push.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.ServiceLifecycleDispatcher;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.push.PubSubTopics;
import com.comp30022.team_russia.assist.features.push.models.FirebaseTokenData;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import dagger.android.AndroidInjection;
import java.util.Map;

import javax.inject.Inject;

/**
 * Background service for receiving Firebase Cloud Messaging messages.
 */
public class RussiaFirebaseService extends FirebaseMessagingService implements LifecycleOwner {

    private ServiceLifecycleDispatcher dispatcher = new ServiceLifecycleDispatcher(this);

    @Inject
    AuthService authService;

    @Inject
    PubSubHub pushNotificationHub;

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("default",
                "Russia Channel",
                NotificationManager.IMPORTANCE_DEFAULT);

            channel.setDescription("Channel description");
            notificationManager.createNotificationChannel(channel);
            NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, "default");
            builder.setContentText("Firebase service is running.");
            startForeground(1, builder.build());
        }

        if (authService.isLoggedInUnboxed()) {
            // once logged in, update Firebase Token.
            FirebaseInstanceId.getInstance().getInstanceId()
                .addOnSuccessListener(this::updateFirebaseToken);

        }

        authService.isLoggedIn().observe(this, isLoggedIn -> {
            if (isLoggedIn != null && isLoggedIn) {
                // once logged in, update Firebase Token.
                FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnSuccessListener(this::updateFirebaseToken);
            }
        });

    }

    @Override
    public void onNewToken(String s) {
        FirebaseInstanceId.getInstance().getInstanceId()
            .addOnSuccessListener(this::updateFirebaseToken);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> payload = remoteMessage.getData();

        if (payload.containsKey("type") && payload.containsKey("data")) {
            String type = payload.get("type");
            String data = payload.get("data");
            pushNotificationHub.publish(type, data);
        }

    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {

        return this.dispatcher.getLifecycle();

    }

    private void updateFirebaseToken(InstanceIdResult instanceIdResult) {
        pushNotificationHub.publish(PubSubTopics.FIREBASE_TOKEN,
            new FirebaseTokenData(
                instanceIdResult.getId(),
                instanceIdResult.getToken()));
    }
}
