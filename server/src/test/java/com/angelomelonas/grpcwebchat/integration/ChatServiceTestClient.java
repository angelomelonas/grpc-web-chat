package com.angelomelonas.grpcwebchat.integration;

import com.angelomelonas.grpcwebchat.Chat.AuthenticationRequest;
import com.angelomelonas.grpcwebchat.Chat.AuthenticationResponse;
import com.angelomelonas.grpcwebchat.Chat.Message;
import com.angelomelonas.grpcwebchat.Chat.MessageRequest;
import com.angelomelonas.grpcwebchat.Chat.MessageResponse;
import com.angelomelonas.grpcwebchat.Chat.SubscriptionRequest;
import com.angelomelonas.grpcwebchat.Chat.UnsubscriptionRequest;
import com.angelomelonas.grpcwebchat.Chat.UnsubscriptionResponse;
import com.angelomelonas.grpcwebchat.ChatServiceGrpc;
import com.angelomelonas.grpcwebchat.ChatServiceGrpc.ChatServiceBlockingStub;
import com.angelomelonas.grpcwebchat.ChatServiceGrpc.ChatServiceStub;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ChatServiceTestClient {
    private final ManagedChannel managedChannel;
    private final ChatServiceBlockingStub blockingStub;
    private final ChatServiceStub asyncStub;

    public ChatServiceTestClient() {
        managedChannel = ManagedChannelBuilder.forAddress("127.0.0.1", 9090).usePlaintext().build();
        blockingStub = ChatServiceGrpc.newBlockingStub(managedChannel);
        asyncStub = ChatServiceGrpc.newStub(managedChannel);
    }

    public void shutdown() throws InterruptedException {
        managedChannel.shutdown().awaitTermination(3, TimeUnit.SECONDS);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        return blockingStub.authenticate(authenticationRequest);
    }

    public void subscribe(SubscriptionRequest subscriptionRequest, StreamObserver<Message> responseObserver) {
        asyncStub.subscribe(subscriptionRequest, responseObserver);
    }

    public UnsubscriptionResponse unsubscribe(UnsubscriptionRequest unsubscriptionRequest) {
        return blockingStub.unsubscribe(unsubscriptionRequest);
    }

    public MessageResponse sendMessage(MessageRequest messageRequest) {
        return blockingStub.sendMessage(messageRequest);
    }
}
