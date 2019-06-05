package com.angelomelonas.grpcwebchat.chat;

import com.angelomelonas.grpcwebchat.Chat.Message;
import io.grpc.stub.StreamObserver;

import java.util.UUID;

public class ChatSession {

    private final UUID uuid;
    private String username;
    private StreamObserver<Message> responseObserver;

    private boolean isSubscribed;

    public ChatSession(UUID uuid) {
        this.uuid = uuid;
        this.isSubscribed = false;
    }

    public void subscribe(UUID uuid, String username, StreamObserver responseObserver) {
        this.username = username;
        this.responseObserver = responseObserver;
        this.isSubscribed = true;
    }

    public void unsubscribe(UUID uuid) {
        // TODO: Perhaps throw Exceptions or return false?

        if (!this.isSubscribed) {
            return;
        }
        this.responseObserver.onCompleted();
        this.isSubscribed = false;
    }

    public void sendMessage(String username, String message) {
        // TODO
    }
}
