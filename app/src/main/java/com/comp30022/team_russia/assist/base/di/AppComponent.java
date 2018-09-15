package com.comp30022.team_russia.assist.base.di;

import android.app.Application;

import com.comp30022.team_russia.assist.RussiaApplication;
import com.comp30022.team_russia.assist.features.assoc.AssociationModule;
import com.comp30022.team_russia.assist.features.home_contacts.HomeModule;
import com.comp30022.team_russia.assist.features.login.LoginModule;
import com.comp30022.team_russia.assist.features.message.MessageModule;
import com.comp30022.team_russia.assist.features.message.models.Association;

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
        // boilerplate modules
        AndroidInjectionModule.class,
        ActivitiesModule.class,
        ViewModelFactoryModule.class,
        // app-level module
        AppModule.class,
        // feature modules; corresponding to features.* subpackages
        LoginModule.class,
        HomeModule.class,
        MessageModule.class,
        AssociationModule.class,
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
