package com.angelomelonas.grpcwebchat;

import com.angelomelonas.grpcwebchat.Chat.AuthenticationResponse;
import com.angelomelonas.grpcwebchat.Chat.Message;
import com.angelomelonas.grpcwebchat.chat.ChatSessionService;
import io.grpc.stub.StreamObserver;
import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
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

        chatSessionService.create(uuid, streamObserver);

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

        chatSessionService.create(uuid, streamObserver);
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

        chatSessionService.create(uuid, streamObserver);
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

        chatSessionService.create(uuid, streamObserver);
        chatSessionService.subscribe(uuid, "RandomTestUsername123", streamObserver);
        chatSessionService.sendMessage(uuid, "RandomTestUsername123", "Random Test Message 123", streamObserver);

        assertEquals(expectedMessageResponse.getMessage(), ((Message) streamObserver.response).getMessage());
        assertEquals(expectedMessageResponse.getUsername(), ((Message) streamObserver.response).getUsername());
        assertTrue(streamObserver.onCompletedCalled);
    }

    @Test
    public void subscribeNotAuthenticatedExceptionTest() {
        final ChatSessionService chatSessionService = new ChatSessionService();
        final MockStreamObserver streamObserver = new MockStreamObserver();
        final UUID uuid = UUID.randomUUID();

        Class<IllegalArgumentException> expectedExceptionClass = IllegalArgumentException.class;
        String expectedExceptionMessage = "Cannot subscribe. Client not authenticated.";

        assertThatExceptionOfType(expectedExceptionClass)
                .isThrownBy(() -> chatSessionService.subscribe(uuid, "RandomTestUsername123", streamObserver))
                .withMessage(expectedExceptionMessage);

        assertEquals(expectedExceptionClass, streamObserver.error.getClass());
        assertEquals(expectedExceptionMessage, streamObserver.error.getMessage());
    }

    @Test
    public void unsubscribeNotSubscribedExceptionTest() {
        final ChatSessionService chatSessionService = new ChatSessionService();
        final MockStreamObserver streamObserver = new MockStreamObserver();
        final UUID uuid = UUID.randomUUID();

        Class<IllegalArgumentException> expectedExceptionClass = IllegalArgumentException.class;
        String expectedExceptionMessage = "Cannot unsubscribe. Client not subscribed.";

        assertThatExceptionOfType(expectedExceptionClass)
                .isThrownBy(() -> chatSessionService.unsubscribe(uuid, streamObserver))
                .withMessage(expectedExceptionMessage);

        assertEquals(expectedExceptionClass, streamObserver.error.getClass());
        assertEquals(expectedExceptionMessage, streamObserver.error.getMessage());
    }

    @Test
    public void sendMessageNoSubscriptionExceptionTest() {
        final ChatSessionService chatSessionService = new ChatSessionService();
        final MockStreamObserver streamObserver = new MockStreamObserver();
        final UUID uuid = UUID.randomUUID();

        Class<IllegalArgumentException> expectedExceptionClass = IllegalArgumentException.class;
        String expectedExceptionMessage = "Cannot send message. ChatSession does not exist.";

        assertThatExceptionOfType(expectedExceptionClass)
                .isThrownBy(() -> chatSessionService.sendMessage(uuid, "RandomTestUsername123", "Random Test Message 123", streamObserver))
                .withMessage(expectedExceptionMessage);

        assertEquals(expectedExceptionClass, streamObserver.error.getClass());
        assertEquals(expectedExceptionMessage, streamObserver.error.getMessage());
    }

    private class MockStreamObserver implements StreamObserver {
        private Object response = null;
        private Throwable error = null;
        private boolean onCompletedCalled = false;

        @Override
        public void onNext(Object value) {
            this.response = value;
        }

        @Override
        public void onError(Throwable t) {
            this.error = t;
        }

        @Override
        public void onCompleted() {
            onCompletedCalled = true;
        }
    }
}
