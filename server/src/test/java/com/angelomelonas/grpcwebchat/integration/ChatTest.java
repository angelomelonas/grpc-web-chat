package com.angelomelonas.grpcwebchat.integration;

import com.angelomelonas.grpcwebchat.Chat.AuthenticationRequest;
import com.angelomelonas.grpcwebchat.Chat.AuthenticationResponse;
import com.angelomelonas.grpcwebchat.Chat.Message;
import com.angelomelonas.grpcwebchat.Chat.MessageRequest;
import com.angelomelonas.grpcwebchat.Chat.MessageResponse;
import com.angelomelonas.grpcwebchat.Chat.SubscribedUsers;
import com.angelomelonas.grpcwebchat.Chat.SubscribedUsersRequest;
import com.angelomelonas.grpcwebchat.Chat.SubscriptionRequest;
import com.angelomelonas.grpcwebchat.Chat.UnsubscriptionRequest;
import com.angelomelonas.grpcwebchat.Chat.UnsubscriptionResponse;
import com.angelomelonas.grpcwebchat.ChatApplication;
import com.angelomelonas.grpcwebchat.service.ChatService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(
    properties = "spring.profiles.active=test",
    classes = ChatApplication.class
)
@ActiveProfiles("test")
public class ChatTest {

    private ChatTestClient chatServiceTestClient;

    @Before
    public void ChatServiceTestClient() {
        chatServiceTestClient = new ChatTestClient();
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

        // Subscribe to messages.
        SubscriptionRequest subscriptionRequest = SubscriptionRequest.newBuilder()
            .setUuid(String.valueOf(uuid))
            .setUsername(username)
            .build();

        StreamObserverTestHelper<Message> messageStreamObserver = new StreamObserverTestHelper();
        chatServiceTestClient.subscribe(subscriptionRequest, messageStreamObserver);

        // Wait for the server to acknowledge the subscription.
        Message subscriptionResponse = messageStreamObserver.waitForOnNext().get();

        // Subscribe to the user list.
        final SubscribedUsersRequest subscribedUsersRequest = SubscribedUsersRequest.newBuilder()
            .setUuid(authenticationResponse.getUuid())
            .build();

        StreamObserverTestHelper<SubscribedUsers> subscribedUsersStreamObserver = new StreamObserverTestHelper();
        chatServiceTestClient.subscribedUserList(subscribedUsersRequest, subscribedUsersStreamObserver);

        // Wait for the server to acknowledge the subscription.
        subscribedUsersStreamObserver.waitForOnNext().get();

        Assert.assertEquals(String.valueOf(uuid), subscriptionResponse.getUuid());
        Assert.assertEquals(ChatService.SERVER_NAME, subscriptionResponse.getUsername());
        Assert.assertEquals("Welcome to gRPC Web Chat, " + username, subscriptionResponse.getMessage());

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

        // Subscribe to the user list.
        final SubscribedUsersRequest subscribedUsersRequest = SubscribedUsersRequest.newBuilder()
            .setUuid(authenticationResponse.getUuid())
            .build();

        StreamObserverTestHelper<SubscribedUsers> subscribedUsersStreamObserver = new StreamObserverTestHelper();
        chatServiceTestClient.subscribedUserList(subscribedUsersRequest, subscribedUsersStreamObserver);

        // Wait for the server to acknowledge the subscription.
        subscribedUsersStreamObserver.waitForOnNext().get();

        UnsubscriptionRequest unsubscriptionRequest = UnsubscriptionRequest.newBuilder()
            .setUuid(String.valueOf(uuid))
            .build();

        UnsubscriptionResponse unsubscriptionResponse = chatServiceTestClient.unsubscribe(unsubscriptionRequest);

        Assert.assertEquals(String.valueOf(uuid), unsubscriptionResponse.getUuid());
        Assert.assertEquals("User " + username + " has unsubscribed.", unsubscriptionResponse.getMessage());

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
        streamObserver.waitForOnNext().get();

        // Subscribe to the user list.
        final SubscribedUsersRequest subscribedUsersRequest = SubscribedUsersRequest.newBuilder()
            .setUuid(authenticationResponse.getUuid())
            .build();

        StreamObserverTestHelper<SubscribedUsers> subscribedUsersStreamObserver = new StreamObserverTestHelper();
        chatServiceTestClient.subscribedUserList(subscribedUsersRequest, subscribedUsersStreamObserver);

        // Wait for the server to acknowledge the subscription.
        subscribedUsersStreamObserver.waitForOnNext().get();

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
        streamObserver.waitForOnNext().get();

        // Subscribe to the user list.
        final SubscribedUsersRequest subscribedUsersRequest = SubscribedUsersRequest.newBuilder()
            .setUuid(authenticationResponse.getUuid())
            .build();

        StreamObserverTestHelper<SubscribedUsers> subscribedUsersStreamObserver = new StreamObserverTestHelper();
        chatServiceTestClient.subscribedUserList(subscribedUsersRequest, subscribedUsersStreamObserver);

        // Wait for the server to acknowledge the subscription.
        subscribedUsersStreamObserver.waitForOnNext().get();

        MessageRequest messageRequest = MessageRequest.newBuilder()
            .setUuid(String.valueOf(uuid))
            .setUsername(username)
            .setMessage(message)
            .build();

        // Send the message.
        chatServiceTestClient.sendMessage(messageRequest);

        // Wait for the sent message.
        Message receivedMessage = streamObserver.waitForOnNext().get();

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
        // TODO: This test sometimes fails. Investigate.

        String[] messagesToSendClient1 = new String[]{"Hello", "How are you?", "Great, thanks"};
        String[] messagesToSendClient2 = new String[]{"Hi", "Good thanks, and you?", "Cool"};
        String[] messagesToSendClient3 = new String[]{"Yo", "Alright", "Yeah"};

        int totalMessageCount = messagesToSendClient1.length + messagesToSendClient2.length + messagesToSendClient3.length;

        SimulatedClient client1 = new SimulatedClient(messagesToSendClient1, totalMessageCount);
        SimulatedClient client2 = new SimulatedClient(messagesToSendClient2, totalMessageCount);
        SimulatedClient client3 = new SimulatedClient(messagesToSendClient3, totalMessageCount);

        client1.start();
        client2.start();
        client3.start();

        client1.join();
        client2.join();
        client3.join();

        Assert.assertEquals(totalMessageCount, client1.getReceivedMessages().size());
        Assert.assertEquals(totalMessageCount, client2.getReceivedMessages().size());
        Assert.assertEquals(totalMessageCount, client3.getReceivedMessages().size());
    }

