CREATE TABLE IF NOT EXISTS `user`
(
    `id`              INT UNSIGNED auto_increment,
    `name`            VARCHAR(64)  NOT NULL,
    `password`        VARCHAR(64)  not null,
    `avatar`          VARCHAR(200) NULL,
    `type`            int          not null default 0,
    `max_size`        int          not null default 200,
    `max_file_size`   int          not null default 10,
    `create_time`     timestamp    not null default ' 2020-06-14 22:16:17 ',
    `last_login_time` timestamp    not null default CURRENT_TIMESTAMP ,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

insert into user (id, name,password, avatar, type,max_size,max_file_size, create_time, last_login_time)
values (1, 'demo','c6746e520368ffc81f8066359e7cc445',null,0,200,10,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)