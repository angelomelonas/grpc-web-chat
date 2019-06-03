package com.angelomelonas.grpcwebchat.subscription;

import com.google.protobuf.Any;

public interface SubscriptionListener {
    void onUnsubscribe();

    void onSubscriptionDataUpdate(Any subscriptionData);
}
