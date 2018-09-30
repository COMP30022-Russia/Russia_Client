package com.comp30022.team_russia.assist.base.di;

import com.comp30022.team_russia.assist.features.push.services.RussiaFirebaseService;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * Dagger module for Android Services.
 */
@Module
public abstract class ServicesModule {
    @ContributesAndroidInjector()
    abstract RussiaFirebaseService contributeRussiaFirebaseService();
}
