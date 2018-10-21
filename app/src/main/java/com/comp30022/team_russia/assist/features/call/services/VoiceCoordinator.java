package com.comp30022.team_russia.assist.features.call.services;

import android.arch.lifecycle.LiveData;

/**
 * This component's responsibility is to reconcile the states of different components for
 * voice call.
 * There are 3 areas:
 *   - The state of the Jitsi Meet SDK
 *   - The desired voice chat state of the session (set by server; subject to other users' action)
 *   - UI state (voice button)
 */
public interface VoiceCoordinator {

    void initialise();

    void destroy();

    void acceptIncomingCall();

    void declineIncomingCall();

    void stopOngoingCall();

    void startCallForSession(int sessionId);


    LiveData<Boolean> getIsRemoteCameraOn();

    void setRemoteCameraOn(boolean isOn);

    LiveData<NavCallDesiredState> getState();
}
