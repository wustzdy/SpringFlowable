package com.wustzdy.springboot.flowable.demo.vo;

import lombok.Data;

import java.util.Date;


@Data
public class CommentVo {

    private String commentUser;

    private Date commentTime;

    private String commentContent;

    private String commentId;

    private String taskId;

    private String taskName;

    private String taskDefinitionKey;
}
