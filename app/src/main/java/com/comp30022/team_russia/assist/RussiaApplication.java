package com.comp30022.team_russia.assist;

import static com.comp30022.team_russia.assist.features.push.EmergencyDeduplicator.ensureEmergencyNotDuplicated;
import static com.comp30022.team_russia.assist.features.push.NavSyncTokenDeduplicator.ensureNavSyncTokenValid;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.multidex.MultiDexApplication;
import android.support.v4.content.LocalBroadcastManager;

import com.comp30022.team_russia.assist.base.DisposableCollection;
import com.comp30022.team_russia.assist.base.db.RussiaDatabase;
import com.comp30022.team_russia.assist.base.di.AppInjector;
import com.comp30022.team_russia.assist.base.persist.KeyValueStore;
import com.comp30022.team_russia.assist.features.emergency.ui.EmergencyNotificationActivity;
import com.comp30022.team_russia.assist.features.jitsi.JitsiModule;
import com.comp30022.team_russia.assist.features.jitsi.JitsiStartArgs;
import com.comp30022.team_russia.assist.features.jitsi.services.JitsiMeetHolder;
import com.comp30022.team_russia.assist.features.jitsi.services.VoiceCoordinator;
import com.comp30022.team_russia.assist.features.jitsi.sys.JitsiPlaceholderService;
import com.comp30022.team_russia.assist.features.login.models.User;
import com.comp30022.team_russia.assist.features.login.services.AuthService;
import com.comp30022.team_russia.assist.features.nav.NavigationModule;
import com.comp30022.team_russia.assist.features.nav.models.NavMapScreenStartArgs;
import com.comp30022.team_russia.assist.features.push.PubSubTopics;
import com.comp30022.team_russia.assist.features.push.PushModule;
import com.comp30022.team_russia.assist.features.push.models.FirebaseTokenData;
import com.comp30022.team_russia.assist.features.push.models.NewEmergencyStartPushNotification;
import com.comp30022.team_russia.assist.features.push.models.NewMessagePushNotification;
import com.comp30022.team_russia.assist.features.push.models.NewNavStartPushNotification;
import com.comp30022.team_russia.assist.features.push.models.NewPicturePushNotification;
import com.comp30022.team_russia.assist.features.push.services.PayloadToObjectConverter;
import com.comp30022.team_russia.assist.features.push.services.PubSubHub;
import com.comp30022.team_russia.assist.features.push.services.SubscriberCallback;
import com.comp30022.team_russia.assist.features.push.sys.FirebaseBroadcastReceiver;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasBroadcastReceiverInjector;
import dagger.android.HasServiceInjector;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

/**
 * The application context.
 */
