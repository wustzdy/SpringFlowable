package com.wustzdy.springboot.flowable.demo.service;

import com.wustzdy.springboot.flowable.demo.constant.FlowConstants;
import com.wustzdy.springboot.flowable.demo.entity.BusinessAssigneeEntity;
import com.wustzdy.springboot.flowable.demo.vo.ApproveLeaderVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class TaskAssgineeService {

    @Value("${order.plat.approveUser}")
    private String platApproveUser;

    @Autowired
    private BusinessAssigneeService assigneeService;

    public String getApproveLeader(ApproveLeaderVo params) {
        return getApproveLeader(params, true);
    }

    public String getApproveLeader(ApproveLeaderVo params, boolean validSuperLeader) {
        log.info("---ApproveLeaderVo--:{}", params.toString());
        String operType = params.getOperType();
        String activityId = params.getActivityId();
        String processDefinitionId = params.getProcessDefinitionId();
        Integer businessTemplateId = params.getBusinessTemplateId();
        String username = params.getCurrentUser();
        String serviceId = params.getServiceId();
        String serviceType = params.getServiceType();
        String applyUser = params.getApplyUser();
        String owner = params.getOwner();
        String nextUser;
        // 默认直接汇报人为审批人
        if (activityId == null || processDefinitionId == null || operType == null) {
            return platApproveUser;
        }
        try {
            BusinessAssigneeEntity entityList;
            if (businessTemplateId == null || businessTemplateId == 0) {
                // 获取环节配置审批人信息
                entityList = assigneeService.getActivitiAssignees(
                        processDefinitionId,
                        operType,
                        activityId);
            } else {
                // 获取环节配置审批人信息
                entityList = assigneeService.getActivitiAssignees(
                        processDefinitionId,
                        operType,
                        activityId,
                        businessTemplateId);
            }
            log.info("---entityList--:{}", entityList);

            if (entityList == null && operType.equals(FlowConstants.FLOW_OPER_PASS)) {
                return sysUserService.getLeader(username, validSuperLeader);
            }
            if (entityList == null && operType.equals(FlowConstants.FLOW_OPER_TRANSFER)) {
                return platApproveUser;
            }
            if (entityList == null) {
                return platApproveUser;
            }
            String assignee = entityList.getAssignee();
            log.info("---assignee--:{}", assignee);
            switch (assignee) {
                case FlowConstants.APPLICANT:
                    nextUser = applyUser;
                    break;
                case FlowConstants.PLAT_MANAGER:
                    nextUser = platApproveUser;
                    break;
                case FlowConstants.LEADER:
                    nextUser = sysUserService.getLeader(username, validSuperLeader);
                    break;
                case FlowConstants.RESOURCE_OWNER:
                    nextUser = owner;
                    break;
                case FlowConstants.ORDER_HPC_MANAGER:
                    nextUser = getOrderHpcPartitionManagers(serviceId);
                    break;
                case FlowConstants.ORDER_HPC_CLUSTER_MANAGER:
                    nextUser = getOrderHpcClusterManagers(serviceType);
                    break;
                default:
                    // 默认寻找对应的系统角色信息
                    nextUser = getSystemUserRole(assignee);
                    break;
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            nextUser = platApproveUser;
        }
        if (StringUtils.isBlank(nextUser)) {
            log.error("未找到审批人");
            nextUser = platApproveUser;
        }
        return nextUser;
    }

}
