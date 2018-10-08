package com.comp30022.team_russia.assist.features.push.sys;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.ServiceLifecycleDispatcher;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.push.PushModule;
import com.comp30022.team_russia.assist.features.push.models.FirebaseTokenData;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.Map;

/**
 * Background service for receiving Firebase Cloud Messaging messages.
 */
public class RussiaFirebaseService extends FirebaseMessagingService implements LifecycleOwner {

    private static final String TAG = RussiaFirebaseService.class.getSimpleName();

    private final Gson gson = new Gson();

    private ServiceLifecycleDispatcher dispatcher = new ServiceLifecycleDispatcher(this);

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Created");
    }

    @Override
    public void onNewToken(String s) {
        Log.d(TAG, "onNewToken");
        FirebaseInstanceId.getInstance().getInstanceId()
            .addOnSuccessListener(this::updateFirebaseToken);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived");

        Map<String, String> payload = remoteMessage.getData();

        if (payload.containsKey("type") && payload.containsKey("data")) {
            String type = payload.get("type");
            String data = payload.get("data");

            Intent intent = new Intent()
                .setAction(PushModule.FIREBASE_BROADCAST_ACTION_DATA)
                .putExtra("type", type)
                .putExtra("data", data);

            sendBroadcastBoth(intent);
        }
    }


    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return this.dispatcher.getLifecycle();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    private void updateFirebaseToken(InstanceIdResult instanceIdResult) {
        FirebaseTokenData tokenData = new FirebaseTokenData(
            instanceIdResult.getId(),
            instanceIdResult.getToken());

        Intent intent = new Intent()
            .setAction(PushModule.FIREBASE_BROADCAST_ACTION_TOKEN)
            .putExtra("data", gson.toJson(tokenData));

        sendBroadcastBoth(intent);
    }

    private void sendBroadcastBoth(Intent intent) {
        Log.d(TAG, "sending broadcast");
        // For when the app is not running.
        sendBroadcast(intent);
        // For when the app is running.
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
