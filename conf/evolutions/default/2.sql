# ---!Ups

ALTER TABLE  `events` CHANGE  `date`  `date` DATETIME NULL DEFAULT NULL

#---!Downs

ALTER TABLE  `events` CHANGE  `date`  `date` DATE NULL DEFAULT NULL