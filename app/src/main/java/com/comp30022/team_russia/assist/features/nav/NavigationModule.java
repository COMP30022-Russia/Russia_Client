package com.comp30022.team_russia.assist.features.nav;

import android.arch.lifecycle.ViewModel;

import com.comp30022.team_russia.assist.base.di.ViewModelKey;
import com.comp30022.team_russia.assist.features.nav.service.NavigationService;
import com.comp30022.team_russia.assist.features.nav.service.NavigationServiceImpl;
import com.comp30022.team_russia.assist.features.nav.ui.NavigationFragment;
import com.comp30022.team_russia.assist.features.nav.ui.NavigationNotificationFragment;
import com.comp30022.team_russia.assist.features.nav.vm.NavigationNotificationViewModel;
import com.comp30022.team_russia.assist.features.nav.vm.NavigationViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import dagger.multibindings.IntoMap;

import javax.inject.Singleton;

/**
 * Dagger module for navigation-related feature area.
 */
@Module
public abstract class NavigationModule {

    //Service
    @Singleton
    @Binds
    public abstract NavigationService
        bindNavigationService(NavigationServiceImpl navigationService);

    // ViewModels
    @Binds
    @IntoMap
    @ViewModelKey(NavigationViewModel.class)
    abstract ViewModel bindNavigationViewModel(NavigationViewModel navigationViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(NavigationNotificationViewModel.class)
    abstract ViewModel
        bindNavigationRequestViewModel(NavigationNotificationViewModel navigationRequestViewModel);

    // Fragments
    @ContributesAndroidInjector
    public abstract NavigationFragment contributeNavigationFragment();

    @ContributesAndroidInjector
    public abstract NavigationNotificationFragment contributeNavigationRequestFragment();
}
