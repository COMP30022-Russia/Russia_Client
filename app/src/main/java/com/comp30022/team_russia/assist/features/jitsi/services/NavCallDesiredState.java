package com.comp30022.team_russia.assist.features.jitsi.services;

/**
 * Desired state of Navigation Session (Voice) call. This is the source of truth given
 * by the server.
 */
public enum NavCallDesiredState {
    /**
     * No active call.
     */
    Off,
    StartRequested,
    /**
     * Call is ongoing. (Jitsi Conference should join).
     */
    Ongoing,
    /**
     * The current user is being asked to accept a call.
     */
    RingingMe,
    /**
     * The other user is being asked to accept a call.
     */
    RingingOther
}
