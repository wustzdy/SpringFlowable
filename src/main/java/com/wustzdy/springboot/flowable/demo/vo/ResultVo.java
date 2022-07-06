package com.wustzdy.springboot.flowable.demo.vo;

import lombok.Data;

import java.util.List;

@Data
public class ResultVo {
    /**
     * 业务id
     */
    private String businessKey;
    /**
     * 流程定义key
     */
    private String processDefinitionKey;
    /**
     * 流程定义id
     */
    private String processDefinitionId;
    /**
     * 流程实例id
     */
    private String processInstanceId;
    /**
     * 任务id
     */
    private String taskId;

    /**
     * 指派签收人
     */
    private String assignee;
    /**
     * 状态
     */
    private String jobStat;
    /**
     * activityId
     */
    private String activityId;
    /**
     * 下一环节任务
     */
    private List<String> nextActivityIds;

    private String nextTaskId;

    private String commentId;
}
