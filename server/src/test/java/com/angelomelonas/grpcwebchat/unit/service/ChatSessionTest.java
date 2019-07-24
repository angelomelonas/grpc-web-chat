package com.angelomelonas.grpcwebchat.unit.service;

import com.angelomelonas.grpcwebchat.Chat.Message;
import com.angelomelonas.grpcwebchat.common.ChatSession;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ChatSessionTest {

    @Test
    public void sendMessageTest() {
        UUID sessionId = UUID.randomUUID();
        ChatSession chatSession = new ChatSession(sessionId);
        MockStreamObserver streamObserver = new MockStreamObserver();

        String username = "RandomTestUsername123";
        String message = "Random Test Message 123";
        long timestamp = Instant.now().toEpochMilli();

        Message expectedMessageResponse = Message.newBuilder()
                .setUsername(username)
                .setMessage(message)
                .setTimestamp(timestamp)
                .build();

        chatSession.subscribe(username, streamObserver);

        Message newMessage = Message.newBuilder()
                .setUuid(String.valueOf(sessionId))
                .setUsername(username)
                .setMessage(message)
                .setTimestamp(timestamp)
                .build();

        chatSession.sendMessage(newMessage);

        assertEquals(expectedMessageResponse.getMessage(), ((Message) streamObserver.response).getMessage());
        assertEquals(expectedMessageResponse.getUsername(), ((Message) streamObserver.response).getUsername());

        chatSession.unsubscribe();
        assertTrue(streamObserver.onCompletedCalled);
    }

    @Test
    public void unsubscribeNoSubscriptionExceptionTest() {
        UUID sessionId = UUID.randomUUID();
        ChatSession chatSession = new ChatSession(sessionId);

        Class<IllegalArgumentException> expectedExceptionClass = IllegalArgumentException.class;
        String expectedExceptionMessage = "Cannot unsubscribe. Session not subscribed.";

        assertThatExceptionOfType(expectedExceptionClass)
                .isThrownBy(() -> chatSession.unsubscribe())
                .withMessage(expectedExceptionMessage);
    }
}
