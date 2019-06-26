DROP TABLE IF EXISTS User;
CREATE TABLE User
(
    id          BIGINT NOT NULL AUTO_INCREMENT,
    username    CHAR(64) NOT NULL DEFAULT '',
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS Message;
CREATE TABLE Message
(
    id          BIGINT  NOT NULL AUTO_INCREMENT,
    userId      BIGINT  NOT NULL,
    message     CHAR(64) NOT NULL DEFAULT '',
    timestamp   DATETIME NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (userId) REFERENCES User(id)
);
