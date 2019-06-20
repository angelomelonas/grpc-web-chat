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

import java.util.ArrayList;
import java.util.List;
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

        UnsubscriptionRequest unsubscriptionRequest = UnsubscriptionRequest.newBuilder()
                .setUuid(String.valueOf(uuid))
                .build();

        chatServiceTestClient.unsubscribe(unsubscriptionRequest);
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

        UnsubscriptionRequest unsubscriptionRequest = UnsubscriptionRequest.newBuilder()
                .setUuid(String.valueOf(uuid))
                .build();

        chatServiceTestClient.unsubscribe(unsubscriptionRequest);
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

        UnsubscriptionRequest unsubscriptionRequest = UnsubscriptionRequest.newBuilder()
                .setUuid(String.valueOf(uuid))
                .build();

        chatServiceTestClient.unsubscribe(unsubscriptionRequest);
    }

    @Test
    public void multiClientTest() throws InterruptedException {
        String[] messagesToSendClient1 = new String[]{"hello user2", "how is the weather", "good cheers"};
        String[] messagesToSendClient2 = new String[]{"hi user1", "weather is great", "good cheers"};

        int totalMessageCount = messagesToSendClient1.length + messagesToSendClient2.length;

        SimulatedClient client1 = new SimulatedClient(messagesToSendClient1, totalMessageCount);
        SimulatedClient client2 = new SimulatedClient(messagesToSendClient2, totalMessageCount);

        client1.start();
        client2.start();

        client1.join();
        client2.join();

        Assert.assertEquals(totalMessageCount, client1.getReceivedMessages().size());
        Assert.assertEquals(totalMessageCount, client2.getReceivedMessages().size());
    }

    private class SimulatedClient extends Thread {
        private String[] messagesToSend;
        private final int totalMessageCount;
        private final StreamObserverTestHelper<Message> streamObserver;
        private List<Message> receivedMessages;

        SimulatedClient(String[] messagesToSend, int totalMessageCount) {
            this.messagesToSend = messagesToSend;
            this.totalMessageCount = totalMessageCount;
            this.streamObserver = new StreamObserverTestHelper<>();
            this.receivedMessages = new ArrayList<>();
        }

        @Override
        public void run() {
            AuthenticationResponse authenticationResponse = chatServiceTestClient.authenticate(AuthenticationRequest.newBuilder().build());

            UUID uuid = UUID.fromString(authenticationResponse.getUuid());
            String username = "TestUsername" + randomSuffixGenerator();

            SubscriptionRequest subscriptionRequest = SubscriptionRequest.newBuilder()
                    .setUuid(String.valueOf(uuid))
                    .setUsername(username)
                    .build();

            chatServiceTestClient.subscribe(subscriptionRequest, this.streamObserver);
            // Wait for the server to acknowledge the subscription.
            this.streamObserver.waitForOnNext();

            // Send all the messages.
            for (String message : messagesToSend) {
                MessageRequest messageRequest = MessageRequest.newBuilder()
                        .setUuid(String.valueOf(uuid))
                        .setUsername(username)
                        .setMessage(message)
                        .build();

                chatServiceTestClient.sendMessage(messageRequest);
            }

            // Wait for all messages to be received.
            for (int i = 0; i < totalMessageCount; i++) {
                this.receivedMessages.add(this.streamObserver.waitForOnNext().get());
            }

            UnsubscriptionRequest unsubscriptionRequest = UnsubscriptionRequest.newBuilder()
                    .setUuid(String.valueOf(uuid))
                    .build();

            chatServiceTestClient.unsubscribe(unsubscriptionRequest);
        }

        public List<Message> getReceivedMessages() {
            return this.receivedMessages;
        }
    }

    private String randomSuffixGenerator() {
        return " " + (new Random().nextInt(10000));
    }
}
