package com.wustzdy.springboot.flowable.demo.vo;

import lombok.Data;
import org.apache.commons.collections.MapUtils;

import java.util.Date;
import java.util.Map;


@Data
public class TaskVo {

    private String taskId;

    private String activityId;

    private String businessKey;

    private String assignee;

    private String name;

    private Date createTime;

    private Date endTime;

    private String deleteReason;

    private String processDefinitionId;

    private String title;

    /**
     * 流程变量
     */
    private Map<String, Object> processVariables;

    public void setTitle(Map<String, Object> processVariables) {
        if (MapUtils.isNotEmpty(processVariables)) {
            this.title = (String) processVariables.get("title");
        }
    }
}
