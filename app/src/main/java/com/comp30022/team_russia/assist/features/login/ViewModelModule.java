package com.comp30022.team_russia.assist.features.login;

import android.arch.lifecycle.ViewModel;

import com.comp30022.team_russia.assist.base.di.ViewModelKey;
import com.comp30022.team_russia.assist.features.login.ui.LoginViewModel;
import com.comp30022.team_russia.assist.features.login.ui.RegisterChooseTypeViewModel;
import com.comp30022.team_russia.assist.features.login.ui.RegisterFormViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

/**
 * Dagger module for all the login/registration related ViewModels.
 */
@Module
public abstract class ViewModelModule {
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

}
