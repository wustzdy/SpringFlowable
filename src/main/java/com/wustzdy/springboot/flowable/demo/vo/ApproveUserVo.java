package com.wustzdy.springboot.flowable.demo.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@Data
public class ApproveUserVo {

    private String operType;

    @NotNull(message = "业务ID不能为空")
    private String id;

    private String activityId;

    @NotBlank(message = "流程实例ID不能为空")
    private String processDefinitionId;

    private Integer businessTemplateId;

    /**
     * 当前审批用户名称
     */
    private String username;

}
