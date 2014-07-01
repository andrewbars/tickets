# ---!Ups

CREATE TABLE sectors (
id BIGINT NOT NULL AUTO_INCREMENT, 
name VARCHAR(5) CHARACTER SET utf8,
numOfRows INT,
seatsInRow INT,
sitPrice INT,
eventID BIGINT,
PRIMARY KEY (id)
);

CREATE TABLE sits (
id BIGINT NOT NULL AUTO_INCREMENT, 
sectorID BIGINT,
rowNumber INT,
num INT,
sold BOOLEAN,
saleID BIGINT,
PRIMARY KEY (id)
);

CREATE TABLE sales(
id BIGINT AUTO_INCREMENT,
eventID BIGINT,
date DATETIME,
PRIMARY KEY (id)
);

#---!Downs

DROP TABLE IF EXISTS sectors;
DROP TABLE IF EXISTS sits;
DROP TABLE IF EXISTS sales;