package com.comp30022.team_russia.assist.base.di;

import com.comp30022.team_russia.assist.HomeContactListActivity;
import com.comp30022.team_russia.assist.LoginActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivitiesModule {
    @ContributesAndroidInjector()
    abstract LoginActivity contributeLoginActivity();

    @ContributesAndroidInjector()
    abstract HomeContactListActivity contributeHomeContactListActivity();
}
