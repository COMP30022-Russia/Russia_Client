package com.comp30022.team_russia.assist;

import android.app.Activity;
import android.app.Application;
import android.support.multidex.MultiDexApplication;

import com.comp30022.team_russia.assist.base.di.AppInjector;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;

import java.io.IOException;

import javax.inject.Inject;

/**
 * The application context.
 */
public class RussiaApplication extends MultiDexApplication implements HasActivityInjector {
    @Inject
    DispatchingAndroidInjector<Activity> dispatchingAndroidInjector;

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
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchingAndroidInjector;
    }
}
