# ---!Ups

CREATE TABLE bookings(
id BIGINT AUTO_INCREMENT,
eventID BIGINT,
date DATETIME,
expDate DATETIME,
price INTEGER,
confirmed BOOLEAN,
PRIMARY KEY (id)
);

ALTER TABLE  `sits` ADD  `booked` BOOLEAN NOT NULL;
ALTER TABLE  `sits` ADD  `bookingID` BIGINT NULL;

#---!Downs

DROP TABLE IF EXISTS bookings;
ALTER TABLE  `sits` DROP  `booked`;
ALTER TABLE  `sits` DROP  `bookingID`;