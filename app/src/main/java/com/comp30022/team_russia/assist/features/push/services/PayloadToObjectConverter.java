package com.comp30022.team_russia.assist.features.push.services;

/**
 * Converter for serialisable payload for {@link PubSubHub}. To prevent memory leak, all payload
 * published to {@link PubSubHub} should be serialised. This class provides a way to convert
 * arbitrary classes to and from Strings.
 * @param <T> Type of the payload.
 */
public abstract class PayloadToObjectConverter<T> {
    public abstract T fromString(String payloadStr);

    public abstract String toString(T payload);
}
