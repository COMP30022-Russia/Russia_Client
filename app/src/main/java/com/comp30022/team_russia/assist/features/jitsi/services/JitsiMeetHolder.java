package com.comp30022.team_russia.assist.features.jitsi.services;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.comp30022.team_russia.assist.features.jitsi.JitsiStartArgs;

/**
 * A holder of {@link org.jitsi.meet.sdk.JitsiMeetView}. We use a single instance of Jitsi
 * throughout the app, and it need to be kept alive while the app is in the background.
 */
public interface JitsiMeetHolder {

    void initialise(Activity activityContext);

    void requestCallStart(JitsiStartArgs args);

    void requestCallStop();

    void destroy();

    void onActivityResume(Activity activityContext);

    void onActivityStop(Activity activityContext);

    void onApplicationTerminate();

    void onNewIntent(Intent intent);

    void onUserLeaveHint();

    View getJitsiMeetView();

    void loadConfig();
}
