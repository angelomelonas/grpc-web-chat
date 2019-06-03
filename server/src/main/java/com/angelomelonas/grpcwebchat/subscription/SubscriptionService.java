package com.angelomelonas.grpcwebchat.subscription;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SubscriptionService {

    private final Map<UUID, SubscriptionSession> subscriptions = new HashMap<>();

    public SubscriptionService() {

    }

    public void subscribe(SubscriptionData subscriptionData, SubscriptionListener subscriptionListener) {
        subscriptions.put(subscriptionData.getSubscriptionId(), new SubscriptionSession(subscriptionData, subscriptionListener));
    }

    public void unsubscribe(UUID subscriptionId) {
        final SubscriptionSession subscription = subscriptions.get(subscriptionId);

        if (subscription == null) {
            throw new IllegalArgumentException("Cannot unsubscribe. Subscription does not exist.");
        }

        subscription.getSubscriptionListener().onUnsubscribe();
    }

    public void updateSubscriptionData(UUID subscriptionId, SubscriptionData subscriptionData) {
        final SubscriptionSession subscription = subscriptions.get(subscriptionId);

        if (subscription == null) {
            throw new IllegalArgumentException("Cannot update subscription. Subscription does not exist.");
        }

        subscription.setSubscriptionData(subscriptionData);
    }
}
