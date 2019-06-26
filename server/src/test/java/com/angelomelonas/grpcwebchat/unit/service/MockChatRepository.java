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
}
