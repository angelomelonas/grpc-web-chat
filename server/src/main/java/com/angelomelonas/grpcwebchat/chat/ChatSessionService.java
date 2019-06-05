package com.angelomelonas.grpcwebchat.chat;

import com.angelomelonas.grpcwebchat.Chat.AuthenticationResponse;
import com.angelomelonas.grpcwebchat.Chat.Message;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;

public class ChatSessionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatSessionService.class);

    HashMap<UUID, ChatSession> chatSessionHashMap;

    public ChatSessionService() {
        this.chatSessionHashMap = new HashMap<>();
    }

    public void create(StreamObserver<AuthenticationResponse> responseObserver, UUID uuid) {
        ChatSession chatSession = new ChatSession(uuid);

        // Add the ChatSession.
        this.chatSessionHashMap.put(uuid, chatSession);

        // Respond with the newly generated UUID.
        responseObserver.onNext(AuthenticationResponse.newBuilder().setUuid(String.valueOf(uuid)).build());
        responseObserver.onCompleted();

        LOGGER.info("ChatSession with UUID {} created.", uuid);
    }

    public void subscribe(UUID uuid, String username, StreamObserver<Message> responseObserver) {
        ChatSession chatSession = chatSessionHashMap.get(uuid);

        if (chatSession == null) {
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException("Cannot subscribe. Client not authenticated.");
            responseObserver.onError(illegalArgumentException);
            throw illegalArgumentException;
        }

        chatSession.subscribe(uuid, username, responseObserver);

        final Message clientSubscribed = Message.newBuilder()
                .setUsername("Server")
                .setMessage("Client subscribed successfully.")
                .setTimestamp(Instant.now().toEpochMilli())
                .build();

        responseObserver.onNext(clientSubscribed);

        LOGGER.info("ChatSession with UUID {} and username {} subscribed.", uuid, username);
    }

    public void unsubscribe(UUID uuid, StreamObserver<Message> responseObserver) {
        ChatSession chatSession = this.chatSessionHashMap.remove(uuid);

        if (chatSession == null) {
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException("Cannot unsubscribe. Client not subscribed.");
            responseObserver.onError(illegalArgumentException);
            throw illegalArgumentException;
        }

        chatSession.unsubscribe(uuid);

        final Message clientUnsubscribed = Message.newBuilder()
                .setUsername("Server")
                .setMessage("You have successfully been unsubscribed")
                .setTimestamp(Instant.now().toEpochMilli())
                .build();

        responseObserver.onNext(clientUnsubscribed);
        responseObserver.onCompleted();

        LOGGER.info("ChatSession with UUID {} unsubscribed.", uuid);
    }

    public synchronized void sendMessage(UUID uuid, String username, String message, StreamObserver<Message> responseObserver) {
        if (!this.chatSessionHashMap.containsKey(uuid)) {
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException("Cannot send message. ChatSession does not exist.");
            responseObserver.onError(illegalArgumentException);
            throw illegalArgumentException;
        }

        // Broadcast the message to everyone.
        this.chatSessionHashMap.forEach(((id, chatSession) -> chatSession.sendMessage(username, message)));

        final Message messageSent = Message.newBuilder()
                .setUsername("Server")
                .setMessage("Message sent successfully.")
                .setTimestamp(Instant.now().toEpochMilli())
                .build();

        responseObserver.onNext(messageSent);
        responseObserver.onCompleted();

        LOGGER.info("Client with username {} sent a message.", username);
    }
}
