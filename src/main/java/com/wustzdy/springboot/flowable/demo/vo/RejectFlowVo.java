package com.wustzdy.springboot.flowable.demo.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;


@Data
public class RejectFlowVo {

    /**
     * 流程实例ID
     */
    @NotBlank(message = "流程实例ID不能空")
    private String processInstanceId;
    /**
     * 备注
     */
    @NotBlank(message = "备注不能空")
    private String comment;
}
