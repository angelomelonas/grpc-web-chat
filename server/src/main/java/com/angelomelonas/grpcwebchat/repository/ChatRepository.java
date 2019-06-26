package com.angelomelonas.grpcwebchat.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Repository
public class ChatRepository {

    @Autowired
    JdbcTemplate template;

    public void addUser(UUID sessionId, String username) {
        String query = "INSERT INTO User(sessionId, username) VALUES(?,?)";

        template.update(query, preparedStatement -> {
            preparedStatement.setObject(1, sessionId);
            preparedStatement.setString(2, username);
        });
    }

    public void addMessage(UUID sessionId, String message, Instant timestamp) {
        String query = "INSERT INTO Message(sessionId, message, timestamp) VALUES(?,?,?)";

        template.update(query, preparedStatement -> {
            preparedStatement.setObject(1, sessionId);
            preparedStatement.setString(2, message);
            preparedStatement.setTimestamp(3, Timestamp.from(timestamp));
        });
    }
}
