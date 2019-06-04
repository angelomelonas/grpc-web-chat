package com.angelomelonas.grpcwebchat.chat;

import com.angelomelonas.grpcwebchat.Chat.Message;
import io.grpc.stub.StreamObserver;

import java.util.UUID;

public class ChatSession {

    private StreamObserver<Message> streamObserver;

    private final UUID uuid;

    public ChatSession(UUID uuid) {
        this.uuid = uuid;
    }

    public void unsubscribe(UUID uuid) {
        // TODO
    }

    public void subscribe(UUID uuid, String username, StreamObserver responseObserver) {
        // TODO
    }

    public void sendMessage(String username, String message) {
        // TODO
    }
}
