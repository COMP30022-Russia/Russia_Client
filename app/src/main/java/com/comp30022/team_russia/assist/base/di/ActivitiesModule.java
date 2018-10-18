package com.comp30022.team_russia.assist.base.di;

import com.comp30022.team_russia.assist.HomeContactListActivity;
import com.comp30022.team_russia.assist.LoginActivity;
import com.comp30022.team_russia.assist.NavigationRequestActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * Dagger module for all the Activity classes in the app.
 */
@Module
public abstract class ActivitiesModule {
    @ContributesAndroidInjector()
    abstract LoginActivity contributeLoginActivity();

    @ContributesAndroidInjector()
    abstract HomeContactListActivity contributeHomeContactListActivity();

    @ContributesAndroidInjector()
    abstract NavigationRequestActivity contributeNavigationRequestActivity();
}
