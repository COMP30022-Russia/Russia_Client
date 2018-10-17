package com.comp30022.team_russia.assist.features.media;

import com.comp30022.team_russia.assist.features.media.services.MediaManager;
import com.comp30022.team_russia.assist.features.media.services.MediaManagerImpl;

import dagger.Binds;
import dagger.Module;

import javax.inject.Singleton;

/**
 * Dagger module for the Media management feature area.
 * Media management refers to the management of media files (e.g. pictures).
 */
@Module
public abstract class MediaModule {

    @Binds
    @Singleton
    public abstract MediaManager bindMediaManager(MediaManagerImpl mediaManager);
}
