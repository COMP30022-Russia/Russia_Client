package com.comp30022.team_russia.assist.base.di;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.comp30022.team_russia.assist.RussiaApplication;

import dagger.android.AndroidInjection;
import dagger.android.support.AndroidSupportInjection;
import dagger.android.support.HasSupportFragmentInjector;

/**
 * Helper class for auto-injecting fragments into activities.
 */
public class AppInjector {
    /**
     * Method invoked by the Application to trigger injection.
     * @param russiaApp The Application.
     */
    public static void init(RussiaApplication russiaApp) {
        DaggerAppComponent.builder().application(russiaApp)
            .build().inject(russiaApp);

        russiaApp.registerActivityLifecycleCallbacks(
            new Application.ActivityLifecycleCallbacks() {
                @Override
                public void onActivityCreated(Activity activity,
                                              Bundle savedInstanceState) {
                    handleActivity(activity);
                }

                @Override
                public void onActivityStarted(Activity activity) {

                }

                @Override
                public void onActivityResumed(Activity activity) {

                }

                @Override
                public void onActivityPaused(Activity activity) {

                }

                @Override
                public void onActivityStopped(Activity activity) {

                }

                @Override
                public void onActivitySaveInstanceState(Activity activity,
                                                        Bundle outState) {

                }

                @Override
                public void onActivityDestroyed(Activity activity) {

                }
            });
    }

    private static void handleActivity(Activity activity) {
        if (activity instanceof HasSupportFragmentInjector) {
            AndroidInjection.inject(activity);
        }
        if (activity instanceof FragmentActivity) {
            ((FragmentActivity) activity).getSupportFragmentManager()
                .registerFragmentLifecycleCallbacks(
                    new FragmentManager.FragmentLifecycleCallbacks() {
                        @Override
                        public void onFragmentCreated(@NonNull FragmentManager fm,
                                                      @NonNull Fragment f,
                                                      @Nullable Bundle
                                                          savedInstanceState) {
                            if (f instanceof Injectable) {
                                AndroidSupportInjection.inject(f);
                            }
                        }
                    }, true);
        }
    }
}
