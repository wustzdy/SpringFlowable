--liquibase formatted sql

--changeset zhudayang:20220118-01
CREATE TABLE `salesorder_after_sale_service_order` (

    `id`                  bigint(20) unsigned NOT NULL COMMENT '分布式主id',
    `tenant_id`           bigint(20) unsigned NOT NULL COMMENT '所属租户id',
    `owned_by_id`         bigint(20) unsigned NOT NULL COMMENT '拥有者account id',
    `created_by_id`       bigint(20) unsigned NOT NULL COMMENT '创建者account id',
    `created_time`        datetime            NOT NULL COMMENT '创建的UTC时间',
    `updated_by_id`       bigint(20) unsigned          DEFAULT NULL COMMENT '最后更新者account id',
    `updated_time`        datetime                     DEFAULT NULL COMMENT '最后更新的UTC时间',
    `version`             bigint(20)          NOT NULL DEFAULT 0 COMMENT '乐观锁版本控制字段',
    `extras`              json                         DEFAULT NULL COMMENT '扩展属性',

    `question_type` varchar(32) NOT NULL COMMENT '问题类型',
    `description` varchar(2048) NOT NULL COMMENT '问题描述',
    `contact` varchar(128) NOT NULL COMMENT '联系人',
    `contact_telephone` varchar(32) NOT NULL COMMENT '联系人电话',
    `contact_email` varchar(64) NOT NULL COMMENT '联系人邮箱',
    `corporate_name` varchar(128) NOT NULL COMMENT '公司名称',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '售后服务工单表';
