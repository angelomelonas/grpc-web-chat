package com.angelomelonas.grpcwebchat.integration;

import com.angelomelonas.grpcwebchat.Chat.AuthenticationResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ChatServiceTest {

    @Autowired
    private ChatServiceTestClient chatClient;

    @Test
    public void authenticateTest() {
        AuthenticationResponse authenticationResponse = chatClient.authenticate();
        assertNotNull(UUID.fromString(authenticationResponse.getUuid()));
    }

    @Test
    public void subscribeTest() {

    }

    @Test
    public void unsubscribeTest() {

    }

    @Test
    public void sendMessageTest() {

    }
}
