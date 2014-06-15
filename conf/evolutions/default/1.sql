# ---!Ups

CREATE TABLE events (
id BIGINT NOT NULL AUTO_INCREMENT, 
tp VARCHAR(20),
name VARCHAR(100),
date DATE,
dscr VARCHAR(255),
PRIMARY KEY (id)
);

#---!Downs

DROP TABLE IF EXISTS events