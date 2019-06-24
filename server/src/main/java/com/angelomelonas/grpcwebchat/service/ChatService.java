package com.angelomelonas.grpcwebchat.service;

import com.angelomelonas.grpcwebchat.Chat.AuthenticationRequest;
import com.angelomelonas.grpcwebchat.Chat.AuthenticationResponse;
import com.angelomelonas.grpcwebchat.Chat.Message;
import com.angelomelonas.grpcwebchat.Chat.MessageRequest;
import com.angelomelonas.grpcwebchat.Chat.MessageResponse;
import com.angelomelonas.grpcwebchat.Chat.SubscriptionRequest;
import com.angelomelonas.grpcwebchat.Chat.UnsubscriptionRequest;
import com.angelomelonas.grpcwebchat.Chat.UnsubscriptionResponse;
import com.angelomelonas.grpcwebchat.ChatServiceGrpc.ChatServiceImplBase;
import com.angelomelonas.grpcwebchat.common.ChatSession;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@GRpcService
public class ChatService extends ChatServiceImplBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatService.class);

    private final ConcurrentHashMap<UUID, ChatSession> chatSessionHashMap;

    public ChatService() {
        this.chatSessionHashMap = new ConcurrentHashMap<>();
    }

    @Override
    public void authenticate(AuthenticationRequest request, StreamObserver<AuthenticationResponse> responseObserver) {
        ChatSession chatSession = new ChatSession();
        UUID newChatSessionId = UUID.randomUUID();

        // Add the ChatSession.
        this.chatSessionHashMap.put(newChatSessionId, chatSession);

        final AuthenticationResponse authenticationResponse = AuthenticationResponse.newBuilder()
                .setUuid(String.valueOf(newChatSessionId))
                .build();

        // Respond with the newly generated UUID.
        responseObserver.onNext(authenticationResponse);
        responseObserver.onCompleted();

        LOGGER.info("ChatSession with UUID {} created.", newChatSessionId);
    }

    @Override
    public void subscribe(SubscriptionRequest request, StreamObserver<Message> responseObserver) {
        UUID chatSessionId = UUID.fromString(request.getUuid());

        ChatSession chatSession = chatSessionHashMap.get(chatSessionId);

        if (chatSession == null) {
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException("Cannot subscribe. Client not authenticated.");
            responseObserver.onError(illegalArgumentException);
            LOGGER.error("Chat Session was null while trying to subscribe.", illegalArgumentException);
            return;
        }

        chatSession.subscribe(chatSessionId, request.getUsername(), responseObserver);

        final Message clientSubscribed = Message.newBuilder()
                .setUuid(request.getUuid())
                .setUsername("Server")
                .setMessage("Client subscribed successfully.")
                .setTimestamp(Instant.now().toEpochMilli())
                .build();

        responseObserver.onNext(clientSubscribed);

        LOGGER.info("ChatSession with UUID {} and username {} subscribed.", chatSessionId, chatSession.getUsername());
    }

    @Override
    public void unsubscribe(UnsubscriptionRequest request, StreamObserver<UnsubscriptionResponse> responseObserver) {
        UUID chatSessionId = UUID.fromString(request.getUuid());

        ChatSession chatSession = this.chatSessionHashMap.remove(chatSessionId);

        if (chatSession == null) {
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException("Cannot unsubscribe. Client not subscribed.");
            LOGGER.error("Chat Session was null while trying to unsubscribe.", illegalArgumentException);
            responseObserver.onError(illegalArgumentException);
            return;
        }

        chatSession.unsubscribe();

        final UnsubscriptionResponse unsubscriptionResponse = UnsubscriptionResponse.newBuilder()
                .setUuid(request.getUuid())
                .setMessage("Client successfully unsubscribed.")
                .build();

        responseObserver.onNext(unsubscriptionResponse);
        responseObserver.onCompleted();

        LOGGER.info("ChatSession with UUID {} unsubscribed.", chatSessionId);
    }

    @Override
    public void sendMessage(MessageRequest request, StreamObserver<MessageResponse> responseObserver) {
        UUID chatSessionId = UUID.fromString(request.getUuid());
        String message = request.getMessage();

        ChatSession currentClientChatSession = this.chatSessionHashMap.get(chatSessionId);

        if (currentClientChatSession == null) {
            throw new IllegalArgumentException("Cannot send message. ChatSession does not exist.");
        }

        try {
            // Respond to the client which sent the message.
            currentClientChatSession.sendMessage(message);

            // Broadcast the message to everyone.
            HashMap<UUID, ChatSession> temp = new HashMap<>(this.chatSessionHashMap);

            temp.forEach(((id, chatSession) -> {
                if (chatSession != null && !id.equals(chatSessionId)) {
                    chatSession.sendMessage(message);
                }
            }));

            LOGGER.info("Client with username {} sent a message.", currentClientChatSession.getUsername());
        } catch (Throwable throwable) {
            LOGGER.error("Exception while sending message.", throwable);
            responseObserver.onError(throwable);
        }

        final MessageResponse messageResponse = MessageResponse.newBuilder()
                .setUuid(request.getUuid())
                .setMessage("Message sent successfully.")
                .build();

        responseObserver.onNext(messageResponse);
        responseObserver.onCompleted();
    }
}
