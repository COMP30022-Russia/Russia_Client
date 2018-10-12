package com.comp30022.team_russia.assist.features.jitsi.sys;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LifecycleService;
import android.arch.lifecycle.ServiceLifecycleDispatcher;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.Disposable;
import com.comp30022.team_russia.assist.features.jitsi.services.NavCallDesiredState;
import com.comp30022.team_russia.assist.features.jitsi.services.VoiceCoordinator;
import com.comp30022.team_russia.assist.features.push.PubSubTopics;
import com.comp30022.team_russia.assist.features.push.services.PubSubHub;
import com.comp30022.team_russia.assist.features.push.services.SubscriberCallback;

import dagger.android.AndroidInjection;

import java.time.temporal.ValueRange;

import javax.inject.Inject;

/**
 * This Android Service is just a placeholder to display an Notification in the System Tray,
 * and to let the Android system know that we have something running (the Jitsi call),
 * so that it will not kill our RussiaApplication when we are having voice chat in the background.
 */
public class JitsiPlaceholderService extends LifecycleService {

    private static final String TAG = JitsiPlaceholderService.class.getSimpleName();
    private static final String NOTI_TITLE = "Assist Voice call";
    private static final String NOTI_CONTENT_TEXT = "Voice call is ongoing";

    @Inject
    PubSubHub pubSubHub;

    @Inject
    VoiceCoordinator voiceCoordinator;

    private Disposable voiceEndedSubscription;

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
        Log.d(TAG, "created");

        notifyAndStartForeground();

        voiceCoordinator.getState().observe(this, (value) -> {
            if (value != null) {
                Log.d(TAG,"State changed " + value.toString());
                if (value == NavCallDesiredState.Off) {
                    Log.d(TAG,"stopping");
                    JitsiPlaceholderService.this.stopSelf();
                }
            }
        });
        Log.d(TAG,"started");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (voiceEndedSubscription != null) {
            voiceEndedSubscription.dispose();
            voiceEndedSubscription = null;
        }
    }

    @TargetApi(26)
    private void notifyAndStartForeground() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("russiaVoice",
                "Russia Voice Status Channel",
                NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Channel description");
            notificationManager.createNotificationChannel(channel);
            NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, "russiaVoice");
            builder
                .setOngoing(true)
                .setContentTitle(NOTI_TITLE)
                .setSmallIcon(R.drawable.ic_voice_chat)
                .setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(NOTI_CONTENT_TEXT));
            Notification notification = builder.build();
            notificationManager.notify(2, notification);
            startForeground(2, notification);
        }
        // TODO: display notification for API<26.
    }

}
