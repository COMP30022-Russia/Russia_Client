package com.comp30022.team_russia.assist.features.location;

import com.comp30022.team_russia.assist.features.location.services.RealTimeLocationService;
import com.comp30022.team_russia.assist.features.location.services.RealTimeLocationServiceImpl;

import dagger.Binds;
import dagger.Module;

import javax.inject.Singleton;

/**
 * Dagger module for the realtime-location sharing feature area.
 */
@Module
public abstract class RealTimeLocationModule {
    // Service
    @Singleton
    @Binds
    public abstract RealTimeLocationService bindRealTimeLocationService(
        RealTimeLocationServiceImpl realTimeLocationService);
}
