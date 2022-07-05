--liquibase formatted sql

--changeset zhudayang:20220705-01
CREATE TABLE `business_assignee`
(
    `id`                    bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    `activity_id`           varchar(100) NOT NULL COMMENT '环节ID',
    `assignee`              varchar(100) NOT NULL COMMENT '环节处理人',
    `order_type`            varchar(40)           DEFAULT NULL COMMENT '操作类型',
    `create_time`           datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `process_definition_id` varchar(100) NOT NULL COMMENT '流程部署Id',
    `activity_name`         varchar(40)           DEFAULT NULL,
    `send_mail`             tinyint(4) DEFAULT NULL COMMENT '0-不发送，1-发送',
    `business_template_id`  varchar(100)          DEFAULT NULL COMMENT '流程模版',
    `flow_type`             varchar(40)           DEFAULT NULL COMMENT '业务类型',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '任务管理';

