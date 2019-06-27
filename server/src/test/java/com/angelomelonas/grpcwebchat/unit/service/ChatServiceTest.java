package com.angelomelonas.grpcwebchat.unit.service;

import com.angelomelonas.grpcwebchat.Chat.AuthenticationRequest;
import com.angelomelonas.grpcwebchat.Chat.AuthenticationResponse;
import com.angelomelonas.grpcwebchat.Chat.Message;
import com.angelomelonas.grpcwebchat.Chat.MessageRequest;
import com.angelomelonas.grpcwebchat.Chat.MessageResponse;
import com.angelomelonas.grpcwebchat.Chat.SubscriptionRequest;
import com.angelomelonas.grpcwebchat.Chat.UnsubscriptionRequest;
import com.angelomelonas.grpcwebchat.Chat.UnsubscriptionResponse;
import com.angelomelonas.grpcwebchat.service.ChatService;
import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ChatServiceTest {

    @Test
    public void authenticationTest() {
        final ChatService chatService = new ChatService();
        final MockStreamObserver streamObserver = new MockStreamObserver();
        final AuthenticationRequest authenticationRequest = AuthenticationRequest.newBuilder().build();

        chatService.authenticate(authenticationRequest, streamObserver);

        assertTrue(streamObserver.onCompletedCalled);
    }

    @Test
    public void subscribeTest() {
        final ChatService chatService = new ChatService();
        chatService.setChatRepository(new MockChatRepository());
        final MockStreamObserver streamObserver = new MockStreamObserver();

        final String username = "RandomUsername123";
        final String message = "User " + username + " has subscribed.";

        final AuthenticationRequest authenticationRequest = AuthenticationRequest.newBuilder().build();

        chatService.authenticate(authenticationRequest, streamObserver);

        final AuthenticationResponse authenticationResponse = (AuthenticationResponse) streamObserver.response;

        final SubscriptionRequest subscriptionRequest = SubscriptionRequest.newBuilder()
                .setUuid(authenticationResponse.getUuid())
                .setUsername(username)
                .build();

        chatService.subscribe(subscriptionRequest, streamObserver);

        final Message expectedMessageResponse = Message.newBuilder()
                .setUsername("Server")
                .setMessage(message)
                .build();

        assertEquals(expectedMessageResponse.getMessage(), ((Message) streamObserver.response).getMessage());
        assertEquals(expectedMessageResponse.getUsername(), ((Message) streamObserver.response).getUsername());
        assertTrue(streamObserver.onCompletedCalled);
    }

    @Test
    public void unsubscribeTest() {
        final ChatService chatService = new ChatService();
        chatService.setChatRepository(new MockChatRepository());
        final MockStreamObserver streamObserver = new MockStreamObserver();

        final String username = "RandomUsername123";
        final String message = "User " + username + " has unsubscribed.";

        final AuthenticationRequest authenticationRequest = AuthenticationRequest.newBuilder().build();

        chatService.authenticate(authenticationRequest, streamObserver);

        final AuthenticationResponse authenticationResponse = (AuthenticationResponse) streamObserver.response;

        final SubscriptionRequest subscriptionRequest = SubscriptionRequest.newBuilder()
                .setUuid(authenticationResponse.getUuid())
                .setUsername(username)
                .build();

        chatService.subscribe(subscriptionRequest, streamObserver);

        final UnsubscriptionRequest unsubscriptionRequest = UnsubscriptionRequest.newBuilder()
                .setUuid(authenticationResponse.getUuid())
                .build();

        chatService.unsubscribe(unsubscriptionRequest, streamObserver);

        final UnsubscriptionResponse expectedMessageResponse = UnsubscriptionResponse.newBuilder()
                .setUuid(authenticationResponse.getUuid())
                .setMessage(message)
                .build();

        assertEquals(expectedMessageResponse.getMessage(), ((UnsubscriptionResponse) streamObserver.response).getMessage());
        assertTrue(streamObserver.onCompletedCalled);
    }

    @Test
    public void sendMessageTest() {
        final ChatService chatService = new ChatService();
        chatService.setChatRepository(new MockChatRepository());
        final MockStreamObserver streamObserver = new MockStreamObserver();

        final String username = "Random Username 123";
        final String message = "A random test message.";

        final AuthenticationRequest authenticationRequest = AuthenticationRequest.newBuilder().build();

        chatService.authenticate(authenticationRequest, streamObserver);

        AuthenticationResponse authenticationResponse = (AuthenticationResponse) streamObserver.response;

        final SubscriptionRequest subscriptionRequest = SubscriptionRequest.newBuilder()
                .setUuid(authenticationResponse.getUuid())
                .setUsername(username)
                .build();

        chatService.subscribe(subscriptionRequest, streamObserver);

        final MessageRequest messageRequest = MessageRequest.newBuilder()
                .setUuid(authenticationResponse.getUuid())
                .setUsername(username)
                .setMessage(message)
                .build();

        chatService.sendMessage(messageRequest, streamObserver);

        final MessageResponse expectedMessageResponse = MessageResponse.newBuilder()
                .setMessage("Message sent successfully.")
                .build();

        assertEquals(expectedMessageResponse.getMessage(), ((MessageResponse) streamObserver.response).getMessage());
        assertTrue(streamObserver.onCompletedCalled);
    }

    @Test
    public void subscribeNotAuthenticatedExceptionTest() {
        final ChatService chatService = new ChatService();
        final MockStreamObserver streamObserver = new MockStreamObserver();

        final UUID uuid = UUID.randomUUID();
        final String username = "Random Username 123";

        final Class<IllegalArgumentException> expectedExceptionClass = IllegalArgumentException.class;
        final String expectedExceptionMessage = "Cannot subscribe. Client not authenticated.";

        final SubscriptionRequest subscriptionRequest = SubscriptionRequest.newBuilder()
                .setUuid(String.valueOf(uuid))
                .setUsername(username)
                .build();

        chatService.subscribe(subscriptionRequest, streamObserver);

        assertEquals(expectedExceptionClass, streamObserver.error.getClass());
        assertEquals(expectedExceptionMessage, streamObserver.error.getMessage());
    }

    @Test
    public void unsubscribeNotSubscribedExceptionTest() {
        final ChatService chatService = new ChatService();
        final MockStreamObserver streamObserver = new MockStreamObserver();

        final UUID uuid = UUID.randomUUID();

        final Class<IllegalArgumentException> expectedExceptionClass = IllegalArgumentException.class;
        final String expectedExceptionMessage = "Cannot unsubscribe. Client not subscribed.";

        final UnsubscriptionRequest unsubscriptionRequest = UnsubscriptionRequest.newBuilder()
                .setUuid(String.valueOf(uuid))
                .build();

        chatService.unsubscribe(unsubscriptionRequest, streamObserver);

        assertEquals(expectedExceptionClass, streamObserver.error.getClass());
        assertEquals(expectedExceptionMessage, streamObserver.error.getMessage());
    }

    @Test
    public void sendMessageNoSubscriptionExceptionTest() {
        final ChatService chatService = new ChatService();
        final MockStreamObserver streamObserver = new MockStreamObserver();

        final UUID uuid = UUID.randomUUID();

        Class<IllegalArgumentException> expectedExceptionClass = IllegalArgumentException.class;
        String expectedExceptionMessage = "Cannot send message. ChatSession does not exist.";

        final MessageRequest messageRequest = MessageRequest.newBuilder()
                .setUuid(String.valueOf(uuid))
                .setUsername("RandomTestUsername123")
                .setMessage("Random Test Message 123")
                .build();

        assertThatExceptionOfType(expectedExceptionClass)
                .isThrownBy(() -> chatService.sendMessage(messageRequest, streamObserver))
                .withMessage(expectedExceptionMessage);
    }
}
