package com.wustzdy.springboot.flowable.demo.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wustzdy.springboot.flowable.demo.constant.FlowConstants;
import com.wustzdy.springboot.flowable.demo.dao.BusinessTemplateDao;
import com.wustzdy.springboot.flowable.demo.dao.PimOrderDao;
import com.wustzdy.springboot.flowable.demo.entity.BusinessFlowsEntity;
import com.wustzdy.springboot.flowable.demo.entity.BusinessTemplateEntity;
import com.wustzdy.springboot.flowable.demo.entity.PimOrderEntity;
import com.wustzdy.springboot.flowable.demo.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.wustzdy.springboot.flowable.demo.constant.Constant.SEPARATOR_COMMA;
import static com.wustzdy.springboot.flowable.demo.constant.Constant.SEPARATOR_SEMICOLON;
import static com.wustzdy.springboot.flowable.demo.constant.FlowConstants.*;

@SuppressWarnings("all")
@Service
@Slf4j
public class BasicFlowService {
    @Autowired
    private IStartTaskSvc iStartTaskSvc;
    @Autowired
    private TaskAssgineeService taskAssgineeService;
    @Autowired
    private ActTaskService actTaskService;
    @Autowired
    private WorkFlowService workFlowService;
    @Value("${order.plat.approveUser}")
    private String platApproveUser;
    @Autowired
    private BusinessFlowsProxyService proxyService;
    @Autowired
    private IHandlerTaskSvc iHandlerTaskSvc;
    @Autowired
    private IActivitiUtilSvc iActivitiUtilSvc;
    @Autowired
    private PimOrderDao pimOrderDao;
    @Autowired
    private BusinessTemplateDao businessTemplateDao;


    public ResultVo startAndPassProcess(String businessKey, String userName, BusinessFlowsEntity businessFlowsEntity, Map<String, Object> vars) {
        log.info("--startAndPassProcess--businessKey:{}", businessKey);
        log.info("--startAndPassProcess--businessFlowsEntity:{}", businessFlowsEntity);
        log.info("--startAndPassProcess--vars:{}", vars);
        // 获取流程定义Key
        BusinessTemplateEntity templateEntity = getBusinessTemplate(businessFlowsEntity.getServiceType(), businessFlowsEntity.getOperationType());
        if (businessFlowsEntity == null) {
            throw new RuntimeException("流程业务businessFlows信息为空");
        }
        // 校验流程定义
        validBusnessTemplate(templateEntity);

        log.info("--templateEntity:{}", templateEntity.getVarTmpl());
        if (templateEntity.getVarTmpl() == null) {
            return doStartProcess(businessKey, templateEntity, userName, businessFlowsEntity);
        } else {
//            return doStartMpProcess(businessKey, templateEntity, userName, businessFlowsEntity, vars);
            return null;
        }

    }

    private void validBusnessTemplate(BusinessTemplateEntity templateEntity) {
        if (templateEntity == null) {
            throw new RuntimeException("流程未定义");
        }
    }

    public BusinessTemplateEntity getBusinessTemplate(String serviceType, String operationType) {
        // 非默认设置
        BusinessTemplateEntity templateEntity = businessTemplateDao.selectByBusinessTypeAndBusinessOperation(serviceType, operationType);

        // 以逗号分隔符处理的复合操作类型默认设置
        if (templateEntity == null) {
            String[] complexTemplateTypes = operationType.split(SEPARATOR_COMMA);
            if (complexTemplateTypes.length > 1) {
                templateEntity = businessTemplateDao.selectByBusinessTypeAndBusinessOperation(serviceType, complexTemplateTypes[0]);
            }
        }

        // 类型默认设置
        if (templateEntity == null) {
            templateEntity = businessTemplateDao.selectDefaultByBusinessType(serviceType);
        }

        return templateEntity;
    }

