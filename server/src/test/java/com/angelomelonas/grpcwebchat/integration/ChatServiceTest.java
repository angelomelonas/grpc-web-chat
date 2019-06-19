package com.angelomelonas.grpcwebchat.integration;

import com.angelomelonas.grpcwebchat.Chat.AuthenticationRequest;
import com.angelomelonas.grpcwebchat.Chat.AuthenticationResponse;
import com.angelomelonas.grpcwebchat.Chat.Message;
import com.angelomelonas.grpcwebchat.Chat.MessageRequest;
import com.angelomelonas.grpcwebchat.Chat.SubscriptionRequest;
import com.angelomelonas.grpcwebchat.Chat.UnsubscriptionRequest;
import io.grpc.stub.StreamObserver;
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

        chatServiceTestClient.subscribe(subscriptionRequest, new StreamObserver<Message>() {
            @Override
            public void onNext(Message message) {
                Assert.assertEquals("Client subscribed successfully.", message.getMessage());
                Assert.assertEquals("Server", message.getUsername());
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        });
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

        chatServiceTestClient.subscribe(subscriptionRequest, new StreamObserver<Message>() {
            @Override
            public void onNext(Message value) {

            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        });

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

        chatServiceTestClient.subscribe(subscriptionRequest, new StreamObserver<Message>() {
            @Override
            public void onNext(Message value) {

            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        });
        MessageRequest messageRequest = MessageRequest.newBuilder()
                .setUuid(String.valueOf(uuid))
                .setUsername(username)
                .setMessage(message)
                .build();

        Message sendMessageMessage = chatServiceTestClient.sendMessage(messageRequest);
        Assert.assertEquals("Message sent successfully.", sendMessageMessage.getMessage());
        Assert.assertEquals("Server", sendMessageMessage.getUsername());
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

        chatServiceTestClient.subscribe(subscriptionRequest, new StreamObserver<Message>() {
            @Override
            public void onNext(Message value) {
                Assert.assertEquals(message, value.getMessage());
                Assert.assertEquals(username, value.getUsername());
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        });

        MessageRequest messageRequest = MessageRequest.newBuilder()
                .setUuid(String.valueOf(uuid))
                .setUsername(username)
                .setMessage(message)
                .build();

        chatServiceTestClient.sendMessage(messageRequest);
    }

    private String randomSuffixGenerator() {
        return " " + (new Random().nextInt(10000));
    }
}
