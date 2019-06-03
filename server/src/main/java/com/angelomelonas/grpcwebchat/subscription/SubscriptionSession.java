package com.angelomelonas.grpcwebchat.subscription;

public class SubscriptionSession {
    private SubscriptionData subscriptionData;
    private SubscriptionListener subscriptionListener;

    public SubscriptionSession(SubscriptionData subscriptionData, SubscriptionListener subscriptionListener) {
        this.subscriptionListener = subscriptionListener;
        this.subscriptionData = subscriptionData;
    }

    public SubscriptionData getSubscriptionData() {
        return subscriptionData;
    }

    public void setSubscriptionData(SubscriptionData subscriptionData) {
        this.subscriptionData = subscriptionData;
    }

    public SubscriptionListener getSubscriptionListener() {
        return subscriptionListener;
    }

    public void setSubscriptionListener(SubscriptionListener subscriptionListener) {
        this.subscriptionListener = subscriptionListener;
    }
}
