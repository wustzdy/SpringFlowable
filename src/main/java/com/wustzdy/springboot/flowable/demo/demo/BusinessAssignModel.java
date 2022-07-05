package com.wustzdy.springboot.flowable.demo.demo;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
@ToString(callSuper = true)
public class BusinessAssignModel {

    private String activityId;

    private String assignee;

    private String orderType;

    private Date createTime;

    private String processDefinitionId;

    private String activityName;

    private int sendMail;

    private String businessTemplateId;

    private String flowType;
}
