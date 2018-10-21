package com.comp30022.team_russia.assist.base.di;

import com.comp30022.team_russia.assist.features.jitsi.sys.JitsiPlaceholderService;
import com.comp30022.team_russia.assist.features.push.sys.RussiaFirebaseService;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * Dagger module for Android Services.
 */
@SuppressWarnings("unused")
@Module
public abstract class ServicesModule {
    @ContributesAndroidInjector()
    abstract RussiaFirebaseService contributeRussiaFirebaseService();

    @ContributesAndroidInjector()
    abstract JitsiPlaceholderService contributeJitsiService();
}
