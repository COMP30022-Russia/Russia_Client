package com.comp30022.team_russia.assist.features.push;

/**
 * Defines topic strings for publish-subscribe topics. Topics are used when calling
 * PubSubHub.publish() or PubSubHub.subscribe().
 *
 * <p>It is not strictly required to define topics here, but this centralised place is preferred
 * over scattering magic strings in other classes.
 */
public final class PubSubTopics {

    /**
     * New chat message.
     */
    public static final String NEW_MESSAGE = "chat";
    public static final String FIREBASE_TOKEN = "firebaseToken";

    /**
     * Someone associated with the current user.
     */
    public static final String NEW_ASSOCIATION = "association";
}
