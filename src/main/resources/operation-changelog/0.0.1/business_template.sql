--liquibase formatted sql

--changeset zhudayang:20220704-01
CREATE TABLE `business_template`
(
    `id`                 bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    `template_name`      varchar(32)   NOT NULL COMMENT '模版名称',
    `template_type`      varchar(2048) NOT NULL COMMENT '模版类型',
    `business_type`      varchar(128) DEFAULT NULL COMMENT '业务类型',
    `business_operation` varchar(32)  DEFAULT NULL COMMENT '联系人电话',
    `create_time`        datetime     DEFAULT NULL COMMENT '创建时间',
    `template_default`   varchar(128) DEFAULT NULL COMMENT '',
    `create_by`          varchar(128) DEFAULT NULL COMMENT '创建者',
    `update_time`        datetime     DEFAULT NULL COMMENT '更新时间',
    `update_by`          varchar(128) DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '流程模版';
