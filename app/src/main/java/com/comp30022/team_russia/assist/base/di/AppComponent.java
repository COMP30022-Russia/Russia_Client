package com.comp30022.team_russia.assist.base.di;

import android.app.Application;

import com.comp30022.team_russia.assist.RussiaApplication;
import com.comp30022.team_russia.assist.features.assoc.AssociationModule;
import com.comp30022.team_russia.assist.features.call.CallModule;
import com.comp30022.team_russia.assist.features.chat.MessageModule;
import com.comp30022.team_russia.assist.features.home.HomeModule;
import com.comp30022.team_russia.assist.features.location.RealTimeLocationModule;
import com.comp30022.team_russia.assist.features.login.LoginModule;
import com.comp30022.team_russia.assist.features.media.MediaModule;
import com.comp30022.team_russia.assist.features.nav.NavigationModule;
import com.comp30022.team_russia.assist.features.profile.ProfileModule;
import com.comp30022.team_russia.assist.features.push.PushModule;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;

import javax.inject.Singleton;

/**
 * Application top-level Dagger component.
 */
@Singleton
@Component(
    modules = {
        // boilerplate modules
        AndroidInjectionModule.class,
        ActivitiesModule.class,
        ServicesModule.class,
        ViewModelFactoryModule.class,
        // app-level module
        AppModule.class,
        // feature modules; corresponding to the `features.*` subpackages
        LoginModule.class,
        HomeModule.class,
        MessageModule.class,
        AssociationModule.class,
        NavigationModule.class,
        CallModule.class,
        ProfileModule.class,
        PushModule.class,
        ProfileModule.class,
        MediaModule.class,
        RealTimeLocationModule.class
    }
)

public interface AppComponent {
    //CHECKSTYLE.OFF: JavadocTypeCheck
    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);

        AppComponent build();
    }

    void inject(RussiaApplication app);
}
