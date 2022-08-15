CREATE DATABASE `garble` DEFAULT CHARACTER SET utf8mb4;

USE `garble`;
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) DEFAULT NULL,
  `ext` varchar(50) DEFAULT NULL,
  `update_record` int(4) DEFAULT 0,
  PRIMARY KEY (`Id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4;

insert into user(id, name, ext) values('1','张老大','aaa');
insert into user(id, name, ext) values('2','张老二','bbb');
insert into user(id, name, ext) values('3','张老三','ccc');
insert into user(id, name, ext) values('4','张老四','ddd');
insert into user(id, name, ext) values('5','张老五','eee');
insert into user(id, name, ext) values('6','张老六','fff');
