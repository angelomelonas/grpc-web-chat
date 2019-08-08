package com.angelomelonas.grpcwebchat.common;

import com.angelomelonas.grpcwebchat.Chat.Message;
import com.angelomelonas.grpcwebchat.Chat.SubscribedUsers;
import io.grpc.Context;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class ChatSession {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatSession.class);

    private UUID sessionId;
    private String username;
    private Context currentContext;
    private StreamObserver<Message> subscriptionResponseObserver;
    private StreamObserver<SubscribedUsers> usersListResponseObserver;

    public ChatSession(UUID sessionId, Context currentContext) {
        this.sessionId = sessionId;
        this.currentContext = currentContext;
        LOGGER.info("ChatSession with UUID {} has been created.", sessionId);
    }

    public synchronized void subscribe(String username, StreamObserver responseObserver) {
        if (!this.isConnected()) {
            notConnected("Subscribe");
            return;
        }
        this.username = username;
        this.subscriptionResponseObserver = responseObserver;

        LOGGER.info("Client with username {} subscribed.", this.username);
    }

    public synchronized void unsubscribe() {
        if (!this.isConnected()) {
            notConnected("Unsubscribe");
            return;
        }

        // Try to close the streams.
        try {
            this.subscriptionResponseObserver.onCompleted();
            this.subscriptionResponseObserver = null;
        } catch (IllegalStateException exception) {
            this.subscriptionResponseObserver.onError(new StatusRuntimeException(Status.fromThrowable(new IllegalArgumentException("Failed to publish list of subscribed clients"))));
            LOGGER.error("An error was thrown while trying to unsubscribe user with session ID {}.", this.sessionId, exception);
            throw exception;
        }

        try {
            this.usersListResponseObserver.onCompleted();
            this.usersListResponseObserver = null;
        } catch (IllegalStateException exception) {
            this.usersListResponseObserver.onError(new StatusRuntimeException(Status.fromThrowable(new IllegalArgumentException("An error was thrown while trying to unsubscribe user with session ID {} from subscribed clients list."))));
            LOGGER.error("An error was thrown while trying to unsubscribe user with session ID {} from subscribed clients list.", this.sessionId, exception);
            throw exception;
        }

        LOGGER.info("Client with session ID {} has unsubscribed.", this.sessionId);
    }

    public synchronized void sendMessage(Message newMessage) {
        if (!this.isConnected()) {
            notConnected("Send Message");
            return;
        }

        try {
            this.subscriptionResponseObserver.onNext(newMessage);
            LOGGER.info("Message sent successfully from user with username {}.", newMessage.getUsername());
        } catch (IllegalStateException exception) {
            LOGGER.error("Send Message Failed", exception);
            throw exception;
        }
    }

    public void setSubscribedUserListResponseObserver(StreamObserver<SubscribedUsers> responseObserver) {
        if (!this.isConnected()) {
            notConnected("Set Subscribed User List Response Observer");
            return;
        }
        this.usersListResponseObserver = responseObserver;
    }

    public synchronized void publishSubscribedUserList(SubscribedUsers subscribedUsers) {
        if (!this.isConnected()) {
            notConnected("Publish Subscribed User List");
            return;
        }

        try {
            this.usersListResponseObserver.onNext(subscribedUsers);
        } catch (IllegalStateException exception) {
            this.usersListResponseObserver = null;
            this.usersListResponseObserver.onError(new StatusRuntimeException(Status.fromThrowable(new IllegalArgumentException("Failed to publish list of subscribed clients"))));
            LOGGER.error("Failed to publish list of subscribed clients", exception);
            throw exception;
        }
    }

    public String getUsername() {
        return this.username;
    }

    public synchronized boolean isConnected() {
        return !this.currentContext.isCancelled();
    }

    private void notConnected(String action) {
        LOGGER.warn("{}: Client not connected. Chat Session ID {}.", action, this.sessionId);
    }
}
