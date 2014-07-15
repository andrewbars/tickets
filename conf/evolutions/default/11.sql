# ---!Ups

ALTER TABLE  `events` ADD  `bookingExpTime` INT NOT NULL DEFAULT  '15' AFTER  `date`;

#---!Downs

ALTER TABLE  `events` DROP  `bookingExpTime`;