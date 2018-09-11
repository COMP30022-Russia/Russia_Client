package com.comp30022.team_russia.assist.features.login;

import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.login.services.AuthServiceImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * (Dependency Injection) Top-level Dagger module for the Login-related feature
 * area.
 */
@Module(includes = {
    FragmentsModule.class,
    ViewModelModule.class
})
public class LoginModule {

    @Singleton
    @Provides
    public AuthService provideAuthService() {
        return new AuthServiceImpl();
    }
}
