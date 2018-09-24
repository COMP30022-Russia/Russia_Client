package com.comp30022.team_russia.assist.features.login;

import android.arch.lifecycle.ViewModel;

import com.comp30022.team_russia.assist.base.di.ViewModelKey;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.login.services.AuthServiceImpl;
import com.comp30022.team_russia.assist.features.login.ui.LoginFragment;
import com.comp30022.team_russia.assist.features.login.ui.RegisterChooseTypeFragment;
import com.comp30022.team_russia.assist.features.login.ui.RegisterFormFragment;
import com.comp30022.team_russia.assist.features.login.vm.LoginViewModel;
import com.comp30022.team_russia.assist.features.login.vm.RegisterChooseTypeViewModel;
import com.comp30022.team_russia.assist.features.login.vm.RegisterFormViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import dagger.multibindings.IntoMap;

import javax.inject.Singleton;

/**
 * (Dependency Injection) Top-level Dagger module for the Login-related feature
 * area.
 */
@Module()
public abstract class LoginModule {

    @Singleton
    @Binds
    public abstract AuthService bindAuthService(AuthServiceImpl authService);

    // ViewModels

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel.class)
    abstract ViewModel bindLoginViewModel(LoginViewModel loginViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(RegisterChooseTypeViewModel.class)
    abstract ViewModel bindRegisterChooseTypeViewModel(
        RegisterChooseTypeViewModel registerChooseTypeViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(RegisterFormViewModel.class)
    abstract ViewModel bindRegisterFormViewModel(RegisterFormViewModel registerFormViewModel);

    // Fragments

    @ContributesAndroidInjector
    public abstract LoginFragment contributeLoginFragment();

    @ContributesAndroidInjector
    public abstract RegisterChooseTypeFragment contributeRegisterChooseTypeFragment();

    @ContributesAndroidInjector
    public abstract RegisterFormFragment contributeRegisterFormFragment();

}
