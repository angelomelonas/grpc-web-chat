package com.angelomelonas.grpcwebchat.unit;

import com.angelomelonas.grpcwebchat.Chat.Message;
import com.angelomelonas.grpcwebchat.chat.ChatSession;
import org.junit.Test;

import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ChatSessionTest {

    @Test
    public void sendMessageTest() {
        ChatSession chatSession = new ChatSession();
        MockStreamObserver streamObserver = new MockStreamObserver();

        String username = "RandomTestUsername123";
        String message = "Random Test Message 123";
        Message expectedMessageResponse = Message.newBuilder()
                .setUsername(username)
                .setMessage(message)
                .setTimestamp(Instant.now().toEpochMilli())
                .build();

        chatSession.subscribe(username, streamObserver);
        chatSession.sendMessage(message);

        assertEquals(expectedMessageResponse.getMessage(), ((Message) streamObserver.response).getMessage());
        assertEquals(expectedMessageResponse.getUsername(), ((Message) streamObserver.response).getUsername());

        chatSession.unsubscribe();
        assertTrue(streamObserver.onCompletedCalled);
    }

    @Test
    public void unsubscribeNoSubscriptionExceptionTest() {
        ChatSession chatSession = new ChatSession();

        Class<IllegalArgumentException> expectedExceptionClass = IllegalArgumentException.class;
        String expectedExceptionMessage = "Cannot unsubscribe. Session not subscribed.";

        assertThatExceptionOfType(expectedExceptionClass)
                .isThrownBy(() -> chatSession.unsubscribe())
                .withMessage(expectedExceptionMessage);
    }

}
