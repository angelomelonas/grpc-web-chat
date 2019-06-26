INSERT INTO User(username) VALUES ('Johnny69');
INSERT INTO User(username) VALUES ('William53');

INSERT INTO Message(userId, message, timestamp)
VALUES(SELECT id FROM User WHERE username = 'Johnny69', 'Hi there, how are you?', NOW());

INSERT INTO Message(userId, message, timestamp)
VALUES(SELECT id FROM User WHERE username = 'William53', 'Great thanks, and you?', NOW());
