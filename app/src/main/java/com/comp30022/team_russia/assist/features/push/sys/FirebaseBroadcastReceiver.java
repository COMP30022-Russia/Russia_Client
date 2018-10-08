package com.comp30022.team_russia.assist.features.push.sys;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.comp30022.team_russia.assist.features.push.PubSubTopics;
import com.comp30022.team_russia.assist.features.push.PushModule;
import com.comp30022.team_russia.assist.features.push.models.FirebaseTokenData;
import com.comp30022.team_russia.assist.features.push.services.PubSubHub;

import com.google.gson.Gson;

import dagger.android.DaggerBroadcastReceiver;

import javax.inject.Inject;

/**
 * An Android BroadcastReceiver for receiving broadcasts from RussiaFirebaseService.
 */
public class FirebaseBroadcastReceiver extends DaggerBroadcastReceiver {

    private static final String TAG = FirebaseBroadcastReceiver.class.getSimpleName();

    private final Gson gson = new Gson();

    @Inject
    PubSubHub pubSubHub;


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        try {
            processIntent(intent);
            Log.d(TAG, "Done processing intent");
        } catch (Exception e) {
            Log.e(TAG, "Error processing intent");
            e.printStackTrace();
        }

    }

    private void processIntent(Intent intent) {
        String actionType = intent.getAction();

        if (actionType == null) {
            Log.w(TAG, "Received an Intent with null action");
            return;
        }

        if (actionType.equals(PushModule.FIREBASE_BROADCAST_ACTION_TOKEN)) {
            Log.d(TAG, "Received a Firebase token update broadcast");

            String payload = intent.getStringExtra("data");
            FirebaseTokenData data = gson.fromJson(payload, FirebaseTokenData.class);

            pubSubHub.publish(PubSubTopics.FIREBASE_TOKEN, data);

        } else if (actionType.equals(PushModule.FIREBASE_BROADCAST_ACTION_DATA)) {
            Log.d(TAG, "Received a Firebase data broadcast");

            String type = intent.getStringExtra("type");
            String data = intent.getStringExtra("data");

            pubSubHub.publish(type, data);
        }
    }
}
