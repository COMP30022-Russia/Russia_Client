package com.comp30022.team_russia.assist.features.push;

import com.comp30022.team_russia.assist.features.push.sys.FirebaseBroadcastReceiver;
import com.comp30022.team_russia.assist.features.push.sys.RussiaSocketService;
import com.comp30022.team_russia.assist.features.push.sys.SocketService;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

import javax.inject.Singleton;

/**
 * Dagger module for Push-notification related feature area.
 */
@SuppressWarnings("unused")
@Module
public abstract class PushModule {

    /**
     * Android Broadcast action type for a Firebase data message broadcast.
     * Should be the same as in AndroidManifest.
     * Published by RussiaFirebaseService and received by FirebaseBroadcastReceiver.
     */
    public static final String FIREBASE_BROADCAST_ACTION_DATA
        = "com.comp30022.team_russia.assist.FIREBASE_DATA_MESSAGE_RECEIVED";

    /**
     * Android Broadcast action type for a Firebase token update broadcast.
     * Should be the same as in AndroidManifest.
     * Published by RussiaFirebaseService and received by FirebaseBroadcastReceiver.
     */
    public static final String FIREBASE_BROADCAST_ACTION_TOKEN
        = "com.comp30022.team_russia.assist.FIREBASE_TOKEN_UPDATED";

    @ContributesAndroidInjector
    public abstract FirebaseBroadcastReceiver contributeFirebaseBroadcastReceiver();

    @Singleton
    @Binds
    public abstract SocketService bindSocketService(RussiaSocketService socketService);
}