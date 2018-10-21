package com.comp30022.team_russia.assist.features.nav;

import android.arch.lifecycle.ViewModel;

import com.comp30022.team_russia.assist.base.di.ViewModelKey;
import com.comp30022.team_russia.assist.base.pubsub.PayloadToObjectConverter;
import com.comp30022.team_russia.assist.base.pubsub.PubSubHub;
import com.comp30022.team_russia.assist.base.pubsub.PubSubTopics;

import com.comp30022.team_russia.assist.features.nav.services.NavigationService;
import com.comp30022.team_russia.assist.features.nav.services.NavigationServiceImpl;
import com.comp30022.team_russia.assist.features.nav.ui.NavigationFragment;
import com.comp30022.team_russia.assist.features.nav.vm.NavigationViewModel;
import com.comp30022.team_russia.assist.features.push.models.NewGenericPushNotification;
import com.comp30022.team_russia.assist.features.push.models.NewNavControlPushNotification;
import com.comp30022.team_russia.assist.features.push.models.NewNavStartPushNotification;
import com.comp30022.team_russia.assist.features.push.models.NewPositionPushNotification;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import dagger.multibindings.IntoMap;

import javax.inject.Singleton;

/**
 * Dagger module for navigation-related feature area.
 */
@SuppressWarnings("unused")
@Module
public abstract class NavigationModule {

    //Service
    @Singleton
    @Binds
    public abstract NavigationService
        bindNavigationService(NavigationServiceImpl navigationService);

    // ViewModel
    @Binds
    @IntoMap
    @ViewModelKey(NavigationViewModel.class)
    abstract ViewModel bindNavigationViewModel(NavigationViewModel navigationViewModel);

    // Fragment
    @ContributesAndroidInjector
    public abstract NavigationFragment contributeNavigationFragment();

    /**
     * Configures PubSubTopics related to Navigation sessions.
     * @param pubSubHub The {@link PubSubHub} instance.
     */
    public static void configureGlobalTopics(PubSubHub pubSubHub) {
        pubSubHub.configureTopic(PubSubTopics.NAV_START,
            NewNavStartPushNotification.class,
            PayloadToObjectConverter.createGsonForType(NewNavStartPushNotification.class)
        );

        pubSubHub.configureTopic(PubSubTopics.NAV_END,
            NewGenericPushNotification.class,
            PayloadToObjectConverter.createGsonForType(NewGenericPushNotification.class)
        );

        // new AP location (during nav session)
        pubSubHub.configureTopic(PubSubTopics.NEW_AP_LOCATION,
            NewPositionPushNotification.class,
            PayloadToObjectConverter.createGsonForType(NewPositionPushNotification.class));

        // new route
        pubSubHub.configureTopic(PubSubTopics.NEW_ROUTE, NewGenericPushNotification.class,
            PayloadToObjectConverter.createGsonForType(NewGenericPushNotification.class));

        // Change of navigation control
        pubSubHub.configureTopic(PubSubTopics.NAV_CONTROL_SWTICH,
            NewNavControlPushNotification.class,
            PayloadToObjectConverter.createGsonForType(NewNavControlPushNotification.class));

        // AP off track
        pubSubHub.configureTopic(PubSubTopics.NAV_OFF_TRACK,
            NewGenericPushNotification.class,
            PayloadToObjectConverter.createGsonForType(NewGenericPushNotification.class));
    }
}
