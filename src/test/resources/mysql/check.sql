DROP DATABASE IF EXISTS `garble`;
DROP DATABASE IF EXISTS `garble_else`;
CREATE DATABASE `garble` DEFAULT CHARACTER SET utf8mb4;
CREATE DATABASE `garble_else` DEFAULT CHARACTER SET utf8mb4;
USE `garble`;

DROP TABLE IF EXISTS `garble_company`;
DROP TABLE IF EXISTS `garble_employee`;
DROP TABLE IF EXISTS `garble_task`;

CREATE TABLE `garble_company`
(
    `id`            int(11) NOT NULL AUTO_INCREMENT,
    `c_name`        varchar(50) DEFAULT NULL,
    c_msg           varchar(50) DEFAULT NULL,
    c_code          int(11)     DEFAULT NULL,
    `update_record` int(4)      DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4;

insert into garble_company(id, c_name, c_msg, c_code)
values (1, '暴雪', 'bx', 123);
insert into garble_company(id, c_name, c_msg, c_code)
values (2, '腾讯', 'tx', 234);
insert into garble_company(id, c_name, c_msg, c_code)
values (3, '网易', 'wy', 345);
insert into garble_company(id, c_name, c_msg, c_code)
values (4, '盛大', 'sd', 456);
insert into garble_company(id, c_name, c_msg, c_code)
values (5, '育碧', 'yb', 567);
insert into garble_company(id, c_name, c_msg, c_code)
values (6, '世纪天成', 'sjtc', 678);

CREATE TABLE `garble_employee`
(
    `id`            int(11) NOT NULL AUTO_INCREMENT,
    `c_id`          int(11) NOT NULL,
    `e_name`        varchar(50) DEFAULT NULL,
    `e_msg`         varchar(50) DEFAULT NULL,
    `update_record` int(4)      DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4;


insert into garble_employee(id, e_name, e_msg, c_id)
values (11, '赵包', 'zb', 1);
insert into garble_employee(id, e_name, e_msg, c_id)
values (22, '钱包', 'qb', 1);
insert into garble_employee(id, e_name, e_msg, c_id)
values (33, '孙包', 'sb', 1);
insert into garble_employee(id, e_name, e_msg, c_id)
values (44, '李包', 'lb', 1);
insert into garble_employee(id, e_name, e_msg, c_id)
values (55, '周疼', 'zt', 2);
insert into garble_employee(id, e_name, e_msg, c_id)
values (66, '吴疼', 'wt', 2);
insert into garble_employee(id, e_name, e_msg, c_id)
values (77, '郑疼', 'zt', 2);
insert into garble_employee(id, e_name, e_msg, c_id)
values (88, '王网', 'ww', 3);
insert into garble_employee(id, e_name, e_msg, c_id)
values (99, '冯网', 'fw', 3);
insert into garble_employee(id, e_name, e_msg, c_id)
values (1010, '程网', 'cw', 3);
insert into garble_employee(id, e_name, e_msg, c_id)
values (1111, '祖盛', 'zs', 4);
insert into garble_employee(id, e_name, e_msg, c_id)
values (1212, '蒋育', 'jy', 5);
insert into garble_employee(id, e_name, e_msg, c_id)
values (1313, '沈育', 'sy', 5);
insert into garble_employee(id, e_name, e_msg, c_id)
values (1414, '韩育', 'hy', 5);
insert into garble_employee(id, e_name, e_msg, c_id)
values (1515, '杨时', 'ys', 6);
insert into garble_employee(id, e_name, e_msg, c_id)
values (1616, '朱时', 'zs', 6);


CREATE TABLE `garble_task`
(
    `id`            int(11) NOT NULL AUTO_INCREMENT,
    `e_id`          int(11) NOT NULL,
    `t_name`        varchar(50) DEFAULT NULL,
    `update_record` int(4)      DEFAULT 0,
    `auth_code_col`        varchar(50) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4;

insert into garble_task(t_name, e_id, auth_code_col)
values ('工作1', 11, '123');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作2', 11, '123');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作3', 11, '123');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作4', 22, '123');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作5', 22, '123');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作6', 44, '123');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作7', 55, '123');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作8', 66, '123');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作9', 77, '123');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作9', 77, '123');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作9', 77, '1234');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作9', 77, '1234');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作10', 88, '1234');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作10', 88, '123');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作10', 88, '123');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作11', 99, '123');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作12', 1010, '123');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作13', 1212, '123');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作14', 1313, '123');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作15', 1414, '123');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作16', 1515, '123');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作17', 1515, '123');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作18', 1515, '123');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作19', 1515, '123');

USE `garble_else`;

DROP TABLE IF EXISTS `garble_task`;

CREATE TABLE `garble_task`
(
    `id`            int(11) NOT NULL AUTO_INCREMENT,
    `e_id`          int(11) NOT NULL,
    `t_name`        varchar(50) DEFAULT NULL,
    `update_record` int(4)      DEFAULT 0,
    `auth_code_col`        varchar(50) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4;

insert into garble_task(t_name, e_id, auth_code_col)
values ('工作1', 110, '1234');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作2', 110, '1234');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作3', 110, '1234');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作4', 220, '1234');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作5', 220, '1234');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作6', 440, '1234');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作7', 550, '123');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作8', 660, '123');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作9', 770, '123');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作9', 770, '123');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作9', 770, '123');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作9', 770, '123');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作10', 880, '123');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作10', 880, '123');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作10', 880, '123');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作11', 990, '123');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作12', 10100, '123');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作13', 12120, '123');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作14', 13130, '123');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作15', 14140, '123');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作16', 15150, '123');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作17', 15150, '123');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作18', 15150, '123');
insert into garble_task(t_name, e_id, auth_code_col)
values ('工作19', 15150, '123');