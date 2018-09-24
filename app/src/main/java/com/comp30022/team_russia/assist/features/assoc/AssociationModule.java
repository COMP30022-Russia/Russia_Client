package com.comp30022.team_russia.assist.features.assoc;

import android.arch.lifecycle.ViewModel;

import com.comp30022.team_russia.assist.base.di.ViewModelKey;
import com.comp30022.team_russia.assist.features.assoc.services.UserService;
import com.comp30022.team_russia.assist.features.assoc.services.UserServiceImpl;
import com.comp30022.team_russia.assist.features.assoc.ui.AssociationFragment;
import com.comp30022.team_russia.assist.features.assoc.ui.GenerateQrFragment;
import com.comp30022.team_russia.assist.features.assoc.ui.ScanQrFragment;
import com.comp30022.team_russia.assist.features.assoc.vm.GenerateQrViewModel;
import com.comp30022.team_russia.assist.features.assoc.vm.ScanQrViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import dagger.multibindings.IntoMap;

import javax.inject.Singleton;

/**
 * Dagger module for the 'association' feature area.
 */
@Module
public abstract class AssociationModule {

    // Services

    @Singleton
    @Binds
    public abstract UserService bindUserService(UserServiceImpl userService);

    // ViewModels

    @Binds
    @IntoMap
    @ViewModelKey(GenerateQrViewModel.class)
    public abstract ViewModel bindGenerateQrViewModel(GenerateQrViewModel generateQrViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ScanQrViewModel.class)
    public abstract ViewModel bindScanQrViewModel(ScanQrViewModel scanQrViewModel);

    // Fragments

    @ContributesAndroidInjector
    public abstract AssociationFragment contributeAssociationFragment();

    @ContributesAndroidInjector
    public abstract ScanQrFragment contributeScanQrFragment();

    @ContributesAndroidInjector
    public abstract GenerateQrFragment contributeGenerateQrFragment();
}
