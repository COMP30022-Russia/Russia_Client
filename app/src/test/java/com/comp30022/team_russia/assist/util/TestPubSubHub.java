package com.comp30022.team_russia.assist.util;

import com.comp30022.team_russia.assist.base.Disposable;
import com.comp30022.team_russia.assist.base.pubsub.PayloadToObjectConverter;
import com.comp30022.team_russia.assist.base.pubsub.PubSubHub;
import com.comp30022.team_russia.assist.base.pubsub.SubscriberCallback;

public class TestPubSubHub implements PubSubHub {
    @Override
    public <T> void configureTopic(String topic, Class<T> type, PayloadToObjectConverter<T> converter) {
        System.out.println("Configure topic " + topic + " Type = " + type.getCanonicalName());
    }

    @Override
    public <PayloadT> Disposable subscribe(String topic, SubscriberCallback<PayloadT> callback) {
        System.out.println("Subscribing to topic " + topic);
        return () -> System.out.print(this.toString() + " is being disposed");
    }

    @Override
    public void publish(String topic, String payload) {
        System.out.println("Publishing to topic " + topic + " : "+ payload);
    }

    @Override
    public <T> void publish(String topic, T payload) {
        System.out.println("Publishing to topic " + topic + " : "+ payload.toString());
    }
}
