package com.comp30022.team_russia.assist.features.push.services;

import com.google.gson.Gson;

/**
 * Converter for serialisable payload for {@link PubSubHub}. To prevent memory leak, all payload
 * published to {@link PubSubHub} should be serialised. This class provides a way to convert
 * arbitrary classes to and from Strings.
 * @param <T> Type of the payload.
 */
public abstract class PayloadToObjectConverter<T> {
    public abstract T fromString(String payloadStr);

    public abstract String toString(T payload);

    /**
     * Helper method for creating a {@link PayloadToObjectConverter} for {@link Void} type.
     * @return The {@link PayloadToObjectConverter} created.
     */
    public static PayloadToObjectConverter<Void> createForVoidPayload() {
        return new PayloadToObjectConverter<Void>() {
            @Override
            public Void fromString(String payloadStr) {
                return null;
            }

            @Override
            public String toString(Void payload) {
                return "";
            }
        };
    }

    /**
     * Helper method for creating a {@link PayloadToObjectConverter} for any type
     * using {@link Gson} as the serialiser/deserialiser.
     * @param type The type of the class to serialise.
     * @param <T1> The type of the class to serialise.
     * @return The {@link PayloadToObjectConverter} created.
     */
    public static <T1> PayloadToObjectConverter<T1> createGsonForType(Class type) {

        return new PayloadToObjectConverter<T1>() {

            private final Gson gson = new Gson();
            @Override
            public T1 fromString(String payloadStr) {
                return (T1) gson.fromJson(payloadStr, type);
            }

            @Override
            public String toString(T1 payload) {
                return gson.toJson(payload, type);
            }
        };
    }
}

