package com.comp30022.team_russia.assist.features.profile;

import android.arch.lifecycle.ViewModel;

import com.comp30022.team_russia.assist.base.di.ViewModelKey;
import com.comp30022.team_russia.assist.features.profile.services.ProfileDetailsService;
import com.comp30022.team_russia.assist.features.profile.services.ProfileDetailsServiceImpl;
import com.comp30022.team_russia.assist.features.profile.ui.EditProfileFragment;
import com.comp30022.team_russia.assist.features.profile.ui.ProfileFragment;
import com.comp30022.team_russia.assist.features.profile.vm.EditProfileViewModel;
import com.comp30022.team_russia.assist.features.profile.vm.ProfileViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import dagger.multibindings.IntoMap;

import javax.inject.Singleton;

/**
 * (Dependency Injection) Top-level Dagger module for the Profile-related
 * feature area.
 */
@Module()
public abstract class ProfileModule {

    // Services
    @Singleton
    @Binds
    public abstract ProfileDetailsService bindProfileService(
        ProfileDetailsServiceImpl profileService);

    // ViewModels
    @Binds
    @IntoMap
    @ViewModelKey(ProfileViewModel.class)
    abstract ViewModel bindProfileViewModel(ProfileViewModel profileViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(EditProfileViewModel.class)
    abstract ViewModel bindEditProfileViewModel(EditProfileViewModel editProfileViewModel);

    // Fragments
    @ContributesAndroidInjector
    public abstract ProfileFragment contributeProfileFragment();

    @ContributesAndroidInjector
    public abstract EditProfileFragment contributeEditProfileFragment();
}
