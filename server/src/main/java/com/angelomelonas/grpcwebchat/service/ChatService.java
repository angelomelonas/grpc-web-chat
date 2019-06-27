package com.angelomelonas.grpcwebchat.service;

import com.angelomelonas.grpcwebchat.Chat.AuthenticationRequest;
import com.angelomelonas.grpcwebchat.Chat.AuthenticationResponse;
import com.angelomelonas.grpcwebchat.Chat.Message;
import com.angelomelonas.grpcwebchat.Chat.MessageRequest;
import com.angelomelonas.grpcwebchat.Chat.MessageResponse;
import com.angelomelonas.grpcwebchat.Chat.SubscribedUsers;
import com.angelomelonas.grpcwebchat.Chat.SubscribedUsers.Builder;
import com.angelomelonas.grpcwebchat.Chat.SubscribedUsersRequest;
import com.angelomelonas.grpcwebchat.Chat.SubscriptionRequest;
import com.angelomelonas.grpcwebchat.Chat.UnsubscriptionRequest;
import com.angelomelonas.grpcwebchat.Chat.UnsubscriptionResponse;
import com.angelomelonas.grpcwebchat.Chat.User;
import com.angelomelonas.grpcwebchat.ChatServiceGrpc.ChatServiceImplBase;
import com.angelomelonas.grpcwebchat.common.ChatSession;
import com.angelomelonas.grpcwebchat.repository.ChatRepository;
import com.google.common.annotations.VisibleForTesting;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@GRpcService
public class ChatService extends ChatServiceImplBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatService.class);

    @Autowired
    ChatRepository chatRepository;

    private final Set<UUID> authenticatedClients;
    private final ConcurrentHashMap<UUID, ChatSession> connectedClients;

    public ChatService() {
        this.authenticatedClients = ConcurrentHashMap.newKeySet();
        this.connectedClients = new ConcurrentHashMap<>();

        // This timer will send all subscribed clients a list of subscribed clients.
        startSubscribedUserListTimer();

    }

    @Override
    public void authenticate(AuthenticationRequest request, StreamObserver<AuthenticationResponse> responseObserver) {
        UUID newSessionId = UUID.randomUUID();

        authenticatedClients.add(newSessionId);

        final AuthenticationResponse authenticationResponse = AuthenticationResponse.newBuilder()
                .setUuid(String.valueOf(newSessionId))
                .build();

        // Respond with the newly generated UUID.
        responseObserver.onNext(authenticationResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void subscribe(SubscriptionRequest request, StreamObserver<Message> responseObserver) {
        UUID chatSessionId = UUID.fromString(request.getUuid());
        String username = request.getUsername();

        if (!this.authenticatedClients.contains(chatSessionId)) {
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException("Cannot subscribe. Client not authenticated.");
            responseObserver.onError(illegalArgumentException);
            LOGGER.error("Cannot subscribe. Client not authenticated.", illegalArgumentException);
            return;
        }

        ChatSession chatSession = new ChatSession(chatSessionId);

        connectedClients.put(chatSessionId, chatSession);

        chatSession.subscribe(username, responseObserver);

        final Message clientSubscribed = Message.newBuilder()
                .setUuid(request.getUuid())
                .setUsername("Server")
                .setMessage("User " + username + " has subscribed.")
                .setTimestamp(Instant.now().toEpochMilli())
                .build();

        responseObserver.onNext(clientSubscribed);

        // Log the user.
        chatRepository.addUser(chatSessionId, username);
    }

    @Override
    public void unsubscribe(UnsubscriptionRequest request, StreamObserver<UnsubscriptionResponse> responseObserver) {
        UUID chatSessionId = UUID.fromString(request.getUuid());

        if (!this.connectedClients.contains(chatSessionId)) {
            responseObserver.onError(new IllegalArgumentException("Cannot unsubscribe. Client does not exist."));
            LOGGER.warn("Cannot unsubscribe. Client does not exist.");
            return;
        }

        ChatSession chatSession = this.connectedClients.get(chatSessionId);
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
        String message = request.getMessage();
        Instant timestamp = Instant.now();

        if (!this.connectedClients.contains(senderSessionId)) {
            responseObserver.onError(new IllegalArgumentException("Cannot send message. Client does not exist."));
            LOGGER.warn("Cannot send message. Client does not exist.");
            return;
        }

        ChatSession senderChatSession = this.connectedClients.get(senderSessionId);

        if (!senderChatSession.isSubscribed()) {
            responseObserver.onError(new IllegalArgumentException("Cannot send message. Client is not subscribed."));
            LOGGER.warn("Cannot send message. Client is not subscribed.");
            return;
        }

        // Make a temporary copy to prevent concurrency issues.
        HashMap<UUID, ChatSession> currentlyConnectedClients = new HashMap<>(this.connectedClients);

        Message newMessage = Message.newBuilder()
                .setUuid(String.valueOf(senderSessionId))
                .setUsername(senderChatSession.getUsername())
                .setMessage(message)
                .setTimestamp(timestamp.toEpochMilli())
                .build();

        try {
            // Broadcast the message to everyone, including the client which sent the message.
            currentlyConnectedClients.forEach(((id, chatSession) -> chatSession.sendMessage(newMessage)));
        } catch (Throwable throwable) {
            responseObserver.onError(throwable);
            LOGGER.error("Exception while sending message.", throwable);
        }

        final MessageResponse messageResponse = MessageResponse.newBuilder()
                .setUuid(request.getUuid())
                .setMessage("Message sent successfully.")
                .build();

        responseObserver.onNext(messageResponse);
        responseObserver.onCompleted();

        // Log the message.
        chatRepository.addMessage(senderSessionId, message, timestamp);
    }

    @Override
    public void subscribedUserList(SubscribedUsersRequest request, StreamObserver<SubscribedUsers> responseObserver) {
        UUID chatSessionId = UUID.fromString(request.getUuid());

        if (!this.connectedClients.contains(chatSessionId)) {
            responseObserver.onError(new IllegalArgumentException("Cannot obtain subscribed user list. Client does not exist."));
            LOGGER.warn("Cannot obtain subscribed user list. Client does not exist.");
            return;
        }

        ChatSession chatSession = this.connectedClients.get(chatSessionId);

        if (!chatSession.isSubscribed()) {
            responseObserver.onError(new IllegalArgumentException("Cannot obtain subscribed user list. Client not subscribed."));
            LOGGER.warn("Cannot obtain subscribed user list. Client not subscribed.");
            return;
        }

        chatSession.setSubscribedUserListResponseObserver(responseObserver);
    }

    private void startSubscribedUserListTimer() {
        TimerTask timerTask = new TimerTask() {

            @Override
            public void run() {
                publishSubscribedUserList();
            }
        };

        Timer timer = new Timer("MyTimer");
        timer.scheduleAtFixedRate(timerTask, 30, 1000);
    }

    private void publishSubscribedUserList() {
        // Make a temporary copy to prevent concurrency issues.
        HashMap<UUID, ChatSession> currentlyConnectedClients = new HashMap<>(this.connectedClients);

        List<User> userList = currentlyConnectedClients.entrySet().stream().map(chatSession -> User.newBuilder()
                .setUsername(chatSession.getValue().getUsername())
                .setSubscribed(chatSession.getValue().isSubscribed())
                .build()).collect(Collectors.toList());

        SubscribedUsers subscribedUsers = SubscribedUsers.newBuilder().addAllUsers(userList).build();
        try {
            currentlyConnectedClients.forEach((uuid, chatSession) -> chatSession.publishSubscribedUserList(subscribedUsers));
        } catch (Throwable throwable) {
            LOGGER.error("Exception while publishing subscribed users.", throwable);
        }
    }

    @VisibleForTesting
    public void setChatRepository(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }
}
