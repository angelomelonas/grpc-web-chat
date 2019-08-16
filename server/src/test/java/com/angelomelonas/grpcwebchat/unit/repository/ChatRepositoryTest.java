package com.angelomelonas.grpcwebchat.unit.repository;

import com.angelomelonas.grpcwebchat.ChatApplication;
import com.angelomelonas.grpcwebchat.repository.ChatRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(
        properties = "spring.profiles.active=test",
        classes = ChatApplication.class
)
@ActiveProfiles("test")
public class ChatRepositoryTest {

    @Autowired
    private ChatRepository chatRepository;

    @Test
    public void addUserTest() {
        UUID sessionId = UUID.randomUUID();
        String username = "RandomTestUsername123";

        chatRepository.addUser(sessionId, username);

        Assert.assertTrue(chatRepository.doesSessionExist(sessionId));
        Assert.assertTrue(chatRepository.doesUserExist(username));
    }

    @Test
    public void addMessageTest() {
        UUID sessionId = UUID.randomUUID();
        String username = "RandomTestUsername123";
        String message = "Random test message 123";

        chatRepository.addUser(sessionId, username);
        chatRepository.addMessage(sessionId, message, Instant.now());

        Assert.assertEquals(1, chatRepository.messagesBySessionId(sessionId));
    }
}
