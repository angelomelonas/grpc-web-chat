package com.angelomelonas.grpcwebchat.common;

import com.angelomelonas.grpcwebchat.Chat.Message;
import com.angelomelonas.grpcwebchat.Chat.SubscribedUsers;
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
    private volatile boolean isSubscribed;
    private StreamObserver<Message> subscriptionResponseObserver;
    private StreamObserver<SubscribedUsers> usersListResponseObserver;

    public ChatSession(UUID sessionId) {
        this.sessionId = sessionId;
        this.isSubscribed = false;
        LOGGER.info("ChatSession with UUID {} has been created.", sessionId);
    }

    public synchronized void subscribe(String username, StreamObserver responseObserver) {
        if (this.isSubscribed) {
            LOGGER.warn("Cannot subscribe. Client with session ID {} already subscribed.", this.sessionId);
            return;
        }
        this.isSubscribed = true;

        this.username = username;
        this.subscriptionResponseObserver = responseObserver;

        LOGGER.info("Client with username {} subscribed.", this.username);
    }

    public synchronized void unsubscribe() {
        if (!this.isSubscribed) {
            LOGGER.warn("Cannot unsubscribe. Client with session ID {} already unsubscribed.", this.sessionId);
            return;
        }

        this.isSubscribed = false;

        // Try to close the streams.
        try {
            this.subscriptionResponseObserver.onCompleted();
            this.subscriptionResponseObserver = null;
        } catch (IllegalStateException exception) {
            this.subscriptionResponseObserver.onError(new IllegalArgumentException("Failed to publish list of subscribed clients"));
            LOGGER.error("An error was thrown while trying to unsubscribe user with session ID {}.", this.sessionId, exception);
            throw exception;
        }

        try {
            this.usersListResponseObserver.onCompleted();
            this.usersListResponseObserver = null;
        } catch (IllegalStateException exception) {
            this.usersListResponseObserver.onError(new IllegalArgumentException("An error was thrown while trying to unsubscribe user with session ID {} from subscribed clients list."));
            LOGGER.error("An error was thrown while trying to unsubscribe user with session ID {} from subscribed clients list.", this.sessionId, exception);
            throw exception;
        }

        LOGGER.info("Client with session ID {} has unsubscribed.", this.sessionId);
    }

    public synchronized void sendMessage(Message newMessage) {
        if (!this.isSubscribed) {
            LOGGER.info("Client not subscribed. Cannot send message to client with session ID {}.", this.sessionId);
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
        if (!this.isSubscribed) {
            LOGGER.warn("Client not subscribed. Client with session ID {} cannot obtain list of subscribed users.", this.sessionId);
            // TODO: Investigate
            responseObserver.onCompleted();
            return;
        }
        this.usersListResponseObserver = responseObserver;
    }

    public synchronized void publishSubscribedUserList(SubscribedUsers subscribedUsers) {
        if (!this.isSubscribed || this.usersListResponseObserver == null) {
            LOGGER.info("Client not subscribed. Cannot publish list of subscribed clients to client with session ID {}.", this.sessionId);
            return;
        }

        try {
            this.usersListResponseObserver.onNext(subscribedUsers);
        } catch (IllegalStateException exception) {
            this.isSubscribed = false;
            this.usersListResponseObserver = null;
            this.usersListResponseObserver.onError(new StatusRuntimeException(Status.fromThrowable(new IllegalArgumentException("Failed to publish list of subscribed clients"))));
            LOGGER.error("Failed to publish list of subscribed clients", exception);
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
