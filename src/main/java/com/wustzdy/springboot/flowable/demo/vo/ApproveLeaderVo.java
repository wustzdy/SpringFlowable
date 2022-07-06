package com.wustzdy.springboot.flowable.demo.vo;

import com.wustzdy.springboot.flowable.demo.entity.BusinessFlowsEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;


@Data
@NoArgsConstructor
public class ApproveLeaderVo {

    @NotNull
    private String operType;

    @NotNull
    private String activityId;

    @NotNull
    private String processDefinitionId;

    @NotNull
    private Integer businessTemplateId;

    @NotNull
    private String currentUser;

    @NotNull
    private String applyUser;

    private String serviceType;

    private String serviceId;

    private String owner;

    public ApproveLeaderVo(BusinessFlowsEntity businessFlowsEntity, String currentUser, String operateType, String activityId) {
        this.setActivityId(activityId);
        this.setApplyUser(businessFlowsEntity.getApplyUser());
        this.setBusinessTemplateId(businessFlowsEntity.getBusinessTemplateId());
        this.setCurrentUser(currentUser);
        this.setOperType(operateType);
        this.setOwner(businessFlowsEntity.getOwner());
        this.setServiceType(businessFlowsEntity.getServiceType());
        this.setServiceId(businessFlowsEntity.getServiceId());
        this.setProcessDefinitionId(businessFlowsEntity.getProcessDefinitionId());
    }

}
