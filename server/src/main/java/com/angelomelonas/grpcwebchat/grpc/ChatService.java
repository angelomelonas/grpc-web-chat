package com.angelomelonas.grpcwebchat.grpc;

import com.angelomelonas.grpcwebchat.Chat.Message;
import com.angelomelonas.grpcwebchat.Chat.MessageRequest;
import com.angelomelonas.grpcwebchat.Chat.SubscriptionRequest;
import com.angelomelonas.grpcwebchat.ChatServiceGrpc.ChatServiceImplBase;
import com.angelomelonas.grpcwebchat.chat.ChatSession;
import com.angelomelonas.grpcwebchat.subscription.SubscriptionService;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@GRpcService
public class ChatService extends ChatServiceImplBase {
    private SubscriptionService subscriptionService;
    private ChatSession chatSession;

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatService.class);

    public ChatService() {
        this.subscriptionService = new SubscriptionService();
    }

    @Override
    public void subscribe(SubscriptionRequest request, StreamObserver<Message> responseObserver) {
        new ChatSession(responseObserver, subscriptionService);
    }

    @Override
    public void unsubscribe(SubscriptionRequest request, StreamObserver<Message> responseObserver) {
        // TODO: Get the following ID from the request.
        UUID clientId = UUID.randomUUID();
        subscriptionService.unsubscribe(clientId);
    }

    @Override
    public void sendMessage(MessageRequest request, StreamObserver<Message> responseObserver) {
        // TODO
    }
}
