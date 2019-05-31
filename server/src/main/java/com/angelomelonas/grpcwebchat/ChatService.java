package com.angelomelonas.grpcwebchat;

import com.angelomelonas.grpcwebchat.Chat.Message;
import com.angelomelonas.grpcwebchat.Chat.MessageRequest;
import com.angelomelonas.grpcwebchat.Chat.SubscriptionRequest;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GRpcService
public class ChatService extends ChatServiceGrpc.ChatServiceImplBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatService.class);

    @Override
    public void subscribe(SubscriptionRequest request, StreamObserver<Message> responseObserver) {
        // TODO
    }

    @Override
    public void unsubscribe(SubscriptionRequest request, StreamObserver<Message> responseObserver) {
        // TODO
    }

    @Override
    public void sendMessage(MessageRequest request, StreamObserver<Message> responseObserver) {
        // TODO
    }
}
