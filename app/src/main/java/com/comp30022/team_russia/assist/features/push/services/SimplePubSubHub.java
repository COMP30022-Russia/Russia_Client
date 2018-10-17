package com.comp30022.team_russia.assist.features.push.services;

import com.comp30022.team_russia.assist.base.Disposable;
import com.comp30022.team_russia.assist.base.LoggerFactory;
import com.comp30022.team_russia.assist.base.LoggerInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import kotlin.Pair;


/**
 * A naive implementation of a Publish-Subscribe hub.
 */
public class SimplePubSubHub implements PubSubHub {

    private final Map<String, List<SubscriberCallback>> subscribers = new HashMap<>();
    private final Map<String, PayloadToObjectConverter> typeConverters = new HashMap<>();
    private final ConcurrentLinkedQueue<Pair<String, String>> pendingItems
        = new ConcurrentLinkedQueue<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final LoggerInterface logger;

    /**
     * Required for Dagger 2 to inject.
     */
    @Inject
    public SimplePubSubHub(LoggerFactory loggerFactory) {
        this.logger = loggerFactory.getLoggerForClass(this.getClass());
    }

    @Override
    public <T> void configureTopic(String topic, Class<T> type,
                                   PayloadToObjectConverter<T> converter) {
        if (converter == null) {
            throw new NullPointerException();
        }
        synchronized (typeConverters) {
            if (typeConverters.containsKey(topic)) {
                return;
            }

            typeConverters.put(topic, converter);
        }
    }

    @Override
    public <PayloadT> Disposable subscribe(String topic, SubscriberCallback<PayloadT> callback) {
        assert (callback != null);
        synchronized (subscribers) {
            if (!subscribers.containsKey(topic)) {
                subscribers.put(topic, new ArrayList<>());
            }
            subscribers.get(topic).add(callback);
            logger.info("Subscribed " + callback.toString() + " from topic " + topic);
            return () -> {
                synchronized (subscribers) {
                    subscribers.get(topic).remove(callback);
                    logger.info("Unsubscribed" + callback.toString() + " from topic " + topic);
                }
            };
        }
    }

    @Override
    public void publish(String type, String payload) {
        this.pendingItems.add(new Pair<>(type, payload));
        logger.debug("Publishing to topic " + type + " : " + payload);
        executor.execute(this::processPendingItems);
    }

    @Override
    public <T> void publish(String topic, T payload) {
        PayloadToObjectConverter<T> converter = getTypeConverter(topic);
        if (converter == null) {
            logger.error("No converter for topic " + topic);
        }
        try {
            String serialisedPayload = converter.toString(payload);
            publish(topic, serialisedPayload);
            logger.info(String.format("Published %s: %s", topic, serialisedPayload));
        } catch (Exception e) {
            logger.error("Error during serialisation");
            e.printStackTrace();
        }
    }

    private PayloadToObjectConverter getTypeConverter(String topic) {
        synchronized (typeConverters) {
            PayloadToObjectConverter typeConverter = typeConverters.get(topic);
            if (typeConverter == null) {
                logger.warn(String.format("TypeConverter for topic %s does not exist", topic));
            }
            return typeConverter;
        }
    }

    private void processPendingItems() {
        Pair<String, String> item;
        while ((item = pendingItems.poll()) != null) {
            String type = item.getFirst();
            synchronized (subscribers) {
                if (!this.subscribers.containsKey(type) || this.subscribers.get(type).isEmpty()) {
                    logger.error("No handlers for message type " + type);
                    continue;
                }
            }

            // make a copy of subscribers
            List<SubscriberCallback> topicSubscribers;
            synchronized (subscribers) {
                topicSubscribers = new ArrayList<>(subscribers.get(item.getFirst()));
            }

            // deserialise and send
            String payloadStr = item.getSecond();
            try {
                Object payload = getTypeConverter(item.getFirst()).fromString(payloadStr);
                for (SubscriberCallback subscriber: topicSubscribers) {
                    try {
                        //noinspection unchecked
                        subscriber.onReceived(payload);
                    } catch (Exception e) {
                        logger.error("Error calling handler.");
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                logger.warn("Unknown error deserialising the payload:");
                e.printStackTrace();
            }
        }
    }
}