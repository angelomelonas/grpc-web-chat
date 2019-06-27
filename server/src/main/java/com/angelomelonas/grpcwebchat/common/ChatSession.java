package com.angelomelonas.grpcwebchat.common;

import com.angelomelonas.grpcwebchat.Chat.Message;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;

public class ChatSession {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatSession.class);

    private UUID sessionId;
    private String username;
    private boolean isSubscribed;
    private StreamObserver<Message> responseObserver;

    public ChatSession(UUID sessionId) {
        this.sessionId = sessionId;
        this.isSubscribed = false;
        LOGGER.info("ChatSession with UUID {} has been created.", sessionId);
    }

    public synchronized void subscribe(String username, StreamObserver responseObserver) {
        this.username = username;
        this.responseObserver = responseObserver;
        this.isSubscribed = true;

        LOGGER.info("Client with username {} subscribed.", this.username);
    }

    public synchronized void unsubscribe() {
        if (!this.isSubscribed) {
            throw new IllegalArgumentException("Cannot unsubscribe. Session not subscribed.");
        }

        try {
            // Try to close the stream.
            this.responseObserver.onCompleted();
        } catch (IllegalStateException exception) {
            LOGGER.error("An error was thrown while trying to unsubscribe user with username {}", this.username, exception);
            throw exception;
        } finally {
            this.isSubscribed = false;
        }

        LOGGER.info("Client with username {} unsubscribed.", this.username);
    }

    public synchronized void sendMessage(UUID senderId, String username, String message, long timestamp) {
        if (!this.isSubscribed) {
            LOGGER.warn("Client not subscribed. Message was not sent.", this.username);
            return;
        }

        Message newMessage = Message.newBuilder()
                .setUuid(String.valueOf(senderId))
                .setUsername(username)
                .setMessage(message)
                .setTimestamp(timestamp)
                .build();
        try {
            this.responseObserver.onNext(newMessage);
            LOGGER.info("Message sent successfully from user with username {}.", this.username);
            return;
        } catch (IllegalStateException exception) {
            LOGGER.error("Send Message Failed", exception);
            throw exception;
        }
    }

    public String getUsername() {
        return this.username;
    }

    public synchronized boolean isSubscribed() {
        return this.isSubscribed;
    }
}
