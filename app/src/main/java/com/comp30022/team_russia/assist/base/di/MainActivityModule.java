package com.comp30022.team_russia.assist.base.di;

import com.comp30022.team_russia.assist.MainActivity;
import com.comp30022.team_russia.assist.features.login.FragmentsModule;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class MainActivityModule {
    @ContributesAndroidInjector(modules = FragmentsModule.class)
    abstract MainActivity contributeMainActivity();
}
