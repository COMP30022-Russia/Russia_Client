package com.comp30022.team_russia.assist.base.pubsub;

import com.comp30022.team_russia.assist.base.Disposable;

/**
 * Publisher-subscriber hub. Used for decoupled message passing.
 */
public interface PubSubHub {

    <T> void configureTopic(String topic, Class<T> type, PayloadToObjectConverter<T> converter);

    <PayloadT> Disposable subscribe(String topic, SubscriberCallback<PayloadT> callback);

    void publish(String topic, String payload);

    <T> void publish(String topic, T payload);

}