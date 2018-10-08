package com.comp30022.team_russia.assist;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.support.multidex.MultiDexApplication;
import android.support.v4.content.LocalBroadcastManager;

import com.comp30022.team_russia.assist.base.di.AppInjector;
import com.comp30022.team_russia.assist.base.persist.KeyValueStore;
import com.comp30022.team_russia.assist.features.push.PushModule;
import com.comp30022.team_russia.assist.features.push.sys.FirebaseBroadcastReceiver;

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

    @Override
    public void onCreate() {
        super.onCreate();
        AppInjector.init(this);

        // Initialise configuration manager
        try {
            ConfigurationManager.createInstance(
                this.getApplicationContext().getAssets().open(
                    ConfigurationManager.CONFIG_FILENAME));
        } catch (IOException e) {
            e.printStackTrace();
        }

        keyValueStore.initialise(this);
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
}
