package com.angelomelonas.grpcwebchat.service;

import com.angelomelonas.grpcwebchat.Chat.AuthenticationRequest;
import com.angelomelonas.grpcwebchat.Chat.AuthenticationResponse;
import com.angelomelonas.grpcwebchat.Chat.Message;
import com.angelomelonas.grpcwebchat.Chat.MessageRequest;
import com.angelomelonas.grpcwebchat.Chat.MessageResponse;
import com.angelomelonas.grpcwebchat.Chat.SubscribedUsers;
import com.angelomelonas.grpcwebchat.Chat.SubscribedUsersRequest;
import com.angelomelonas.grpcwebchat.Chat.SubscriptionRequest;
import com.angelomelonas.grpcwebchat.Chat.UnsubscriptionRequest;
import com.angelomelonas.grpcwebchat.Chat.UnsubscriptionResponse;
import com.angelomelonas.grpcwebchat.Chat.User;
import com.angelomelonas.grpcwebchat.ChatServiceGrpc.ChatServiceImplBase;
import com.angelomelonas.grpcwebchat.common.ChatSession;
import com.angelomelonas.grpcwebchat.repository.ChatRepository;
import com.google.common.annotations.VisibleForTesting;
import io.grpc.Context;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@GRpcService
public class ChatService extends ChatServiceImplBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatService.class);

    public static final String SERVER_NAME = "Server";

    @Autowired
    ChatRepository chatRepository;

    private final Set<UUID> authenticatedClients;
    private final ConcurrentHashMap<UUID, ChatSession> connectedClients;

    public ChatService() {
        this.authenticatedClients = ConcurrentHashMap.newKeySet();
        this.connectedClients = new ConcurrentHashMap<>();

        // This timer will send all subscribed clients a list of subscribed clients.
        this.startSubscribedUserListTimer();
        // This timer will periodically remove all disconnected users from the client list.
        this.startCleanUpTimer();
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
        if (!checkIfValidUUID(request.getUuid())) {
            responseObserver.onError(getStatusRuntimeException(new IllegalArgumentException("Cannot subscribe. Invalid UUID received from client attempting to subscribe.")));
            LOGGER.error("Cannot subscribe. Invalid UUID received from client attempting to subscribe.");
            return;
        }

        UUID chatSessionId = UUID.fromString(request.getUuid());
        String username = request.getUsername();

        if (!this.authenticatedClients.contains(chatSessionId)) {
            responseObserver.onError(getStatusRuntimeException(new IllegalArgumentException("Cannot subscribe. Client not authenticated.")));
            LOGGER.warn("Cannot subscribe. Client not authenticated.");
            return;
        }

        // TODO: Duplicate usernames are currently not allowed This can potentially be improved by removing Session UUIDs as primary keys or by removing users when they disconnect.
        if (chatRepository.doesUserExist(username)) {
            responseObserver.onError(getStatusRuntimeException(new IllegalArgumentException("Cannot subscribe. Username already taken.")));
            LOGGER.warn("Cannot subscribe. Username already taken.");
            return;
        }

        ChatSession chatSession = new ChatSession(chatSessionId, Context.current());

        connectedClients.put(chatSessionId, chatSession);

        chatSession.subscribe(username, responseObserver);

        final Message clientSubscribed = Message.newBuilder()
            .setUuid(request.getUuid())
            .setUsername(SERVER_NAME)
            .setMessage("Welcome to gRPC Web Chat, " + username)
            .setTimestamp(Instant.now().toEpochMilli())
            .build();

        responseObserver.onNext(clientSubscribed);

        // Log the user.
        chatRepository.addUser(chatSessionId, username);
    }

    @Override
    public void unsubscribe(UnsubscriptionRequest request, StreamObserver<UnsubscriptionResponse> responseObserver) {
        UUID chatSessionId = UUID.fromString(request.getUuid());

        if (!this.connectedClients.containsKey(chatSessionId)) {
            responseObserver.onError(getStatusRuntimeException(new IllegalArgumentException("Cannot unsubscribe. Client does not exist.")));
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

        if (!this.connectedClients.containsKey(senderSessionId)) {
            responseObserver.onError(getStatusRuntimeException(new IllegalArgumentException("Cannot send message. Client does not exist.")));
            LOGGER.warn("Cannot send message. Client does not exist.");
            return;
        }

        ChatSession senderChatSession = this.connectedClients.get(senderSessionId);

        if (!senderChatSession.isConnected()) {
            responseObserver.onError(getStatusRuntimeException(new IllegalArgumentException("Cannot send message. Client is not connected.")));
            LOGGER.warn("Cannot send message. Client is not connected.");
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
            currentlyConnectedClients.forEach((id, chatSession) -> chatSession.sendMessage(newMessage));
        } catch (Throwable throwable) {
            responseObserver.onError(getStatusRuntimeException(throwable));
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

        if (!this.connectedClients.containsKey(chatSessionId)) {
            responseObserver.onError(getStatusRuntimeException(new IllegalArgumentException("Cannot obtain subscribed user list. Client does not exist.")));
            LOGGER.info("Cannot obtain subscribed user list. Client does not exist.");
            return;
        }

        ChatSession chatSession = this.connectedClients.get(chatSessionId);
        chatSession.setSubscribedUserListResponseObserver(responseObserver);
    }

    private void startSubscribedUserListTimer() {
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(() -> {
            // Make a temporary copy to prevent concurrency issues.
            HashMap<UUID, ChatSession> currentlyConnectedClients = new HashMap<>(this.connectedClients);

            List<User> userList = currentlyConnectedClients.entrySet().stream().map(chatSession -> User.newBuilder()
                .setUsername(chatSession.getValue().getUsername())
                .build()).collect(Collectors.toList());

            SubscribedUsers subscribedUsers = SubscribedUsers.newBuilder().addAllUsers(userList).build();
            try {
                currentlyConnectedClients.forEach((uuid, chatSession) -> chatSession.publishSubscribedUserList(subscribedUsers));
            } catch (Throwable throwable) {
                LOGGER.error("Exception while publishing subscribed users.", throwable);
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    private void startCleanUpTimer() {
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(() -> {
            Set<UUID> toRemove = new HashSet<>();

            this.connectedClients.forEach((uuid, chatSession) -> {
                if (!chatSession.isConnected()) {
                    toRemove.add(uuid);
                }
            });

            toRemove.forEach(uuid -> {
                LOGGER.info("Removing disconnected client with ID: {}", uuid);
                this.connectedClients.remove(uuid);
            });
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }

    public static StatusRuntimeException getStatusRuntimeException(Throwable throwable) {
        return Status.INTERNAL
            .withDescription(throwable.getMessage())
            .withCause(throwable)
            .asRuntimeException();
    }

    private boolean checkIfValidUUID(String uuid) {
        return Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$").matcher(uuid).matches();
    }

    @VisibleForTesting
    public void setChatRepository(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }
}
