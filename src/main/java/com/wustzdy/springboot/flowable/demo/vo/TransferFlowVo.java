package com.wustzdy.springboot.flowable.demo.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author xielianjun
 * @date 2019-05-31 14:17
 */
@Data
public class TransferFlowVo {
    /**
     * 任务id
     */
    @NotBlank(message = "任务ID不能空")
    private String taskId;
    /**
     * 备注
     */
    @NotBlank(message = "备注不能空")
    private String comment;
    /**
     * 转发人员
     */
    @NotBlank(message = "转发人员不能空")
    private String assignee;

    private String userName;

}
