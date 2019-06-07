package com.angelomelonas.grpcwebchat.integration;

import com.angelomelonas.grpcwebchat.Chat.AuthenticationRequest;
import com.angelomelonas.grpcwebchat.Chat.AuthenticationResponse;
import com.angelomelonas.grpcwebchat.Chat.Message;
import com.angelomelonas.grpcwebchat.Chat.MessageRequest;
import com.angelomelonas.grpcwebchat.Chat.SubscriptionRequest;
import com.angelomelonas.grpcwebchat.Chat.UnsubscriptionRequest;
import com.angelomelonas.grpcwebchat.ChatServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Iterator;
import java.util.UUID;

@Component
public class ChatServiceTestClient {

    private ChatServiceGrpc.ChatServiceBlockingStub chatServiceBlockingStub;

    @PostConstruct
    private void init() {
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost", 9090).usePlaintext().build();
        chatServiceBlockingStub = ChatServiceGrpc.newBlockingStub(managedChannel);
    }

    public AuthenticationResponse authenticate() {
        AuthenticationRequest authenticationRequest = AuthenticationRequest.newBuilder().build();

        AuthenticationResponse authenticationResponse = chatServiceBlockingStub.authenticate(authenticationRequest);

        return authenticationResponse;
    }

    public Iterator<Message> subscribe(UUID uuid, String username) {
        SubscriptionRequest subscriptionRequest = SubscriptionRequest.newBuilder()
                .setUuid(String.valueOf(uuid))
                .setUsername(username)
                .build();

        Iterator<Message> subscription = chatServiceBlockingStub.subscribe(subscriptionRequest);

        return subscription;
    }

    public Message unsubscribe(UUID uuid) {
        UnsubscriptionRequest unsubscriptionRequest = UnsubscriptionRequest.newBuilder()
                .setUuid(String.valueOf(uuid))
                .build();

        Message unsubscriptionMessage = chatServiceBlockingStub.unsubscribe(unsubscriptionRequest);

        return unsubscriptionMessage;
    }


    public Message sendMessage(UUID uuid, String username, String message) {
        MessageRequest messageRequest = MessageRequest.newBuilder()
                .setUuid(String.valueOf(uuid))
                .setUsername(username)
                .setMessage(message)
                .build();

        Message messageResponse = chatServiceBlockingStub.sendMessage(messageRequest);

        return messageResponse;
    }
}
