# ---!Ups

ALTER TABLE  `users` ADD  `isNew` BOOLEAN NOT NULL;
ALTER TABLE  `users` ADD  `isActive` BOOLEAN NOT NULL;

#---!Downs

ALTER TABLE  `users` DROP  `isNew`;
ALTER TABLE  `users` DROP  `isActive`;