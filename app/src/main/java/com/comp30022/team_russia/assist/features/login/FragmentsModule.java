package com.comp30022.team_russia.assist.features.login;

import com.comp30022.team_russia.assist.features.login.ui.LoginFragment;
import com.comp30022.team_russia.assist.features.login.ui.RegisterChooseTypeFragment;
import com.comp30022.team_russia.assist.features.login.ui.RegisterFormFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * Dagger module for all the login/registration related fragments.
 */
@Module
public abstract class FragmentsModule {

    @ContributesAndroidInjector
    public abstract LoginFragment contributeLoginFragment();

    @ContributesAndroidInjector
    public abstract RegisterChooseTypeFragment
        contributeRegisterChooseTypeFragment();

    @ContributesAndroidInjector
    public abstract RegisterFormFragment contributeRegisterFormFragment();
}
