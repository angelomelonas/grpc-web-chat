package com.angelomelonas.grpcwebchat.chat;

import com.angelomelonas.grpcwebchat.Chat.Message;
import com.angelomelonas.grpcwebchat.subscription.SubscriptionData;
import com.angelomelonas.grpcwebchat.subscription.SubscriptionListener;
import com.angelomelonas.grpcwebchat.subscription.SubscriptionService;
import com.google.protobuf.Any;
import io.grpc.stub.StreamObserver;

import java.util.UUID;

public class ChatSession implements SubscriptionListener {

    private StreamObserver<Message> streamObserver;
    private SubscriptionService subscriptionService;
    private SubscriptionData subscriptionData;
    private boolean isSubscribed;

    public ChatSession(StreamObserver<Message> streamObserver, SubscriptionService subscriptionService) {
        this.streamObserver = streamObserver;
        this.subscriptionService = subscriptionService;

        // Construct server message confirming client subscription.
        Message message = Message.newBuilder().setMessage("Welcome to gRPC Chat!").build();

        this.subscriptionData = new SubscriptionData(UUID.randomUUID(), Any.pack(message));

        this.subscriptionService.subscribe(subscriptionData, this);
        isSubscribed = true;
    }

    @Override
    public void onUnsubscribe() {
        this.isSubscribed = false;
        notify();
    }

    @Override
    public void onSubscriptionDataUpdate(Any subscriptionData) {
        this.subscriptionData.setSubscriptionData(subscriptionData);
    }

    public void sendMessage(Message message) {
        streamObserver.onNext(message);
    }

    public boolean isSubscribed() {
        return this.isSubscribed;
    }
}
