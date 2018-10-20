package com.comp30022.team_russia.assist.features.jitsi.services;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Intent;
import android.view.View;

import com.comp30022.team_russia.assist.ConfigurationManager;
import com.comp30022.team_russia.assist.base.LoggerFactory;
import com.comp30022.team_russia.assist.base.LoggerInterface;
import com.comp30022.team_russia.assist.features.jitsi.JitsiStartArgs;
import com.comp30022.team_russia.assist.features.jitsi.JitsiStartType;
import com.comp30022.team_russia.assist.features.push.PubSubTopics;
import com.comp30022.team_russia.assist.features.push.services.PubSubHub;

import java.util.Map;

import javax.inject.Inject;

import org.jitsi.meet.sdk.JitsiMeetView;
import org.jitsi.meet.sdk.JitsiMeetViewListener;


/**
 * Implementation of {@link JitsiMeetHolder}.
 */
public class JitsiMeetHolderImpl implements JitsiMeetHolder {

    private static final String CONFIG_JITSI_MEET_URL = "JITSI_MEET_URL";
    private static final String CONFIG_JITSI_MEET_CONFERENCE_PREFIX
        = "JITSI_MEET_CONFERENCE_PREFIX";
    private static final String CONFIG_JITSI_MEET_FAKE_CALL = "JITSI_MEET_FAKE_CALL";


    private JitsiMeetView view = null;

    private final MutableLiveData<View> viewLd = new MutableLiveData<>();

    private Activity lastActivity;
    private boolean lastActivityDead = false;

    private String jitsiMeetServer;
    private String jitstMeetConferencePrefix;

    /**
     * If this is set to true, do not actually join a Jitsi Conference.
     * For debug.
     */
    private boolean fakeCall = false;

    private boolean configLoaded = false;

    private final LoggerInterface logger;

    private final PubSubHub pubSubHub;

    private String activeRoomId = null;

    private boolean hasPendingStartRequest = false;
    private boolean hasPendingStopRequest = false;
    private JitsiStartArgs pendingStartArgs = null;

    @Inject
    public JitsiMeetHolderImpl(LoggerFactory loggerFactory, PubSubHub pubSubHub
                               ) {
        logger = loggerFactory.create(this.getClass().getSimpleName());
        this.pubSubHub = pubSubHub;
    }

    @Override
    public void initialise(Activity activityContext) {
        if (view == null) {
            activityContext.runOnUiThread(() -> {
                view = new JitsiMeetView(activityContext);
                viewLd.postValue(view);
                view.setListener(new JitsiMeetViewListener() {
                    @Override
                    public void onConferenceFailed(Map<String, Object> map) {
                        pubSubHub.publish(PubSubTopics.JITSI_FAILED, null);
                    }

                    @Override
                    public void onConferenceJoined(Map<String, Object> map) {
                        pubSubHub.publish(PubSubTopics.JITSI_JOINED, null);
                    }

                    @Override
                    public void onConferenceLeft(Map<String, Object> map) {
                        pubSubHub.publish(PubSubTopics.JITSI_LEFT, null);
                    }

                    @Override
                    public void onConferenceWillJoin(Map<String, Object> map) {

                    }

                    @Override
                    public void onConferenceWillLeave(Map<String, Object> map) {

                    }

                    @Override
                    public void onLoadConfigError(Map<String, Object> map) {
                        pubSubHub.publish(PubSubTopics.JITSI_FAILED, null);
                    }
                });
                view.loadURLString("");
            });
        }
    }

    @Override
    public synchronized void requestCallStart(JitsiStartArgs args) {
        if (args == null) {
            return;
        }
        if (lastActivityDead || lastActivity == null) {
            logger.warn("Jitsi cannot start now because activity is not in foreground. "
                        + "However the request has been noted.");
            hasPendingStartRequest = true;
            pendingStartArgs = args;
            hasPendingStopRequest = false;
            return;
        }
        doCallStart(args);

    }

