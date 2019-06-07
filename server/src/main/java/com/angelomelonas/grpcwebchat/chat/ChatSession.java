package com.angelomelonas.grpcwebchat.chat;

import com.angelomelonas.grpcwebchat.Chat.Message;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class ChatSession {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatSession.class);

    private String username;
    private StreamObserver<Message> responseObserver;

    private boolean isSubscribed;

    public ChatSession() {
        this.isSubscribed = false;
    }

    public void subscribe(String username, StreamObserver responseObserver) {
        this.username = username;
        this.responseObserver = responseObserver;
        this.isSubscribed = true;

        LOGGER.info("Client with username {} subscribed.", this.username);
    }

    public void unsubscribe() {
        if (!this.isSubscribed) {
            throw new IllegalArgumentException("Cannot unsubscribe. Session not subscribed.");
        }

        try {
            // Try to close the stream.
            this.responseObserver.onCompleted();
        } catch (IllegalStateException exception) {
            throw exception;
        }
        this.isSubscribed = false;

        LOGGER.info("Client with username {} unsubscribed.", this.username);
    }

    public void sendMessage(String message) {
        Message newMessage = Message.newBuilder()
                .setUsername(this.username)
                .setMessage(message)
                .setTimestamp(Instant.now().toEpochMilli())
                .build();
        try {
            this.responseObserver.onNext(newMessage);
            LOGGER.info("Message sent successfully from user with username {}.", this.username);
        } catch (IllegalStateException exception) {
            throw exception;
        } finally {
            // TODO: Investigate what happens here.
            this.responseObserver.onCompleted();
        }
    }
}
