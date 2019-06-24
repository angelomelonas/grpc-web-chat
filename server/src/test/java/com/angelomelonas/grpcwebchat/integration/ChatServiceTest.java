package com.angelomelonas.grpcwebchat.integration;

import com.angelomelonas.grpcwebchat.Chat.AuthenticationRequest;
import com.angelomelonas.grpcwebchat.Chat.AuthenticationResponse;
import com.angelomelonas.grpcwebchat.Chat.Message;
import com.angelomelonas.grpcwebchat.Chat.MessageRequest;
import com.angelomelonas.grpcwebchat.Chat.MessageResponse;
import com.angelomelonas.grpcwebchat.Chat.SubscriptionRequest;
import com.angelomelonas.grpcwebchat.Chat.UnsubscriptionRequest;
import com.angelomelonas.grpcwebchat.Chat.UnsubscriptionResponse;
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
    public void ChatServiceTestClient() {
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
        Message subscriptionResponse = streamObserver.waitForOnNext().get();

        Assert.assertEquals(String.valueOf(uuid), subscriptionResponse.getUuid());
        Assert.assertEquals("Server", subscriptionResponse.getUsername());
        Assert.assertEquals("Client subscribed successfully.", subscriptionResponse.getMessage());

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

        UnsubscriptionResponse unsubscriptionResponse = chatServiceTestClient.unsubscribe(unsubscriptionRequest);

        Assert.assertEquals(String.valueOf(uuid), unsubscriptionResponse.getUuid());
        Assert.assertEquals("You have successfully been unsubscribed", unsubscriptionResponse.getMessage());

        streamObserver.waitForOnCompleted();
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
        Message subscriptionResponse = streamObserver.waitForOnNext().get();

        Assert.assertEquals(String.valueOf(uuid), subscriptionResponse.getUuid());
        Assert.assertEquals("Server", subscriptionResponse.getUsername());
        Assert.assertEquals("Client subscribed successfully.", subscriptionResponse.getMessage());

        MessageRequest messageRequest = MessageRequest.newBuilder()
                .setUuid(String.valueOf(uuid))
                .setUsername(username)
                .setMessage(message)
                .build();

        // Send the message.
        MessageResponse messageResponse = chatServiceTestClient.sendMessage(messageRequest);

        Assert.assertEquals(String.valueOf(uuid), messageResponse.getUuid());
        Assert.assertEquals("Message sent successfully.", messageResponse.getMessage());

        UnsubscriptionRequest unsubscriptionRequest = UnsubscriptionRequest.newBuilder()
                .setUuid(String.valueOf(uuid))
                .build();

        chatServiceTestClient.unsubscribe(unsubscriptionRequest);

        streamObserver.waitForOnCompleted();
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
        Message subscriptionResponse = streamObserver.waitForOnNext().get();

        Assert.assertEquals(String.valueOf(uuid), subscriptionResponse.getUuid());
        Assert.assertEquals("Server", subscriptionResponse.getUsername());
        Assert.assertEquals("Client subscribed successfully.", subscriptionResponse.getMessage());

        MessageRequest messageRequest = MessageRequest.newBuilder()
                .setUuid(String.valueOf(uuid))
                .setUsername(username)
                .setMessage(message)
                .build();

        // Send the message.
        chatServiceTestClient.sendMessage(messageRequest);

        // Wait for the sent message.
        Message receivedMessage = streamObserver.waitForOnNext().get(); // TODO: Sometimes receive Client Subscribed Successfully.

        Assert.assertEquals(String.valueOf(uuid), receivedMessage.getUuid());
        Assert.assertEquals(message, receivedMessage.getMessage());
        Assert.assertEquals(username, receivedMessage.getUsername());

        UnsubscriptionRequest unsubscriptionRequest = UnsubscriptionRequest.newBuilder()
                .setUuid(String.valueOf(uuid))
                .build();

        chatServiceTestClient.unsubscribe(unsubscriptionRequest);

        streamObserver.waitForOnCompleted();
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
                this.receivedMessages.add(this.streamObserver.waitForOnNextN().get());
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
