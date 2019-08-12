package com.angelomelonas.grpcwebchat.unit.service;

import com.angelomelonas.grpcwebchat.repository.ChatRepository;

import java.time.Instant;
import java.util.UUID;

public class MockChatRepository extends ChatRepository {

    @Override
    public void addUser(UUID sessionId, String username) {
    }

    @Override
    public void addMessage(UUID sessionId, String message, Instant timestamp) {
    }

    @Override
    public boolean doesSessionExist(UUID sessionId) {
        return false;
    }

    @Override
    public boolean doesUserExist(String username) {
        return false;
    }

    @Override
    public int messagesBySessionId(UUID sessionId) {
        return 0;
    }
}
