# ---!Ups

ALTER TABLE  `sales` ADD  `price` INT UNSIGNED NOT NULL;
ALTER TABLE  `sales` ADD  `confirmed` BOOLEAN NOT NULL;

#---!Downs

ALTER TABLE `sales` DROP `price`;
ALTER TABLE `sales` DROP `confirmed;