public class RussiaApplication extends MultiDexApplication
    implements HasActivityInjector, HasServiceInjector, HasBroadcastReceiverInjector {

    public static RussiaApplication instance;

    @Inject
    JitsiMeetHolder jitsiMeetHolder;

    @Inject
    VoiceCoordinator voiceCoordinator;

    @Inject
    DispatchingAndroidInjector<Activity> dispatchingAndroidActivityInjector;

    @Inject
    DispatchingAndroidInjector<Service> dispatchingAndroidServiceInjector;

    @Inject
    DispatchingAndroidInjector<BroadcastReceiver> dispatchingAndroidBroadcastReceiverInjector;

    @Inject
    KeyValueStore keyValueStore;

    @Inject
    AuthService authService;

    @Inject
    PubSubHub pubSubHub;

    private final DisposableCollection subscriptions = new DisposableCollection();

    @Inject
    RussiaDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        // Initialise configuration manager
        try {
            ConfigurationManager.createInstance(
                this.getApplicationContext().getAssets().open(
                    ConfigurationManager.CONFIG_FILENAME));
        } catch (IOException e) {
            e.printStackTrace();
        }
        AppInjector.init(this);
        keyValueStore.initialise(this);

        configurePubSubTopics();
        registerFirebaseBroadcastReceiver();

        jitsiMeetHolder.loadConfig();
        voiceCoordinator.initialise();

        subscriptions.add(pubSubHub.subscribe(PubSubTopics.JITSI_PLEASE_START,
            new SubscriberCallback<JitsiStartArgs>() {
                @Override
                public void onReceived(JitsiStartArgs payload) {
                    jitsiMeetHolder.requestCallStart(payload);

                    Intent firebaseServiceIntent = new Intent(RussiaApplication.this,
                        JitsiPlaceholderService.class);
                    if (android.os.Build.VERSION.SDK_INT >= 26) {
                        startForegroundService(firebaseServiceIntent);
                    } else {
                        startService(firebaseServiceIntent);
                    }
                }
            }));

        subscriptions.add(pubSubHub.subscribe(PubSubTopics.JITSI_PLEASE_STOP,
            new SubscriberCallback<Void>() {
                @Override
                public void onReceived(Void payload) {
                    jitsiMeetHolder.requestCallStop();
                }
            }));

        subscriptions.add(pubSubHub.subscribe(PubSubTopics.LOGGED_OUT,
            new SubscriberCallback<Void>() {
                @Override
                public void onReceived(Void payload) {
                    new ClearDatabaseAsyncTask(database).execute();
                    AsyncTask.execute(() -> clearFiles());
                }
            }));


        subscriptions.add(pubSubHub.subscribe(PubSubTopics.EMERGENCY_START,
            new SubscriberCallback<NewEmergencyStartPushNotification>() {
                @Override
                public void onReceived(NewEmergencyStartPushNotification payload) {
                    ensureEmergencyNotDuplicated(payload.getEventId(), () -> {
                        // launch the emergency notification activity
                        Intent intent = new Intent(RussiaApplication.this,
                            EmergencyNotificationActivity.class);

                        intent.putExtra("mobileNumber", payload.getMobileNumber());
                        intent.putExtra("name", payload.getSenderName());
                        intent.putExtra("eventId", payload.getEventId());
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    });
                }

            }));

        subscriptions.add(pubSubHub.subscribe(PubSubTopics.NAV_START,
            new SubscriberCallback<NewNavStartPushNotification>() {
                @Override
                public void onReceived(NewNavStartPushNotification payload) {
                    ensureNavSyncTokenValid(payload.getSessionId(), payload.getSync(), () -> {
                        // start nav session
                        Bundle bundle = new Bundle();
                        bundle.putInt("assocId", payload.getAssociationId());
                        bundle.putInt("sessionId", payload.getSessionId());
                        Boolean isAp =
                            authService.getCurrentUser().getUserType() != User.UserType.AP;
                        bundle.putBoolean("apInitiated", isAp);
                        bundle.putString("senderName", payload.getSenderName());

                        Intent intent = new Intent(RussiaApplication.this,
                            NavigationRequestActivity.class);
                        intent.putExtras(bundle);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getApplicationContext().startActivity(intent);

                    });
                }
            })
        );
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchingAndroidActivityInjector;
    }

    @Override
    public AndroidInjector<Service> serviceInjector() {
        return dispatchingAndroidServiceInjector;
    }

    @Override
    public AndroidInjector<BroadcastReceiver> broadcastReceiverInjector() {
        return dispatchingAndroidBroadcastReceiverInjector;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        jitsiMeetHolder.onApplicationTerminate();
        voiceCoordinator.destroy();
    }

    /**
     * PubSub topics that are used throughout the app, as opposed to only in certain
     * fragments.
     */
    private void configurePubSubTopics() {
        pubSubHub.configureTopic(PubSubTopics.NEW_MESSAGE, NewMessagePushNotification.class,
            PayloadToObjectConverter.createGsonForType(NewMessagePushNotification.class));

        pubSubHub.configureTopic(PubSubTopics.NEW_PICTURE, NewPicturePushNotification.class,
            PayloadToObjectConverter.createGsonForType(NewPicturePushNotification.class));

        pubSubHub.configureTopic(PubSubTopics.FIREBASE_TOKEN, FirebaseTokenData.class,
            PayloadToObjectConverter.createGsonForType(FirebaseTokenData.class));

        pubSubHub.configureTopic(PubSubTopics.LOGGED_IN, Void.class,
            PayloadToObjectConverter.createForVoidPayload());

        pubSubHub.configureTopic(PubSubTopics.NEW_ASSOCIATION, Void.class,
            PayloadToObjectConverter.createForVoidPayload());

        JitsiModule.configureGlobalTopics(pubSubHub);
        NavigationModule.configureGlobalTopics(pubSubHub);

        pubSubHub.configureTopic(PubSubTopics.LOGGED_OUT, Void.class,
            PayloadToObjectConverter.createForVoidPayload());

        pubSubHub.configureTopic(PubSubTopics.EMERGENCY_START,
            NewEmergencyStartPushNotification.class,
            PayloadToObjectConverter.createGsonForType(NewEmergencyStartPushNotification.class));

        pubSubHub.configureTopic(PubSubTopics.NAV_ACCEPTED, NavMapScreenStartArgs.class,
            PayloadToObjectConverter.createGsonForType(NavMapScreenStartArgs.class));
    }

    private void registerFirebaseBroadcastReceiver() {
        BroadcastReceiver br = new FirebaseBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(PushModule.FIREBASE_BROADCAST_ACTION_DATA);
        filter.addAction(PushModule.FIREBASE_BROADCAST_ACTION_TOKEN);
        LocalBroadcastManager.getInstance(this).registerReceiver(br, filter);
    }

    /**
     * Clears the application's internal storage files.
     * Mostly profile pictures and chat images.
     */
    private void clearFiles() {
        File[] files = this.getFilesDir().listFiles();
        if (files != null) {
            for (File file : files) {
                try {
                    file.delete();
                } catch (Exception  e) {
                    // do nothing
                }
            }
        }
    }

}

class ClearDatabaseAsyncTask extends AsyncTask<Void, Void, Void> {
    private final RussiaDatabase database;

    ClearDatabaseAsyncTask(RussiaDatabase database) {
        this.database = database;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        database.clearAllTables();
        return null;
    }
}
