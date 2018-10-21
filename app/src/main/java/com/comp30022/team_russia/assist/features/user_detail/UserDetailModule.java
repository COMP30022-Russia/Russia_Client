package com.comp30022.team_russia.assist.features.user_detail;

import android.arch.lifecycle.ViewModel;

import com.comp30022.team_russia.assist.base.di.ViewModelKey;
import com.comp30022.team_russia.assist.features.user_detail.ui.UserDetailFragment;
import com.comp30022.team_russia.assist.features.user_detail.vm.UserDetailViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import dagger.multibindings.IntoMap;

/**
 * User Detail Module.
 */
@SuppressWarnings("unused")
@Module
public abstract class UserDetailModule {
    // ViewModel
    @Binds
    @IntoMap
    @ViewModelKey(UserDetailViewModel.class)
    abstract ViewModel bindUserDetailViewModel(UserDetailViewModel userDetailViewModel);

    // Fragment
    @ContributesAndroidInjector
    public abstract UserDetailFragment contributeUserDetailFragment();
}
