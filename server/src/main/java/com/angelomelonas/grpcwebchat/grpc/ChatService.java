package com.angelomelonas.grpcwebchat.grpc;

import com.angelomelonas.grpcwebchat.Chat.AuthenticationRequest;
import com.angelomelonas.grpcwebchat.Chat.AuthenticationResponse;
import com.angelomelonas.grpcwebchat.Chat.Message;
import com.angelomelonas.grpcwebchat.Chat.MessageRequest;
import com.angelomelonas.grpcwebchat.Chat.SubscriptionRequest;
import com.angelomelonas.grpcwebchat.Chat.UnsubscriptionRequest;
import com.angelomelonas.grpcwebchat.ChatServiceGrpc.ChatServiceImplBase;
import com.angelomelonas.grpcwebchat.chat.ChatSessionService;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;

import java.util.UUID;

@GRpcService
public class ChatService extends ChatServiceImplBase {
    private final ChatSessionService chatSessionService;

    public ChatService() {
        chatSessionService = new ChatSessionService();
    }

    @Override
    public void authenticate(AuthenticationRequest request, StreamObserver<AuthenticationResponse> responseObserver) {
        chatSessionService.create(responseObserver, UUID.randomUUID());
    }

    @Override
    public void subscribe(SubscriptionRequest request, StreamObserver<Message> responseObserver) {
        chatSessionService.subscribe(UUID.fromString(request.getUuid()), request.getUsername(), responseObserver);
    }

    @Override
    public void unsubscribe(UnsubscriptionRequest request, StreamObserver<Message> responseObserver) {
        chatSessionService.unsubscribe(UUID.fromString(request.getUuid()), responseObserver);
    }

    @Override
    public void sendMessage(MessageRequest request, StreamObserver<Message> responseObserver) {
        chatSessionService.sendMessage(UUID.fromString(request.getUuid()), request.getUsername(), request.getMessage(), responseObserver);
    }
}