    private void doCallStart(JitsiStartArgs args) {
        hasPendingStartRequest = false;
        if (view == null) {
            logger.warn("Jitsi Meet not initialised");
            initialise(lastActivity);
        }
        pubSubHub.publish(PubSubTopics.JITSI_IDLE, null);
        assertConfigLoaded();
        String config;
        if (args.getType() == JitsiStartType.Voice) {
            config = "config.startWithVideoMuted=true";
        } else {
            config = "config.constraints.video.facingMode=\"environment\"";
        }
        String jitsiConferenceUrl =
            String.format("%s%s%s#%s",
                jitsiMeetServer, jitstMeetConferencePrefix, args.getRoom(), config);
        logger.info("Joining Jitsi Conference at " + jitsiConferenceUrl);

        if (activeRoomId != null && activeRoomId.equals(args.getRoom())) {
            logger.info("The requested Jitsi Conference is already on.");
            return;
        }
        activeRoomId = args.getRoom();
        if (fakeCall) {
            logger.warn("Debug mode. Not actually joining Jitsi Conference.");
        } else {
            lastActivity.runOnUiThread(() -> view.loadURLString(jitsiConferenceUrl));
        }
    }

    @Override
    public void requestCallStop() {
        if (lastActivityDead || lastActivity == null) {
            logger.warn("Jitsi cannot stop now because activity is not in foreground. "
                        + "However the request has been noted.");
            hasPendingStartRequest = false;
            hasPendingStopRequest = true;
            return;
        }
        doCallStop();
    }

    private void doCallStop() {
        hasPendingStartRequest = false;
        if (view == null) {
            logger.warn("requestCallStop: Jitsi Meet not initialised.");
            return;
        }

        // Loading an empty URL stops the Jitsi conference.
        logger.info("Stopping Jitsi call...");
        lastActivity.runOnUiThread(() -> {
            if (view != null) {
                viewLd.postValue(null);
                view.dispose();
                view = null;
            }
        });
        activeRoomId = null;
        pubSubHub.publish(PubSubTopics.JITSI_STOPPED, null);
    }

    @Override
    public void destroy() {
        if (view != null) {
            logger.info("Releasing Jitsi Meet View.");
            viewLd.postValue(null);
            view.dispose();
            activeRoomId = null;
            view = null;
            if (lastActivityDead && lastActivity != null) {
                logger.info("Releasing dead Activity since Jitsi has stopped.");
                lastActivity = null;
            }
        }
    }

    @Override
    public void onActivityResume(Activity activityContext) {
        if (activityContext != null) {
            lastActivity = activityContext;
            JitsiMeetView.onHostResume(activityContext);
            lastActivityDead = false;
            if (hasPendingStartRequest) {
                doCallStart(pendingStartArgs);
            } else if (hasPendingStopRequest) {
                doCallStop();
            }
        }
    }

    @Override
    public void onActivityStop(Activity activityContext) {
        if (activityContext != null) {
            lastActivity = activityContext;
            if (view != null) {
                // Jitsi is ongoing. We don't release lastActivity.
                // This is a hacky approach to keep Jitsi Meet (i.e. the ReactInstance) running
                // while the app in the background.
                // It prevents the GC from collecting the old Activity object, until a new Activity
                // has been created.
                // May cause memory leaks. But should not be too bad for now.
                lastActivityDead = true;
                logger.info("Suppressing Activity Stop event to keep Jitsi running...");
            } else {
                try {
                    JitsiMeetView.onHostPause(lastActivity);
                } catch (Exception e) {
                    // do nothing
                }
                lastActivity = null;
            }
        }
    }

    @Override
    public void onApplicationTerminate() {
        if (lastActivity != null) {
            destroy();

            JitsiMeetView.onHostDestroy(lastActivity);
            lastActivity = null;
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        JitsiMeetView.onNewIntent(intent);
    }

    @Override
    public void onUserLeaveHint() {
        if (view != null) {
            logger.info("User leave with ongoing Jitsi call. Triggering Picture-in-Picture...");
            //view.onUserLeaveHint();
        } else {
            logger.info("User leave. But we don't have an ongoing Jitsi call. Ignoring...");
        }
    }

    @Override
    public LiveData<View> getJitsiMeetView() {
        return this.viewLd;
    }


    @Override
    public void loadConfig() {
        if (!configLoaded) {
            logger.info("Loading configuration...");
            ConfigurationManager configMan = ConfigurationManager.getInstance();
            jitsiMeetServer = configMan.getProperty(CONFIG_JITSI_MEET_URL);
            jitstMeetConferencePrefix = configMan.getProperty(CONFIG_JITSI_MEET_CONFERENCE_PREFIX);
            fakeCall = configMan.getBooleanProperty(CONFIG_JITSI_MEET_FAKE_CALL);
            configLoaded = true;
        } else {
            logger.warn("Configuration is already loaded.");
        }
    }

    private void assertConfigLoaded() {
        if (!configLoaded) {
            throw new IllegalStateException("Configuration not loaded");
        }
    }
}