    private ResultVo doStartProcess(String businessKey, BusinessTemplateEntity templateEntity, String userName, BusinessFlowsEntity businessFlowsEntity) {
        log.info("-doStartProcess-businessKey-:{}", businessKey);
        log.info("-doStartProcess-templateEntity-:{}", templateEntity);
        log.info("-doStartProcess-businessFlowsEntity-:{}", businessFlowsEntity);

        // 启动流程
        String processDefinitionKey = templateEntity.getTemplateType();

        // 流程基本信息
        String title = businessFlowsEntity.getContent();
        ResultVo data = iStartTaskSvc.doStartProcess(processDefinitionKey, userName, businessKey, title);
        log.info("-ResultVo-data--:{}", data);

        String processInstanceId = data.getProcessInstanceId();
        try {
            // 关联业务信息
            String processDefinitionId = data.getProcessDefinitionId();
            businessFlowsEntity.setProcessDefinitionId(processDefinitionId);
            businessFlowsEntity.setJobStat(FlowConstants.FLOW_STAT_PROCESS);
            businessFlowsEntity.setProcessDefinitionKey(processDefinitionKey);
            businessFlowsEntity.setProcessInstanceId(processInstanceId);
            businessFlowsEntity.setBusinessTemplateId(templateEntity.getTemplateId());
            businessFlowsEntity.setGmtCreated(new Date());
            // 获取下一环节处理人
            String nextTaskAndStaff = getApproveUser(userName,
                    templateEntity.getTemplateId(),
                    businessFlowsEntity.getServiceType(),
                    businessFlowsEntity.getApplyUser(),
                    businessFlowsEntity.getExtend(),
                    data.getProcessDefinitionId(),
                    data.getNextActivityIds().get(0));
            log.info("--doStartProcess-nextTaskAndStaff-:{}", nextTaskAndStaff);
            // 完成当前启动流程
            PassFlowVo passFlowVo = new PassFlowVo();
            passFlowVo.setNextTaskAndStaffId(nextTaskAndStaff);
            passFlowVo.setComment("");
            passFlowVo.setTaskId(data.getTaskId());

            data = passProcess(passFlowVo, userName, "");

            // 代理审批后，设置下一审批人
            businessFlowsEntity.setNextStaff(data.getAssignee());
            // 补全工单流程信息
            updateOrderEntity(businessFlowsEntity);

            // 自动审批
            if (data.getAssignee() != null) {
                String[] users = data.getAssignee().split(",");
                List<String> userList = Arrays.asList(users);
                if (userList.contains(businessFlowsEntity.getApplyUser())) {
                    PassFlowVo nextPassFlowVo = new PassFlowVo();
                    nextPassFlowVo.setTaskId(data.getNextTaskId());
                    nextPassFlowVo.setComment("系统自动审批");
                    workFlowService.passProcess(nextPassFlowVo, businessFlowsEntity.getApplyUser(), "审批通过");
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException("流程启动失败，请联系管理员," + e.getMessage(), e);
        }
        return data;
    }

    public String getApproveUser(String userName, Integer templateId, String serviceType,
                                 String applyUser, String extend,
                                 String processDefinitionId, String activityId) {
        ApproveLeaderVo approveLeaderVo = new ApproveLeaderVo();
        approveLeaderVo.setActivityId(activityId);
        approveLeaderVo.setApplyUser(applyUser);
        approveLeaderVo.setBusinessTemplateId(templateId);
        approveLeaderVo.setCurrentUser(userName);
        approveLeaderVo.setOperType(FLOW_OPER_PASS);
        approveLeaderVo.setOwner(applyUser);
        approveLeaderVo.setProcessDefinitionId(processDefinitionId);
        // 训练集群特殊处理带入extend，extend字段表示分区id，找到对应处理
        if (StringUtils.isNotBlank(serviceType) && serviceType.equalsIgnoreCase(ORDER_TYPE_CLUSTER_TRAIN)) {
            try {
                JSONObject jsonObject = JSON.parseObject(extend);
                String partationId = jsonObject.getString("partationId");
                String cluster = jsonObject.getString("cluster");
                approveLeaderVo.setServiceId(partationId);
                approveLeaderVo.setServiceType(cluster);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return taskAssgineeService.getApproveLeader(approveLeaderVo);
    }

    public ResultVo passProcess(PassFlowVo passflowVo, String userName, String operation) {
        log.info("--passProcess-passflowVo:{}", passflowVo);
        log.info("--passProcess-userName:{}", userName);
        String taskId = passflowVo.getTaskId();
        if (actTaskService.isMpTask(taskId)) {
            return passMpProcess(passflowVo, userName, operation);
        }

        String nextUser = passflowVo.getNextTaskAndStaffId();

        String comment = passflowVo.getComment();
        // 设置通过后，下一环节的处理人
        if (StringUtils.isBlank(nextUser)) {
//            nextUser = com.sensetime.cloud.flow.FlowConstants.PLAT_APPROVE_USER;
            nextUser = platApproveUser;
        }

        validNextUser(nextUser);
        // 分号转逗号
        if (nextUser.contains(SEPARATOR_SEMICOLON)) {
            nextUser = StrUtil.join(SEPARATOR_COMMA, nextUser.split(SEPARATOR_SEMICOLON));
        }
        // 代理审批逻辑

        FlowsProxyVo flowsProxyVo = proxyService.getProxyUserInfo(nextUser);
        log.info("--passProcess-flowsProxyVo:{}", flowsProxyVo);

        nextUser = flowsProxyVo.getNextUser();
        ResultVo resultVo = iHandlerTaskSvc.doPassTask(taskId, comment, nextUser, operation, userName);
        log.info("--passProcess-resultVo:{}", resultVo);

        proxyService.checkAndDoProxy(userName, taskId, resultVo.getCommentId(), "pass");
        proxyService.saveProxyInstance(flowsProxyVo, resultVo);
        return resultVo;
    }

    private void validNextUser(String nextUser) {
    }

    private PimOrderEntity updateOrderEntity(BusinessFlowsEntity businessFlowsEntity) {
        String serviceId = businessFlowsEntity.getServiceId();
        PimOrderEntity orderEntity = pimOrderDao.getByOrderNum(serviceId);
        orderEntity.setProcessDefinitionKey(businessFlowsEntity.getProcessDefinitionKey());
        orderEntity.setProcessInstanceId(businessFlowsEntity.getProcessInstanceId());
        orderEntity.setProcessDefinitionId(businessFlowsEntity.getProcessDefinitionId());
        orderEntity.setBusinessTemplateId(businessFlowsEntity.getBusinessTemplateId());
        orderEntity.setNextStaff(businessFlowsEntity.getNextStaff());
        orderEntity.setCurrentDealUser(businessFlowsEntity.getNextStaff());
        pimOrderDao.updateById(orderEntity);

        return orderEntity;
    }

    public ResultVo passMpProcess(PassFlowVo passflowVo, String userName, String operation) {
        log.debug("call passMpProcess");
        String taskId = passflowVo.getTaskId();
        String comment = passflowVo.getComment();

        ResultVo resultVo = actTaskService.doPassMpTask(taskId, comment, operation, userName);
        log.info("--resultVo.getCommentId()--:{}", resultVo.getCommentId());

        proxyService.checkAndDoProxy(userName, taskId, resultVo.getCommentId(), "pass");

        return resultVo;

    }

    public ResultVo passProcessPropagation(PassFlowVo passflowVo, String userName, String operation) {
        // 获取下一环节处理人
        String taskId = passflowVo.getTaskId();
        if (!actTaskService.isMpTask(taskId)) {
            ProcessInstance processInstance = iActivitiUtilSvc.findProcessInstanceByTaskId(taskId);
            String processInstanceId = processInstance.getProcessInstanceId();
            String processDefinitionId = processInstance.getProcessDefinitionId();
            String nextTaskType = iHandlerTaskSvc.getNextTaskType(processInstanceId);
            if (nextTaskType.equalsIgnoreCase(END)) {
                passflowVo.setNextTaskAndStaffId(NO_BODY);
            } else {
                PimOrderEntity pimOrderEntity = pimOrderDao.getByProcessInstanceId(processInstanceId);
                List<NextTask> nextTasks = nextTask(processInstanceId);
                String nextActivityId = nextTasks.get(0).getId();

                if (pimOrderEntity == null) {
                    throw new RuntimeException("该流程无业务信息");
                }
                String extend = pimOrderEntity.getExtend();
                String applyUser = pimOrderEntity.getOrderUser();
                String serviceType = pimOrderEntity.getOrderTypeId();
                Integer businessTemplateId = pimOrderEntity.getBusinessTemplateId();
                // 审批人
                String nextTaskAndStaff = getApproveUser(userName, businessTemplateId, serviceType, applyUser, extend,
                        processDefinitionId, nextActivityId);
                passflowVo.setNextTaskAndStaffId(nextTaskAndStaff);
            }
        }

        return this.passProcess(passflowVo, userName, operation);
    }

    public List<NextTask> nextTask(String processInstanceId) {
        List<Map> nextTaskList = iHandlerTaskSvc.getNextTasks(processInstanceId);
        return nextTaskList.stream().map(nextTask -> new NextTask(nextTask.get("id"), nextTask.get("name"))).collect(Collectors.toList());
    }

}
