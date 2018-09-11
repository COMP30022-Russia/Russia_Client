package com.comp30022.team_russia.assist.base.di;

import android.app.Application;

import com.comp30022.team_russia.assist.RussiaApplication;
import com.comp30022.team_russia.assist.features.login.LoginModule;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;

/**
 * Application top-level Dagger component.
 */
@Singleton
@Component(
    modules = {
        AndroidInjectionModule.class,
        LoginModule.class,
        MainActivityModule.class,
        ViewModelFactoryModule.class
    }
)
public interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);
        AppComponent build();
    }

    void inject(RussiaApplication app);
}
