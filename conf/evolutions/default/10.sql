# ---!Ups

ALTER TABLE  `events` DROP  `tp`;

#---!Downs

ALTER TABLE  `events` ADD `tp` VARCHAR(20) CHARACTER SET utf8 AFTER 'id';
