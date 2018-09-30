package com.comp30022.team_russia.assist.features.push;

import com.comp30022.team_russia.assist.features.push.services.PubSubHub;
import com.comp30022.team_russia.assist.features.push.services.SimplePubSubHub;
import dagger.Binds;
import dagger.Module;

import javax.inject.Singleton;

/**
 * Dagger module for Push-notification related feature area.
 */
@Module
public abstract class PushModule {

    @Singleton
    @Binds
    public abstract PubSubHub bindPubSubHub(SimplePubSubHub pushNotificationHub);
}
