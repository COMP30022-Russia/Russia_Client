package com.comp30022.team_russia.assist;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.support.multidex.MultiDexApplication;
import android.support.v4.content.LocalBroadcastManager;

import com.comp30022.team_russia.assist.base.di.AppInjector;
import com.comp30022.team_russia.assist.base.persist.KeyValueStore;
import com.comp30022.team_russia.assist.features.push.PubSubTopics;
import com.comp30022.team_russia.assist.features.push.PushModule;
import com.comp30022.team_russia.assist.features.push.models.NewMessagePushNotification;
import com.comp30022.team_russia.assist.features.push.services.PayloadToObjectConverter;
import com.comp30022.team_russia.assist.features.push.services.PubSubHub;
import com.comp30022.team_russia.assist.features.push.sys.FirebaseBroadcastReceiver;

import com.google.gson.Gson;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasBroadcastReceiverInjector;
import dagger.android.HasServiceInjector;

import java.io.IOException;

import javax.inject.Inject;

/**
 * The application context.
 */
public class RussiaApplication extends MultiDexApplication
    implements HasActivityInjector, HasServiceInjector, HasBroadcastReceiverInjector {

    @Inject
    DispatchingAndroidInjector<Activity> dispatchingAndroidActivityInjector;

    @Inject
    DispatchingAndroidInjector<Service> dispatchingAndroidServiceInjector;

    @Inject
    DispatchingAndroidInjector<BroadcastReceiver> dispatchingAndroidBroadcastReceiverInjector;

    @Inject
    KeyValueStore keyValueStore;

    @Inject
    PubSubHub pubSubHub;

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialise configuration manager
        try {
            ConfigurationManager.createInstance(
                this.getApplicationContext().getAssets().open(
                    ConfigurationManager.CONFIG_FILENAME));
        } catch (IOException e) {
            e.printStackTrace();
        }
        AppInjector.init(this);
        keyValueStore.initialise(this);


        configurePubSubTopics();
        registerFirebaseBroadcastReceiver();
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchingAndroidActivityInjector;
    }

    @Override
    public AndroidInjector<Service> serviceInjector() {
        return dispatchingAndroidServiceInjector;
    }

    @Override
    public AndroidInjector<BroadcastReceiver> broadcastReceiverInjector() {
        return dispatchingAndroidBroadcastReceiverInjector;
    }

    private void registerFirebaseBroadcastReceiver() {
        BroadcastReceiver br = new FirebaseBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(PushModule.FIREBASE_BROADCAST_ACTION_DATA);
        filter.addAction(PushModule.FIREBASE_BROADCAST_ACTION_TOKEN);
        LocalBroadcastManager.getInstance(this).registerReceiver(br, filter);
    }
    
    /**
     * PubSub topics that are used throughout the app, as opposed to only in certain
     * fragments.
     */
    private void configurePubSubTopics() {
        this.pubSubHub.configureTopic(PubSubTopics.NEW_MESSAGE, NewMessagePushNotification.class,
            new PayloadToObjectConverter<NewMessagePushNotification>() {
                private Gson gson = new Gson();
                @Override
                public NewMessagePushNotification fromString(String payloadStr) {
                    return gson.fromJson(payloadStr, NewMessagePushNotification.class);
                }

                @Override
                public String toString(NewMessagePushNotification payload) {
                    // not used. not implemented.
                    return null;
                }
            });
    }
}
