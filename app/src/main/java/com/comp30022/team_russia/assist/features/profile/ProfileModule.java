package com.comp30022.team_russia.assist.features.profile;

import android.app.Application;
import android.arch.lifecycle.ViewModel;

import com.comp30022.team_russia.assist.base.LoggerFactory;
import com.comp30022.team_russia.assist.base.di.ViewModelKey;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.profile.services.ProfileDetailsService;
import com.comp30022.team_russia.assist.features.profile.services.ProfileDetailsServiceImpl;
import com.comp30022.team_russia.assist.features.profile.services.ProfileImageManager;
import com.comp30022.team_russia.assist.features.profile.ui.EditProfileFragment;
import com.comp30022.team_russia.assist.features.profile.ui.ProfileFragment;
import com.comp30022.team_russia.assist.features.profile.ui.UserDetailFragment;
import com.comp30022.team_russia.assist.features.profile.vm.EditProfileViewModel;
import com.comp30022.team_russia.assist.features.profile.vm.ProfileViewModel;
import com.comp30022.team_russia.assist.features.profile.vm.UserDetailViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;
import dagger.multibindings.IntoMap;

import javax.inject.Singleton;

import retrofit2.Retrofit;

/**
 * (Dependency Injection) Top-level Dagger module for the Profile-related
 * feature area.
 */
@SuppressWarnings("unused")
@Module()
public abstract class ProfileModule {

    // Services
    @Singleton
    @Binds
    public abstract ProfileDetailsService bindProfileService(
        ProfileDetailsServiceImpl profileService);

    @Singleton
    @Provides
    public static ProfileImageManager
        bindProfileImageManager(AuthService authService, Retrofit retrofit,
                                Application appContext, LoggerFactory loggerFactory) {
        return new ProfileImageManager(authService, appContext, retrofit, loggerFactory);
    }

    // ViewModels
    @Binds
    @IntoMap
    @ViewModelKey(ProfileViewModel.class)
    abstract ViewModel bindProfileViewModel(ProfileViewModel profileViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(EditProfileViewModel.class)
    abstract ViewModel bindEditProfileViewModel(EditProfileViewModel editProfileViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(UserDetailViewModel.class)
    abstract ViewModel bindUserDetailViewModel(UserDetailViewModel userDetailViewModel);

    // Fragments
    @ContributesAndroidInjector
    public abstract ProfileFragment contributeProfileFragment();

    @ContributesAndroidInjector
    public abstract EditProfileFragment contributeEditProfileFragment();

    @ContributesAndroidInjector
    public abstract UserDetailFragment contributeUserDetailFragment();
}
