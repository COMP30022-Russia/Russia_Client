package com.comp30022.team_russia.assist.features.push;

/**
 * Defines topic strings for publish-subscribe topics. Topics are used when calling
 * PubSubHub.publish() or PubSubHub.subscribe().
 *
 * <p>It is not strictly required to define topics here, but this centralised place is preferred
 * over scattering magic strings in other classes.
 */
public final class PubSubTopics {

    public static final String LOGGED_IN = "loggedIn";

    public static final String LOGGED_OUT = "loggedOut";

    public static final String FIREBASE_TOKEN = "firebaseToken";

    /**
     * New chat message.
     */
    public static final String NEW_MESSAGE = "chat";

    /**
     * New chat image / picture.
     */
    public static final String NEW_PICTURE = "chat_picture_uploaded";

    /**
     * Someone associated with the current user.
     */
    public static final String NEW_ASSOCIATION = "association";



    /******************************* NAVIGATION MAP ********************************/

    /**
     * A new location of AP has been found.
     */
    public static final String NEW_AP_LOCATION = "nav_location_update";

    /**
     * A route has been generated using the current destination.
     */
    public static final String NEW_ROUTE = "route_update";

    /**
     * Navigation control is switched.
     */
    public static final String NAV_CONTROL_SWTICH = "nav_control_switch";

    /**
     * A navigation session started.
     */
    public static final String NAV_START = "nav_start";

    /**
     * A navigation session ended.
     */
    public static final String NAV_END = "nav_end";

    /**
     * Ap went off track.
     */
    public static final String NAV_OFF_TRACK = "nav_off_track";




    /******************************* NAVIGATION CALL ********************************/

    /**
     * The Firebase data message to be received by the receiver of the call.
     * NOTE: this ("_accept_other") is correct. Looks like Steven interpreted "me" and "other"
     * differently than I do.
     */
    public static final String NAV_CALL_PENDING_ME = "nav_voice_pending_accept_other";
    /**
     * The Firebase data message to be received by the sender of the call.
     */
    public static final String NAV_CALL_PENDING_OTHER = "nav_voice_pending_accept_me";
    public static final String NAV_CALL_STARTED = "nav_call_started";
    public static final String NAV_CALL_TERMINATED = "nav_call_terminated";
    public static final String NAV_CALL_STATE_CHANGED = "nav_call_state";

    public static final String NAV_COORDINATOR_ERROR_MESSAGE = "navCoorError";

    // The following events are sent by the JitsiMeetHolder to notify the status of the
    // underlying Jitsi Meet Conference.
    /**
     * Fired when the local client successfully joins a Jitsi Meet conference, i.e.
     * WebRTC connection is established.
     */

    public static final String JITSI_IDLE = "jitsiIdle";

    public static final String JITSI_JOINED = "jitsiJoined";

    public static final String JITSI_LEFT = "jitsiLeft";

    public static final String JITSI_STOPPED = "jitsiStopped";

    /**
     * Fired when a Jitsi Meet conference failed.
     */
    public static final String JITSI_FAILED = "jitsiFailed";

    // The following events are sent by the VoiceChatCoordinator to request JitsiMeetHolder
    // to do stuff

    public static final String JITSI_PLEASE_START = "jitstPleaseStarted";

    public static final String JITSI_PLEASE_STOP = "jitsiPleaseStop";

}
