# ---!Ups

CREATE TABLE users(
id BIGINT AUTO_INCREMENT,
name VARCHAR(30) CHARACTER SET utf8,
password VARCHAR(50),
canEditEvents BOOLEAN,
canEditSales BOOLEAN,
canEditUsers BOOLEAN,
PRIMARY KEY (id)
);

#---!Downs

DROP TABLE IF EXISTS users;