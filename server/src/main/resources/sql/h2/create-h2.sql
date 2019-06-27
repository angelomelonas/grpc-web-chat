DROP TABLE IF EXISTS User;
CREATE TABLE User
(
    sessionId   BINARY(16) NOT NULL,
    username    CHAR(64) NOT NULL DEFAULT '',
    PRIMARY KEY (sessionId)
);

DROP TABLE IF EXISTS Message;
CREATE TABLE Message
(
    id          BIGINT  NOT NULL AUTO_INCREMENT,
    sessionId   BINARY(16) NOT NULL,
    message     CHAR(64) NOT NULL DEFAULT '',
    timestamp   TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (sessionId) REFERENCES User(sessionId)
);
