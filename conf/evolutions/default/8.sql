# ---!Ups

ALTER TABLE  `users` ADD  `fullName` VARCHAR( 50 ) NOT NULL AFTER  `name`


#---!Downs

ALTER TABLE  `users` DROP  `fullName`;
