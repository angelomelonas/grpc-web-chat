package com.angelomelonas.grpcwebchat.integration;

import com.angelomelonas.grpcwebchat.Chat.AuthenticationRequest;
import com.angelomelonas.grpcwebchat.Chat.AuthenticationResponse;
import com.angelomelonas.grpcwebchat.Chat.Message;
import com.angelomelonas.grpcwebchat.Chat.MessageRequest;
import com.angelomelonas.grpcwebchat.Chat.SubscriptionRequest;
import com.angelomelonas.grpcwebchat.Chat.UnsubscriptionRequest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Random;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ChatServiceTest {

    private ChatServiceTestClient chatServiceTestClient;

    @Before
    public void resetChatClient() {
        chatServiceTestClient = new ChatServiceTestClient();
    }

    @After
    public void cleanUp() throws InterruptedException {
        chatServiceTestClient.shutdown();
    }

    @Test
    public void authenticateTest() {
        AuthenticationResponse authenticationResponse = chatServiceTestClient.authenticate(AuthenticationRequest.newBuilder().build());

        Assert.assertNotNull(authenticationResponse.getUuid());
    }

    @Test
    public void subscribeTest() {
        AuthenticationResponse authenticationResponse = chatServiceTestClient.authenticate(AuthenticationRequest.newBuilder().build());

        UUID uuid = UUID.fromString(authenticationResponse.getUuid());
        String username = "TestUsername" + randomSuffixGenerator();

        SubscriptionRequest subscriptionRequest = SubscriptionRequest.newBuilder()
                .setUuid(String.valueOf(uuid))
                .setUsername(username)
                .build();

        StreamObserverTestHelper<Message> streamObserver = new StreamObserverTestHelper();

        chatServiceTestClient.subscribe(subscriptionRequest, streamObserver);
        // Wait for the server to acknowledge the subscription.
        Message messageResponse = streamObserver.waitForOnNext().get();

        Assert.assertEquals("Server", messageResponse.getUsername());
        Assert.assertEquals("Client subscribed successfully.", messageResponse.getMessage());
    }

    @Test
    public void unsubscribeTest() {
        AuthenticationResponse authenticationResponse = chatServiceTestClient.authenticate(AuthenticationRequest.newBuilder().build());

        UUID uuid = UUID.fromString(authenticationResponse.getUuid());
        String username = "TestUsername" + randomSuffixGenerator();

        SubscriptionRequest subscriptionRequest = SubscriptionRequest.newBuilder()
                .setUuid(String.valueOf(uuid))
                .setUsername(username)
                .build();

        StreamObserverTestHelper<Message> streamObserver = new StreamObserverTestHelper();

        chatServiceTestClient.subscribe(subscriptionRequest, streamObserver);
        // Wait for the server to acknowledge the subscription.
        streamObserver.waitForOnNext();

        UnsubscriptionRequest unsubscriptionRequest = UnsubscriptionRequest.newBuilder()
                .setUuid(String.valueOf(uuid))
                .build();

        Message unsubscribeMessage = chatServiceTestClient.unsubscribe(unsubscriptionRequest);

        Assert.assertEquals("You have successfully been unsubscribed", unsubscribeMessage.getMessage());
        Assert.assertEquals("Server", unsubscribeMessage.getUsername());
    }

    @Test
    public void sendMessageSimpleTest() {
        AuthenticationResponse authenticationResponse = chatServiceTestClient.authenticate(AuthenticationRequest.newBuilder().build());

        UUID uuid = UUID.fromString(authenticationResponse.getUuid());
        String username = "TestUsername" + randomSuffixGenerator();
        String message = "Test Message " + randomSuffixGenerator();

        SubscriptionRequest subscriptionRequest = SubscriptionRequest.newBuilder()
                .setUuid(String.valueOf(uuid))
                .setUsername(username)
                .build();

        StreamObserverTestHelper<Message> streamObserver = new StreamObserverTestHelper();

        chatServiceTestClient.subscribe(subscriptionRequest, streamObserver);
        // Wait for the server to acknowledge the subscription.
        streamObserver.waitForOnNext();

        MessageRequest messageRequest = MessageRequest.newBuilder()
                .setUuid(String.valueOf(uuid))
                .setUsername(username)
                .setMessage(message)
                .build();

        // Send the message.
        Message messageResponse = chatServiceTestClient.sendMessage(messageRequest);

        Assert.assertEquals("Message sent successfully.", messageResponse.getMessage());
        Assert.assertEquals("Server", messageResponse.getUsername());
    }

    @Test
    public void sendMessageAdvancedTest() {
        AuthenticationResponse authenticationResponse = chatServiceTestClient.authenticate(AuthenticationRequest.newBuilder().build());

        UUID uuid = UUID.fromString(authenticationResponse.getUuid());
        String username = "TestUsername" + randomSuffixGenerator();
        String message = "Test Message " + randomSuffixGenerator();

        SubscriptionRequest subscriptionRequest = SubscriptionRequest.newBuilder()
                .setUuid(String.valueOf(uuid))
                .setUsername(username)
                .build();

        StreamObserverTestHelper<Message> streamObserver = new StreamObserverTestHelper();

        chatServiceTestClient.subscribe(subscriptionRequest, streamObserver);
        // Wait for the server to acknowledge the subscription.
        streamObserver.waitForOnNext();

        MessageRequest messageRequest = MessageRequest.newBuilder()
                .setUuid(String.valueOf(uuid))
                .setUsername(username)
                .setMessage(message)
                .build();

        // Send the message.
        chatServiceTestClient.sendMessage(messageRequest);

        // Wait for the sent message.
        Message messageResponse = streamObserver.waitForOnNext().get();

        Assert.assertEquals(message, messageResponse.getMessage());
        Assert.assertEquals(username, messageResponse.getUsername());
    }

    private String randomSuffixGenerator() {
        return " " + (new Random().nextInt(10000));
    }
}