    private class SimulatedClient extends Thread {
        private String[] messagesToSend;
        private final int totalMessageCount;
        private final StreamObserverTestHelper<Message> messageStreamObserver;
        private final StreamObserverTestHelper<SubscribedUsers> subscribedUsersStreamObserver;
        private List<Message> receivedMessages;

        SimulatedClient(String[] messagesToSend, int totalMessageCount) {
            this.messagesToSend = messagesToSend;
            this.totalMessageCount = totalMessageCount;
            this.messageStreamObserver = new StreamObserverTestHelper<>();
            this.subscribedUsersStreamObserver = new StreamObserverTestHelper<>();
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

            chatServiceTestClient.subscribe(subscriptionRequest, this.messageStreamObserver);

            // Wait for the server to acknowledge the subscription.
            this.messageStreamObserver.waitForOnNext();

            // Subscribe to the user list.
            final SubscribedUsersRequest subscribedUsersRequest = SubscribedUsersRequest.newBuilder()
                .setUuid(authenticationResponse.getUuid())
                .build();

            chatServiceTestClient.subscribedUserList(subscribedUsersRequest, this.subscribedUsersStreamObserver);

            // Wait for the server to acknowledge the subscription.
            this.subscribedUsersStreamObserver.waitForOnNext().get();

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
                this.receivedMessages.add(this.messageStreamObserver.waitForOnNextN().get());
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

    public static String randomSuffixGenerator() {
        return " " + (new Random().nextInt(1000000));
    }
}
