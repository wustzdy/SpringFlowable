--liquibase formatted sql

--changeset zhudayang:20220118-01
CREATE TABLE `demo` (
    `id` bigint(20) unsigned  NOT NULL AUTO_INCREMENT,

    `question_type` varchar(32) NOT NULL COMMENT '问题类型',
    `description` varchar(2048) NOT NULL COMMENT '问题描述',
    `contact` varchar(128) NOT NULL COMMENT '联系人',
    `contact_telephone` varchar(32) NOT NULL COMMENT '联系人电话',
    `contact_email` varchar(64) NOT NULL COMMENT '联系人邮箱',
    `corporate_name` varchar(128) NOT NULL COMMENT '公司名称',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '售后服务工单表';
