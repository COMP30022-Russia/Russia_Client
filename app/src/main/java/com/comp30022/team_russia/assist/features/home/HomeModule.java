package com.comp30022.team_russia.assist.features.home;

import android.arch.lifecycle.ViewModel;

import com.comp30022.team_russia.assist.base.di.ViewModelKey;
import com.comp30022.team_russia.assist.features.home.ui.HomeContactFragment;
import com.comp30022.team_russia.assist.features.home.ui.HomeFragment;
import com.comp30022.team_russia.assist.features.home.vm.HomeContactViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import dagger.multibindings.IntoMap;

/**
 * (Dependency Injection) Top-level Dagger module for the Home screen/contact
 * list-related feature area.
 */
@SuppressWarnings("unused")
@Module
public abstract class HomeModule {

    // ViewModel
    @Binds
    @IntoMap
    @ViewModelKey(HomeContactViewModel.class)
    abstract ViewModel bindHomeContactViewModel(
        HomeContactViewModel homeContactViewModel);

    // Fragments
    @ContributesAndroidInjector
    public abstract HomeContactFragment contributeHomeContactFragment();

    @ContributesAndroidInjector
    public abstract HomeFragment contributeHomeFragment();

}
