# ---!Ups

ALTER TABLE  `sales` ADD  `price` INT UNSIGNED NOT NULL

#---!Downs

ALTER TABLE `sales` DROP `price`