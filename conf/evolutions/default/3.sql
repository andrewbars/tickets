# ---!Ups

CREATE TABLE sectors (
id BIGINT NOT NULL AUTO_INCREMENT, 
name VARCHAR(5) CHARACTER SET utf8,
numOfSeats INT,
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
PRIMARY KEY (id)
);

#---!Downs

DROP TABLE IF EXISTS sectors
DROP TABLE IF EXISTS sits