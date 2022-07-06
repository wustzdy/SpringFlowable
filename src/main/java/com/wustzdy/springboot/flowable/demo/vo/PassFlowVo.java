package com.wustzdy.springboot.flowable.demo.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;


@Data
public class PassFlowVo {
    /**
     * 任务id
     */
    @NotBlank(message = "任务ID不能空")
    private String taskId;
    /**
     * 下一环节处理人
     */
    private String nextTaskAndStaffId;
    /**
     * 备注
     */
    @NotBlank(message = "备注不能空")
    private String comment;
}
