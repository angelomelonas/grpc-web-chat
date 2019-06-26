INSERT INTO User(sessionId, username) VALUES (RANDOM_UUID(), 'Johnny69');
INSERT INTO User(sessionId, username) VALUES (RANDOM_UUID(), 'William53');

INSERT INTO Message(sessionId, message, timestamp)
VALUES(SELECT sessionId FROM User WHERE username = 'Johnny69', 'Hi there, how are you?', NOW());

INSERT INTO Message(sessionId, message, timestamp)
VALUES(SELECT sessionId FROM User WHERE username = 'William53', 'Great thanks, and you?', NOW());
