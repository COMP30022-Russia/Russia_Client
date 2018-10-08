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

    /**
     * New chat message.
     */
    public static final String NEW_MESSAGE = "chat";

    public static final String FIREBASE_TOKEN = "firebaseToken";

    /**
     * Someone associated with the current user.
     */
    public static final String NEW_ASSOCIATION = "association";

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

}
