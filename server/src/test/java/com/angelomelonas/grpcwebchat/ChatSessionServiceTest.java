package com.angelomelonas.grpcwebchat;

import com.angelomelonas.grpcwebchat.Chat.AuthenticationResponse;
import com.angelomelonas.grpcwebchat.Chat.Message;
import com.angelomelonas.grpcwebchat.chat.ChatSessionService;
import io.grpc.stub.StreamObserver;
import org.junit.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ChatSessionServiceTest {

    @Test
    public void createTest() {
        final ChatSessionService chatSessionService = new ChatSessionService();
        final MockStreamObserver streamObserver = new MockStreamObserver();
        final UUID uuid = UUID.randomUUID();

        final AuthenticationResponse expectedAuthenticationResponse = AuthenticationResponse.newBuilder()
                .setUuid(String.valueOf(uuid))
                .build();

        chatSessionService.create(streamObserver, uuid);

        assertEquals(expectedAuthenticationResponse, streamObserver.response);
        assertTrue(streamObserver.onCompletedCalled);
    }

    @Test
    public void subscribeTest() {
        final ChatSessionService chatSessionService = new ChatSessionService();
        final MockStreamObserver streamObserver = new MockStreamObserver();
        final UUID uuid = UUID.randomUUID();

        final Message expectedMessageResponse = Message.newBuilder()
                .setUsername("Server")
                .setMessage("Client subscribed successfully.")
                .build();

        chatSessionService.create(streamObserver, uuid);
        chatSessionService.subscribe(uuid, "RandomTestUsername123", streamObserver);

        assertEquals(expectedMessageResponse.getMessage(), ((Message) streamObserver.response).getMessage());
        assertEquals(expectedMessageResponse.getUsername(), ((Message) streamObserver.response).getUsername());
        assertTrue(streamObserver.onCompletedCalled);
    }

    @Test
    public void unsubscribeTest() {
        final ChatSessionService chatSessionService = new ChatSessionService();
        final MockStreamObserver streamObserver = new MockStreamObserver();
        final UUID uuid = UUID.randomUUID();

        final Message expectedMessageResponse = Message.newBuilder()
                .setUsername("Server")
                .setMessage("You have successfully been unsubscribed")
                .build();

        chatSessionService.create(streamObserver, uuid);
        chatSessionService.subscribe(uuid, "RandomTestUsername123", streamObserver);
        chatSessionService.unsubscribe(uuid, streamObserver);

        assertEquals(expectedMessageResponse.getMessage(), ((Message) streamObserver.response).getMessage());
        assertEquals(expectedMessageResponse.getUsername(), ((Message) streamObserver.response).getUsername());
        assertTrue(streamObserver.onCompletedCalled);
    }

    @Test
    public void sendMessageTest() {
        final ChatSessionService chatSessionService = new ChatSessionService();
        final MockStreamObserver streamObserver = new MockStreamObserver();
        final UUID uuid = UUID.randomUUID();

        final Message expectedMessageResponse = Message.newBuilder()
                .setUsername("Server")
                .setMessage("Message sent successfully.")
                .build();

        chatSessionService.create(streamObserver, uuid);
        chatSessionService.subscribe(uuid, "RandomTestUsername123", streamObserver);
        chatSessionService.sendMessage(uuid, "RandomTestUsername123", "Random Test Message 123", streamObserver);

        assertEquals(expectedMessageResponse.getMessage(), ((Message) streamObserver.response).getMessage());
        assertEquals(expectedMessageResponse.getUsername(), ((Message) streamObserver.response).getUsername());
        assertTrue(streamObserver.onCompletedCalled);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendMessageNoSubscriptionTest() {
        final ChatSessionService chatSessionService = new ChatSessionService();
        final MockStreamObserver streamObserver = new MockStreamObserver();
        final UUID uuid = UUID.randomUUID();

        chatSessionService.sendMessage(uuid, "RandomTestUsername123", "Random Test Message 123", streamObserver);
    }


    private class MockStreamObserver implements StreamObserver {
        private boolean onCompletedCalled = false;
        private Object response = null;

        @Override
        public void onNext(Object value) {
            this.response = value;
        }

        @Override
        public void onError(Throwable t) {

        }

        @Override
        public void onCompleted() {
            onCompletedCalled = true;
        }
    }
}
