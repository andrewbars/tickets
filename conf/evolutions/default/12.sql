# ---!Ups

ALTER TABLE  `sales` ADD  `fromBooking` BOOLEAN NOT NULL DEFAULT FALSE ,
ADD  `bookUserID` BIGINT NULL DEFAULT NULL;

#---!Downs

ALTER TABLE  `sales` DROP  `fromBooking`,
DROP  `bookUserID` ;