# ---!Ups

ALTER TABLE  `sales` ADD  `userID` BIGINT NOT NULL;
ALTER TABLE  `bookings` ADD  `userID` BIGINT NOT NULL;

#---!Downs

ALTER TABLE  `sales` DROP  `userID`;
ALTER TABLE  `bookings` DROP  `userID`;