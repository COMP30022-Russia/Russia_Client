package com.comp30022.team_russia.assist.features.push.services;

/**
 * Represents a subscriber for {@link PubSubHub}.
 * @param <T> Type of payload this subscriber expects. (No runtime guarantee)
 */
public abstract class SubscriberCallback<T> {
    public abstract void onReceived(T payload);
}