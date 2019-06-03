package com.angelomelonas.grpcwebchat.subscription;

import com.google.protobuf.Any;

import java.util.UUID;

public class SubscriptionData {

    private UUID subscriptionId;
    private Any subscriptionData;

    public SubscriptionData(UUID subscriptionId, Any subscriptionData) {
        this.subscriptionId = subscriptionId;
        this.subscriptionData = subscriptionData;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public Any getSubscriptionData() {
        return subscriptionData;
    }

    public void setSubscriptionData(Any subscriptionData) {
        this.subscriptionData = subscriptionData;
    }
}
