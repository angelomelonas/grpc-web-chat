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
        UUID newChatSessionId = UUID.randomUUID();
        ChatSession chatSession = new ChatSession(newChatSessionId);

        // Add the ChatSession.
        this.chatSessionHashMap.put(newChatSessionId, chatSession);

        final AuthenticationResponse authenticationResponse = AuthenticationResponse.newBuilder()
                .setUuid(String.valueOf(newChatSessionId))
                .build();

        // Respond with the newly generated UUID.
        responseObserver.onNext(authenticationResponse);
        responseObserver.onCompleted();
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

        chatSession.subscribe(request.getUsername(), responseObserver);

        final Message clientSubscribed = Message.newBuilder()
                .setUuid(request.getUuid())
                .setUsername("Server")
                .setMessage("User " + chatSession.getUsername() + " has subscribed.")
                .setTimestamp(Instant.now().toEpochMilli())
                .build();

        responseObserver.onNext(clientSubscribed);
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
                .setMessage("User " + chatSession.getUsername() + " has unsubscribed.")
                .build();

        responseObserver.onNext(unsubscriptionResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void sendMessage(MessageRequest request, StreamObserver<MessageResponse> responseObserver) {
        UUID senderSessionId = UUID.fromString(request.getUuid());

        ChatSession currentClientChatSession = this.chatSessionHashMap.get(senderSessionId);

        if (currentClientChatSession == null) {
            throw new IllegalArgumentException("Cannot send message. ChatSession does not exist.");
        }

        try {
            // Broadcast the message to everyone, including the client which sent the message.
            HashMap<UUID, ChatSession> temp = new HashMap<>(this.chatSessionHashMap);

            temp.forEach(((id, chatSession) -> {
                if (chatSession != null) {
                    chatSession.sendMessage(senderSessionId, currentClientChatSession.getUsername(), request.getMessage());
                }
            }));
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
