package com.wustzdy.springboot.flowable.demo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.collections.MapUtils;

import java.util.Date;
import java.util.Map;


@ApiModel
@Data
public class TaskCommentVo {

    // below come from commentVo

    @ApiModelProperty(value = "批注ID", hidden = true)
    private String commentId;

    @ApiModelProperty(value = "批注人")
    private String commentUser;

    @ApiModelProperty(value = "批注时间")
    private Date commentTime;

    @ApiModelProperty(value = "批注内容")
    private String commentContent;

    @ApiModelProperty(value = "任务名称")
    private String taskName;

    // above come from commentVo

    @ApiModelProperty(value = "任务ID")
    private String taskId;
    @ApiModelProperty(value = "活动ID", hidden = true)
    private String activityId;

    @ApiModelProperty(hidden = true)
    private String businessKey;

    @ApiModelProperty(value = "分配人")
    private String assignee;
    @ApiModelProperty(value = "任务名称")
    private String name;

    @ApiModelProperty(value = "开始时间")
    private Date createTime;
    @ApiModelProperty(value = "完成时间")
    private Date endTime;

    @ApiModelProperty(hidden = true)
    private String deleteReason;

    @ApiModelProperty(hidden = true)
    private String processDefinitionId;

    @ApiModelProperty(hidden = true)
    private String taskDefinitionKey;

    @ApiModelProperty(hidden = true)
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
