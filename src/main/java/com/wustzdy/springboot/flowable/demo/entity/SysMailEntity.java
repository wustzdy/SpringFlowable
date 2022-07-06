package com.wustzdy.springboot.flowable.demo.entity;

import lombok.Data;

import java.util.Date;
import java.util.Map;


@Data
public class SysMailEntity {
    /**
     * 邮件名称
     */
    private String mailName;
    /**
     * 邮件主题
     */
    private String mailSubject;
    /**
     * 邮件内容
     */
    private String mailContent;
    /**
     * 创建时间
     */
    private Date createTime;
    private Integer mailEdit;

    private String to;

    private String cc;

    private String from;

    private Map<String, Object> params;

}
