package com.comp30022.team_russia.assist.features.assoc;

import android.arch.lifecycle.ViewModel;

import com.comp30022.team_russia.assist.base.di.ViewModelKey;
import com.comp30022.team_russia.assist.features.assoc.services.UserService;
import com.comp30022.team_russia.assist.features.assoc.services.UserServiceImpl;
import com.comp30022.team_russia.assist.features.assoc.ui.AssociationFragment;
import com.comp30022.team_russia.assist.features.assoc.ui.GenerateQRFragment;
import com.comp30022.team_russia.assist.features.assoc.ui.GenerateQRViewModel;
import com.comp30022.team_russia.assist.features.assoc.ui.ScanQRFragment;
import com.comp30022.team_russia.assist.features.assoc.ui.ScanQRViewModel;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import dagger.multibindings.IntoMap;

@Module
public abstract class AssociationModule {

    // Services

    @Singleton
    @Binds
    public abstract UserService bindUserService(UserServiceImpl userService);

    // ViewModels

    @Binds
    @IntoMap
    @ViewModelKey(GenerateQRViewModel.class)
    public abstract ViewModel bindGenerateQRViewModel(GenerateQRViewModel generateQRViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ScanQRViewModel.class)
    public abstract ViewModel bindScanQRViewModel(ScanQRViewModel scanQRViewModel);

    // Fragments

    @ContributesAndroidInjector
    public abstract AssociationFragment contributeAssociationFragment();

    @ContributesAndroidInjector
    public abstract ScanQRFragment contributeScanQRFragment();

    @ContributesAndroidInjector
    public abstract GenerateQRFragment contributeGenerateQRFragment();
}
