package com.angelomelonas.grpcwebchat;

import com.angelomelonas.grpcwebchat.ChatOuterClass.Message;
import com.angelomelonas.grpcwebchat.ChatOuterClass.MessageRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ChatTest {

    @Autowired
    private ChatClient chatClient;

    @Test
    public void testSendMessage() {
        MessageRequest messageRequest = generateRandomMessageRequest();

        Message message = chatClient.sendMessage(messageRequest.getUsername(), messageRequest.getMessage());

        assertThat(message.getMessage()).isEqualTo(messageRequest.getMessage());
        assertThat(message.getUsername()).isEqualTo(messageRequest.getUsername());
    }

    private MessageRequest generateRandomMessageRequest() {
        String username = UUID.randomUUID().toString();
        String message = UUID.randomUUID().toString();
        return MessageRequest.newBuilder().setUsername(username).setMessage(message).build();
    }
}
