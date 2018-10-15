package com.comp30022.team_russia.assist.features.home_contacts;

import android.arch.lifecycle.ViewModel;

import com.comp30022.team_russia.assist.base.di.ViewModelKey;
import com.comp30022.team_russia.assist.features.home_contacts.ui.HomeContactFragment;
import com.comp30022.team_russia.assist.features.home_contacts.ui.HomeFragment;
import com.comp30022.team_russia.assist.features.home_contacts.vm.HomeContactViewModel;
import com.comp30022.team_russia.assist.features.user_detail.services.RealTimeLocationService;
import com.comp30022.team_russia.assist.features.user_detail.services.RealTimeLocationServiceImpl;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import dagger.multibindings.IntoMap;

import javax.inject.Singleton;

/**
 * (Dependency Injection) Top-level Dagger module for the Home screen/contact
 * list-related feature area.
 */
@Module
public abstract class HomeModule {

    // Service
    @Singleton
    @Binds
    public abstract RealTimeLocationService bindRealTimeLocationService(
        RealTimeLocationServiceImpl realTimeLocationService);

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
