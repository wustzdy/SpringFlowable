package com.wustzdy.springboot.flowable.demo.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import com.wustzdy.springboot.flowable.demo.cmd.WithdrawTaskCmd;
import com.wustzdy.springboot.flowable.demo.service.IActivitiUtilSvc;
import com.wustzdy.springboot.flowable.demo.service.ICommonSvc;
import com.wustzdy.springboot.flowable.demo.util.BaseUtils;
import com.wustzdy.springboot.flowable.demo.vo.ActivityVo;
import com.wustzdy.springboot.flowable.demo.vo.TaskVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.*;
import org.flowable.bpmn.model.Process;
import org.flowable.common.engine.impl.context.Context;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.impl.ProcessEngineImpl;
import org.flowable.engine.impl.RepositoryServiceImpl;
import org.flowable.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.flowable.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Comment;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.image.impl.DefaultProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.flowable.ui.modeler.util.ImageGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipInputStream;

import static com.wustzdy.springboot.flowable.demo.constant.BasicCloudConstants.BAR;
import static com.wustzdy.springboot.flowable.demo.constant.BasicCloudConstants.ZIP;
import static com.wustzdy.springboot.flowable.demo.constant.FlowConstants.END;
import static com.wustzdy.springboot.flowable.demo.constant.FlowConstants.FLOW_STAT_DELETE;


@Slf4j
@Service("iActivitiUtilSvc")
@Transactional(rollbackFor = Exception.class)
public class ActivitiUtilSvcImpl implements IActivitiUtilSvc {

    @Autowired
    private ICommonSvc iCommonSvc;
    /**
     * 获得任务操作接口
     */
    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private IdentityService identityService;

    /**
     * 启动流程
     */
    @Override
    public ProcessInstance startProcess(String processDefinitionKey, String businessKey, Map<String, Object> variables) {
        // 同时向正在执行的执行对象表中的字段BUSINESS_KEY添加业务数据，同时让流程关联业
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, variables);
        return processInstance;
    }

    /**
     * 使用部署对象ID和资源图片名称，获取图片的输入流
     *
     * @return
     */
    @Override
    public InputStream findImageInputStream(String deploymentId,
                                            String diagramResourceName) {
        return repositoryService.getResourceAsStream(deploymentId, diagramResourceName);
    }

    /**
     * 获取流程图片方法
     */
    @Override
    public byte[] getDiagramImageByDeploymentId(String deploymentId, String imageType) {

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId).singleResult();

        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());

        BufferedImage image = ImageGenerator.createImage(bpmnModel);

        return ImageGenerator.createByteArrayForImage(image, imageType);
    }

    /**
     * 获取流程图片方法
     */
    @Override
    public byte[] getDiagramImageByProcessInstanceId(String processInstanceId) {

        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

        if (historicProcessInstance == null) {
            throw new RuntimeException("获取流程图异常");
        } else {
            BpmnModel bpmnModel = repositoryService.getBpmnModel(historicProcessInstance.getProcessDefinitionId());
            BufferedImage image = ImageGenerator.createImage(bpmnModel);

            return ImageGenerator.createByteArrayForImage(image, "png");
        }
    }

    /**
     * 获取批注信息，传递的是当前任务ID，获取历史任务ID对应的批注
     */
    @Override
    public List<Comment> findCommentByTaskId(String taskId) {
        // 使用当前的任务ID，查询当前流程对应的历史任务ID
        // 使用当前任务ID，获取当前任务对象
        Task task = findTaskById(taskId);
        // 获取流程实例ID
        String processInstanceId = task.getProcessInstanceId();
        return taskService.getProcessInstanceComments(processInstanceId);
    }

    /**
     * 获取批注信息，传递的是当前任务ID，获取历史任务ID对应的批注
     */
    @Override
    public List<Comment> findCommentByProcessInstanceId(String processInstanceId) {
        // 使用当前的任务ID，查询当前流程对应的历史任务ID
        // 使用当前任务ID，获取当前任务对象
        // 获取流程实例ID
        return taskService.getProcessInstanceComments(processInstanceId);
    }

    /**
     * 使用业务ID，查询历史批注信息
     */
    @Override
    public List<Comment> findCommentByBusinessKey(String businessKey) {

        /* 1:使用历史的流程实例查询，返回历史的流程实例对象，获取流程实例ID */
        String processInstanceId = getProcessInstanceIdByBusinessKey(businessKey);
        return taskService.getProcessInstanceComments(processInstanceId);
    }


    @Override
    public String getProcessInstanceIdByBusinessKey(String businessKey) {
        //对应历史的流程实例表
        HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                //使用BusinessKey字段查询
                .processInstanceBusinessKey(businessKey)
                .singleResult();
        //流程实例ID
        return hpi.getId();
    }
    /*****************************以下为流程转向操作核心逻辑*****************************/
    /**
     * 签收
     */
    @Override
    public String claimProcess(String taskId, String userId) {
        if (StringUtils.isBlank(userId)) {
            throw new RuntimeException("请指定签收人");
        }
        TaskEntity task = findTaskById(taskId);
        if (task == null) {
            return userId;
        }
        if (task.getAssignee() == null) {
            taskService.claim(taskId, userId);
            return userId;
        } else {
            return task.getAssignee();
        }

    }

    /**
     * 组任务
     */
    @Override
    public void addCandidateUser(String taskId, String[] userIds) {
        for (String userId : userIds) {
            taskService.addCandidateUser(taskId, userId);
        }
    }

    /**
     * 组任务
     */
    @Override
    public void deleteCandidateUser(String taskId, String[] userIds) {
        for (String userId : userIds) {
            taskService.deleteCandidateUser(taskId, userId);
        }
    }

    /**
     * 提交
     *
     * @param taskId     当前任务ID
     * @param variables  流程变量
     * @param activityId 流程转向执行任务节点ID
     *                   此参数为空，默认为提交操作
     */
    @Override
    public void commitProcess(String taskId, Map<String, Object> variables, String activityId) {
        if (variables == null) {
            variables = Collections.emptyMap();
        }
        // 跳转节点为空，默认提交操作
        if (BaseUtils.isEmpty(activityId)) {
            taskService.complete(taskId, variables);
        } else {
            // 流程转向操作
            turnTransition(taskId, activityId, variables);
        }
    }

    /**
     * 终止流程
     * <p>
     * 活动节点
     */
    @Override
    public void stopProcess(String procInstanceId, String deleteReason, String curJobId) {
        runtimeService.deleteProcessInstance(procInstanceId, deleteReason);
        String sql = "update ACT_HI_PROCINST set end_act_id_='" + curJobId + "' where proc_inst_id_='" + procInstanceId + "'";
        iCommonSvc.executeUpdateSql(sql);
    }

    /**
     * 删除流程
     * <p>
     * 活动节点
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public void deleteProcess(String procInstanceId) {
        try {
            List<ProcessInstance> list = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(procInstanceId)
                    .list();
            if (CollectionUtil.isNotEmpty(list)) {
                runtimeService.deleteProcessInstance(procInstanceId, FLOW_STAT_DELETE);
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        try {
            List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery()
                    .processInstanceId(procInstanceId)
                    .list();
            if (CollectionUtil.isNotEmpty(list)) {
                historyService.deleteHistoricProcessInstance(procInstanceId);
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
    }

    /**
     * 转办流程
     * <p>
     * 活动节点
     */
    @Override
    public void transferProcess(String taskId, String userId) {
        taskService.setAssignee(taskId, userId);
    }

    /**
     * 挂起流程
     * <p>
     * 活动节点
     */
    @Override
    public void suspendProcess(String procInstanceId) {
        runtimeService.suspendProcessInstanceById(procInstanceId);
    }

    /**
     * 解挂流程
     * <p>
     * 活动节点
     */
    @Override
    public void resumeProcess(String procInstanceId) {
        runtimeService.activateProcessInstanceById(procInstanceId);
    }


    /**
     * 自己取回流程
     */
    @Override
    public void callBackProcess(String historyTaskId) {
        String historicActivityInstanceId = iCommonSvc.getTranslate("select id_ from ACT_HI_ACTINST where task_id_='" + historyTaskId + "'");
        Command<List<String>> cmd = new WithdrawTaskCmd(historyTaskId, historicActivityInstanceId);
        List<String> historyNodeIds = ProcessEngines.getDefaultProcessEngine().getManagementService().executeCommand(cmd);
        for (String id : historyNodeIds) {
            iCommonSvc.executeUpdateSql("delete from ACT_HI_ACTINST where id_='" + id + "'");
        }
    }

    /**
     * 清空指定活动节点流向
     * <p>
     * 活动节点
     * <p>
     * 节点流向集合
     */
    private List<PvmTransition> clearTransition(ActivityImpl activityImpl) {
        // 存储当前节点所有流向临时变量
        // 获取当前节点所有流向，存储到临时变量，然后清空
        List<PvmTransition> pvmTransitionList = activityImpl.getOutgoingTransitions();
        List<PvmTransition> oriPvmTransitionList = new ArrayList<>(pvmTransitionList);
        pvmTransitionList.clear();

        return oriPvmTransitionList;
    }


    /**
     * 还原指定活动节点流向
     * <p>
     * 活动节点
     *
     * @param oriPvmTransitionList 原有节点流向集合
     */
    private void restoreTransition(ActivityImpl activityImpl, List<PvmTransition> oriPvmTransitionList) {
        // 清空现有流向
        List<PvmTransition> pvmTransitionList = activityImpl.getOutgoingTransitions();
        pvmTransitionList.clear();
        // 还原以前流向
        pvmTransitionList.addAll(oriPvmTransitionList);
    }

    /**
     * 流程转向操作
     *
     * @param taskId     当前任务ID
     * @param activityId 目标节点任务ID
     * @param variables  流程变量
     */
    private void turnTransition(String taskId, String activityId, Map<String, Object> variables) {
        // 当前节点
        ActivityImpl currActivity = findActivitiImpl(taskId, null);
        // 清空当前流向
        List<PvmTransition> oriPvmTransitionList = clearTransition(currActivity);

        // 创建新流向
        TransitionImpl newTransition = currActivity.createOutgoingTransition();
        // 目标节点
        ActivityImpl pointActivity = findActivitiImpl(taskId, activityId);
        // 设置新流向的目标节点
        newTransition.setDestination(pointActivity);

        // 执行转向任务
        taskService.complete(taskId, variables);
        // 删除目标节点新流入
        pointActivity.getIncomingTransitions().remove(newTransition);

        // 还原以前流向
        restoreTransition(currActivity, oriPvmTransitionList);
    }
    /*****************************以上为流程转向操作核心逻辑*****************************/


    /*****************************以下根据 任务节点ID 获取流程各对象查询方法********************************/
    /**
     * 根据流程定义Key获取所有节点
     */
    @Override
    public List<ActivityVo> allActivityIds(String processDefinitionKey) {
        List<ActivityVo> jobList = new ArrayList<>();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey).latestVersion().singleResult();
        /*ProcessDefinitionEntity def = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(processDefinition.getId());
        //rs是指RepositoryService的实例
        List<ActivityImpl> activitiList = def.getActivities();
        for (ActivityImpl activityImpl : activitiList) {
            String type = activityImpl.getProperty("type").toString();
            if (!(type.endsWith("Gateway") || "endEvent".equalsIgnoreCase(type) || type.equalsIgnoreCase("startEvent"))) {
                ActivityVo map = new ActivityVo();
                map.setId(activityImpl.getId());
                map.setName(String.valueOf(activityImpl.getProperty("name")));
                jobList.add(map);
            }
        }
        log.info("list" + jobList.size());
        return jobList;*/

        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());

        Process process = bpmnModel.getProcesses().get(0);
        //获取所有节点
        Collection<FlowElement> flowElements = process.getFlowElements();

        List<UserTask> UserTaskList = process.findFlowElementsOfType(UserTask.class);
        for (UserTask userTask : UserTaskList) {
            ActivityVo map = new ActivityVo();
            map.setId(userTask.getId());
            map.setName(String.valueOf(userTask.getName()));
            jobList.add(map);
        }
        return jobList;
    }

    /**
     * 根据流程定义Key获取所有节点
     */
    @Override
    public List<ActivityVo> allActivityIdsById(String processDefinitionId) {
        List<ActivityVo> jobList = new ArrayList<>();
        /*ProcessDefinitionEntity def = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(processDefinitionId);
        //rs是指RepositoryService的实例
        List<ActivityImpl> activitiList = def.getActivities();
        for (ActivityImpl activityImpl : activitiList) {
            String type = activityImpl.getProperty("type").toString();
            if (!(type.endsWith("Gateway") || "endEvent".equalsIgnoreCase(type) || type.equalsIgnoreCase("startEvent"))) {
                ActivityVo map = new ActivityVo();
                map.setId(activityImpl.getId());
                map.setName(String.valueOf(activityImpl.getProperty("name")));
                jobList.add(map);
            }
        }
        log.info("list" + jobList.size());
        return jobList;*/


        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);

        Process process = bpmnModel.getProcesses().get(0);
        //获取所有节点
        Collection<FlowElement> flowElements = process.getFlowElements();

        List<UserTask> UserTaskList = process.findFlowElementsOfType(UserTask.class);
        for (UserTask userTask : UserTaskList) {
            ActivityVo map = new ActivityVo();
            map.setId(userTask.getId());
            map.setName(String.valueOf(userTask.getName()));
            jobList.add(map);
        }
        return jobList;
    }

    /**
     * 获取下一步节点
     * <p>
     * 1:"map" 返回List<IMap> 2:"string" 返回List<String>
     */
    @Override
    public List getNextJobs(String procInstanceId, String activityId, String listType) {
        List list = new ArrayList<>();

        // 流程标示
        String processDefinitionId = historyService.createHistoricProcessInstanceQuery().processInstanceId(procInstanceId).singleResult()
                .getProcessDefinitionId();

        ProcessDefinitionEntity def = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(processDefinitionId);

        /*
        // 获得当前任务的所有节点
        List<ActivityImpl> activitiList = def.getActivities();
        for (ActivityImpl activityImpl : activitiList) {
            String id = activityImpl.getId();
            if (activityId.equals(id)) {
                System.out.println("当前任务：" + activityImpl.getProperty("name") + "  当前ID：" + activityId);
                List<PvmTransition> outTransitions = activityImpl.getOutgoingTransitions();
                jumpGateway(list, outTransitions, listType);
            }
        }
        return list;
         */

        // taskId:任务ID，approved：任意选择条件
        Map<String, Object> data = new HashMap<>();
        data.put("approved", approved);
        //当前任务信息
        Task task = taskService.createTaskQuery().processInstanceId(procInstanceId).singleResult();

        //获取流程发布Id信息
        String definitionId = runtimeService.createProcessInstanceQuery().processInstanceId(procInstanceId).singleResult().getProcessDefinitionId();

        //获取bpm对象
        BpmnModel bpmnModel = repositoryService.getBpmnModel(definitionId);

        //传节点定义key 获取当前节点
        FlowNode flowNode = (FlowNode) bpmnModel.getFlowElement(task.getTaskDefinitionKey());
        // 获取本节点的Id和Name
        System.out.println("当前节点Id" + flowNode.getId());
        System.out.println("当前节点名称" + flowNode.getName());

        //输出连线
        List<SequenceFlow> outgoingFlows = flowNode.getOutgoingFlows();

        //遍历返回下一个节点信息
        for (SequenceFlow outgoingFlow : outgoingFlows) {
            //类型自己判断（获取下个节点是网关还是节点）
            FlowElement targetFlowElement = outgoingFlow.getTargetFlowElement();
            //下个是节点
            if (targetFlowElement instanceof UserTask) {
                // 判断是否是会签
                UserTask userTask = (UserTask) targetFlowElement;
                if (userTask.getBehavior() instanceof ParallelMultiInstanceBehavior) {
                    ParallelMultiInstanceBehavior behavior = (ParallelMultiInstanceBehavior) userTask.getBehavior();
                    if (behavior != null && behavior.getCollectionExpression() != null) {
                        System.out.println("当前节点是会签");
                    }
                } else {
                    System.out.println("当前节点不是会签");
                }
                if (approvalFlowNodeDto.getBehaviorFlag()) { // 下个节点是会签
                    // 会签的候选用户Key
                    String assignees = ((UserTask) targetFlowElement).getAssignee();
                    log.info("获取会签的id");
                    String nextCandidateUsers = (((UserTask) targetFlowElement).getLoopCharacteristics().getInputDataItem());
                    log.info("获取会签的collection，候选人List", nextCandidateUsers);
                } else { // 下个节点不是会签
                    String candidateUserses = ((UserTask) targetFlowElement).getCandidateUsers().get(0);
                    log.info("获取用户节点的candidateUsers，候选人", candidateUserses);
                    String candidateUsers = candidateUserses.substring(candidateUserses.indexOf("{") + 1, candidateUserses.indexOf("}"));
                    log.info("查询出的值是${submitUser}，截取获取用户节点的candidateUsers，候选人", candidateUsers);
                }
                System.out.println("下一节点: id=" + targetFlowElement.getId() + ",name=" + targetFlowElement.getName())；
            } else if (targetFlowElement instanceof ExclusiveGateway) {
                setExclusiveGateway(targetFlowElement);
            }
        }

    }

    private void setExclusiveGateway(FlowElement targetFlow) {
        //排他网关，获取连线信息
        List<SequenceFlow> targetFlows = ((ExclusiveGateway) targetFlow).getOutgoingFlows();
        for (SequenceFlow sequenceFlow : targetFlows) {
            //目标节点信息
            FlowElement targetFlowElement = sequenceFlow.getTargetFlowElement();
            if (targetFlowElement instanceof UserTask) {
                // do something
            } else if (targetFlowElement instanceof EndEvent) {
                // do something
            } else if (targetFlowElement instanceof ServiceTask) {
                // do something
            } else if (targetFlowElement instanceof ExclusiveGateway) {
                //递归寻找
                setExclusiveGateway(targetFlowElement);
            } else if (targetFlowElement instanceof SubProcess) {
                // do something
            }
        }
    }

    /**
     * 获取排他网关分支名称、分支表达式、下一级任务节点
     *
     * @param flowElement
     * @param data
     */
    private void setExclusiveGateway(FlowElement flowElement, Map data) {
        // 获取所有网关分支
        List<SequenceFlow> targetFlows = ((ExclusiveGateway) flowElement).getOutgoingFlows();
        // 循环每个网关分支
        for (SequenceFlow sequenceFlow : targetFlows) {
            // 获取下一个网关和节点数据
            FlowElement targetFlowElement = sequenceFlow.getTargetFlowElement();
            // 网关数据不为空
            if (StringUtils.isNotBlank(sequenceFlow.getConditionExpression())) {
                //计算连接线上的表达式
                Object result = managementService.executeCommand(new ExpressionEvaluateUtil(sequenceFlow.getConditionExpression(), data));
                System.out.println("排他网关中线条: id=" + sequenceFlow.getId() + ",name=" + sequenceFlow.getName() + ",result=" + result + ",ConditionExpression=" + sequenceFlow.getConditionExpression());
                // 获取网关判断条件
                String conditionExpression = null;
                if (sequenceFlow.getConditionExpression() != null) {
                    conditionExpression = sequenceFlow.getConditionExpression().substring(sequenceFlow.getConditionExpression().indexOf("==") + 2,
                            sequenceFlow.getConditionExpression().indexOf("}"));
                }
                System.out.println("截取后的选择条件: id=" + conditionExpression);
            }
            // 网关的下个节点是用户节点
            if (targetFlowElement instanceof UserTask) {
                // 判断是否是会签
                UserTask userTask = (UserTask) targetFlowElement;
                if (userTask.getBehavior() instanceof ParallelMultiInstanceBehavior) {
                    ParallelMultiInstanceBehavior behavior = (ParallelMultiInstanceBehavior) userTask.getBehavior();
                    if (behavior != null && behavior.getCollectionExpression() != null) {
                        System.out.println("当前节点是会签");
                    }
                } else {
                    System.out.println("当前节点不是会签");
                }
                System.out.println("排他网关的下一节点是UserTask: id=" + targetFlowElement.getId() + ",name=" + targetFlowElement.getName());
                System.out.println("排他网关的下一节点CandidateUsers候选者" + ((UserTask) targetFlowElement).getCandidateUsers());
                if (BehaviorFlag()---- 自己去前边判断){ // 会签的候选用户Key
                    // 判断会签用户的用户提交人的List
                    MultiInstanceLoopCharacteristics a = (((UserTask) targetFlowElement).getLoopCharacteristics());
                    // "获取会签的collection，候选人List"
                    String = nextCandidateUsers(((UserTask) targetFlowElement).getLoopCharacteristics().getInputDataItem());
                } else{ //不是会签
                    // 获取用户的候选人
                    String candidateUserses = ((UserTask) targetFlowElement).getCandidateUsers().get(0);
                    // 截取候选人的值
                    String candidateUsers = candidateUserses.substring(candidateUserses.indexOf("{") + 1, candidateUserses.indexOf("}"));
                }
            } else if (targetFlowElement instanceof EndEvent) {
                System.out.println("排他网关的下一节点是EndEvent: 结束节点");
            } else if (targetFlowElement instanceof ServiceTask) {
                System.out.println("排他网关的下一节点是ServiceTask: 内部方法");
            } else if (targetFlowElement instanceof ExclusiveGateway) {
                setExclusiveGateway(targetFlowElement, data);
            } else if (targetFlowElement instanceof SubProcess) {
                System.out.println("排他网关的下一节点是SubProcess: 内部子流程");
            }
        }
    }

    /**
     * 递归跳过网关节点，直到取到非网关节点为止
     */
    private void jumpGateway(List list, List<PvmTransition> outTransitions, String listType) {
        for (PvmTransition tr : outTransitions) {
            //获取线路的下一节点
            PvmActivity ac = tr.getDestination();
            String type = ac.getProperty("type").toString();
            if (type.endsWith("Gateway")) {
                List<PvmTransition> outTransitionsTemp = ac.getOutgoingTransitions();
                jumpGateway(list, outTransitionsTemp, listType);
            } else {
                if ("map".equals(listType.toLowerCase())) {
                    Map map = new HashMap();
                    map.put("id", ac.getId());
                    map.put("name", ac.getProperty("name"));
                    list.add(map);
                } else if ("string".equals(listType.toLowerCase())) {
                    list.add(ac.getId());
                }
            }
        }
    }

    /**
     * 获取下一节点类型，如果是结束网关，就再获取下一节点
     * <p>
     * exclusive分支，inclusive包含， parallel并发， task任务节点或结束节点，end结束
     */
    @Override
    public String getNextType(String procInstanceId, String activityId) {

        getNextTypeNew(procInstanceId, activityId);
        String resultType = "";

        // 流程标示
        String processDefinitionId = historyService.createHistoricProcessInstanceQuery().processInstanceId(procInstanceId).singleResult()
                .getProcessDefinitionId();
        ProcessDefinitionEntity def = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(processDefinitionId);

        // 获得当前任务的所有节点
        List<ActivityImpl> activitiList = def.getActivities();
        for (ActivityImpl activityImpl : activitiList) {
            String id = activityImpl.getId();
            if (activityId.equals(id)) {
                //当前任务：activityImpl.getProperty("name")，当前ID：activityId
                List<PvmTransition> outTransitions = activityImpl.getOutgoingTransitions();
                if (outTransitions.size() > 0) {
                    PvmTransition tr = outTransitions.get(0);
                    //获取线路的下一节点
                    PvmActivity ac = tr.getDestination();

                    String nextType = ac.getProperty("type").toString();
                    String nextId = ac.getId().toLowerCase();

                    // 结束网关
                    if (nextType.endsWith("Gateway") && nextId.endsWith("_end")) {
                        List<PvmTransition> outTransitionsTemp = ac.getOutgoingTransitions();
                        if (outTransitionsTemp.size() > 0) {
                            PvmTransition tr1 = outTransitionsTemp.get(0);
                            //获取线路的下一节点
                            PvmActivity ac1 = tr1.getDestination();
                            nextType = ac1.getProperty("type").toString();
                        }
                    }

					/*
					("userTask", "用户任务")
			        ("serviceTask", "系统任务")
			        ("startEvent", "开始节点")
			        ("endEvent", "结束节点")
			        ("exclusiveGateway", "分支判断节点")
			        ("inclusiveGateway", "包含处理节点")
			        ("parallelGateway", "并发处理节点")
			        ("callActivity", "子流程")
			        */
                    if ("exclusiveGateway".equalsIgnoreCase(nextType)) {
                        //分支
                        resultType = "exclusive";
                    } else if ("inclusiveGateway".equalsIgnoreCase(nextType)) {
                        //包含
                        resultType = "inclusive";
                    } else if ("parallelGateway".equalsIgnoreCase(nextType)) {
                        //并发
                        resultType = "parallel";
                    } else if ("endEvent".equalsIgnoreCase(nextType)) {
                        //结束
                        resultType = "end";
                    } else {
                        //任务节点
                        resultType = "task";
                    }
                }
            }
        }


        return resultType;
    }

    private String getNextTypeNew(String procInstanceId, String activityId) {
        String resultType = "";

        // 流程标示
        String processDefinitionId = historyService.createHistoricProcessInstanceQuery().processInstanceId(procInstanceId).singleResult()
                .getProcessDefinitionId();
        ProcessDefinitionEntity def = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(processDefinitionId);

        // 获得当前任务的所有节点
        List<ActivityImpl> activitiList = def.getActivities();
        for (ActivityImpl activityImpl : activitiList) {
            String id = activityImpl.getId();
            if (activityId.equals(id)) {
                //当前任务：activityImpl.getProperty("name")，当前ID：activityId
                List<PvmTransition> outTransitions = activityImpl.getOutgoingTransitions();
                if (outTransitions.size() > 0) {
                    PvmTransition tr = outTransitions.get(0);
                    //获取线路的下一节点
                    PvmActivity ac = tr.getDestination();

                    String nextType = ac.getProperty("type").toString();
                    String nextId = ac.getId().toLowerCase();

                    // 结束网关
                    if (nextType.endsWith("Gateway") && nextId.endsWith("_end")) {
                        List<PvmTransition> outTransitionsTemp = ac.getOutgoingTransitions();
                        if (outTransitionsTemp.size() > 0) {
                            PvmTransition tr1 = outTransitionsTemp.get(0);
                            //获取线路的下一节点
                            PvmActivity ac1 = tr1.getDestination();
                            nextType = ac1.getProperty("type").toString();
                        }
                    }

					/*
					("userTask", "用户任务")
			        ("serviceTask", "系统任务")
			        ("startEvent", "开始节点")
			        ("endEvent", "结束节点")
			        ("exclusiveGateway", "分支判断节点")
			        ("inclusiveGateway", "包含处理节点")
			        ("parallelGateway", "并发处理节点")
			        ("callActivity", "子流程")
			        */
                    if ("exclusiveGateway".equalsIgnoreCase(nextType)) {
                        //分支
                        resultType = "exclusive";
                    } else if ("inclusiveGateway".equalsIgnoreCase(nextType)) {
                        //包含
                        resultType = "inclusive";
                    } else if ("parallelGateway".equalsIgnoreCase(nextType)) {
                        //并发
                        resultType = "parallel";
                    } else if ("endEvent".equalsIgnoreCase(nextType)) {
                        //结束
                        resultType = "end";
                    } else {
                        //任务节点
                        resultType = "task";
                    }
                }
            }
        }


        return resultType;
    }


    /**
     * 获取最后节点id
     */
    @Override
    public String getEndActivityId(String procInstanceId) {
        HistoricProcessInstance historicProcessInstance = historyService
                .createHistoricProcessInstanceQuery()
                .processInstanceId(procInstanceId)
                .singleResult();
        return historicProcessInstance.getEndActivityId();
    }

    /**
     * 根据流程实例ID获取当前运行的任务节点
     *
     * @return
     */
    @Override
    public ProcessInstance findProcessInstanceById(String processInstanceId) {
        return runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
    }

    /**
     * 根据流程实例ID获取当前运行的任务节点
     */
    @Override
    public Task findTask(String procInstanceId) {
        return taskService.createTaskQuery().processInstanceId(procInstanceId).singleResult();
    }

    /**
     * 根据流程实例ID获取当前运行的任务节点
     */
    @Override
    public List<Task> findTaskList(String procInstanceId) {
        return taskService.createTaskQuery().processInstanceId(procInstanceId).list();
    }

    /**
     * 根据流程实例ID和任务key值查询所有同级任务集合
     */
    @Override
    public Task findTaskByKey(String processInstanceId, String key) {
        return taskService.createTaskQuery().processInstanceId(processInstanceId).taskDefinitionKey(key).singleResult();
    }

    /**
     * 根据任务ID查询所有同级任务集合
     */
    @Override
    public List<Task> findTaskListByTaskId(String taskId) {
        return taskService.createTaskQuery().processInstanceId(findProcessInstanceByTaskId(taskId).getId())
                .taskDefinitionKey(findTaskById(taskId).getTaskDefinitionKey()).list();
    }


    /**
     * 根据流程实例ID和任务key值查询所有同级任务集合
     */
    @Override
    public List<Task> findTaskListByKey(String processInstanceId, String key) {
        return taskService.createTaskQuery().processInstanceId(processInstanceId).taskDefinitionKey(key).list();
    }

    /**
     * 根据任务ID获得任务实例
     */
    @Override
    public TaskEntity findTaskById(String taskId) {
        return (TaskEntity) taskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();
    }

    /**
     * 根据任务ID获取流程定义
     */
    @Override
    public ProcessDefinitionEntity findProcessDefinitionEntityByTaskId(String taskId) {
        // 取得流程定义
        return (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(findTaskById(taskId)
                        .getProcessDefinitionId());
    }

    /**
     * 根据任务ID获取对应的流程实例
     */
    @Override
    public ProcessInstance findProcessInstanceByTaskId(String taskId) {
        // 找到流程实例
        TaskEntity taskEntity = findTaskById(taskId);
        return runtimeService.createProcessInstanceQuery()
                .processInstanceId(taskEntity.getProcessInstanceId())
                .singleResult();
    }

    /**
     * 根据任务ID和节点ID获取活动节点
     *
     * @param taskId     任务ID
     * @param activityId 活动节点ID
     *                   如果为null或""，则默认查询当前活动节点
     *                   如果为"end"，则查询结束节点
     */
    @Override
    public ActivityImpl findActivitiImpl(String taskId, String activityId) {
        // 取得流程定义
        ProcessDefinitionEntity processDefinition = findProcessDefinitionEntityByTaskId(taskId);
        processDefinition.
                FlowableActivityEventImpl
        processDefinition.get

        // 获取当前活动节点ID
        if (BaseUtils.isEmpty(activityId)) {
            activityId = getActivityIdByTaskId(taskId);
        }

        // 根据流程定义，获取该流程实例的结束节点
        if (END.equals(activityId.toUpperCase())) {
            for (ActivityImpl activityImpl : processDefinition.getActivities()) {
                List<PvmTransition> pvmTransitionList = activityImpl.getOutgoingTransitions();
                if (pvmTransitionList.isEmpty()) {
                    return activityImpl;
                }
            }
        }

        // 根据节点ID，获取对应的活动节点
        ActivityImpl activityImpl = processDefinition.findActivity(activityId);

        return activityImpl;
    }

    @Override
    public String getActivityIdByTaskId(String taskId) {
        return findTaskById(taskId).getTaskDefinitionKey();
    }

    @Override
    public String getActivityIdByProcessInstanceId(String processInstanceId) {
        Task entity = findTaskByProcessId(processInstanceId);
        if (entity == null) {
            throw new RuntimeException("【" + processInstanceId + "】已经结束或撤单");
        }
        return entity.getTaskDefinitionKey();
    }


    /**
     * 获取走过的历史节点
     *
     * @return
     */
    @Override
    public List<ActivityVo> getHistoryTaskId(String procInstanceId) {
        Task task = taskService.createTaskQuery()
                .processInstanceId(procInstanceId)
                .singleResult();
        String currentKey = task.getTaskDefinitionKey();
        List<HistoricTaskInstance> listHisTask = historyService.createHistoricTaskInstanceQuery().processInstanceId(procInstanceId).list();
        Map<String, Object> map = new HashMap<>();
        for (HistoricTaskInstance historicTaskInstance : listHisTask) {
            String activityId = historicTaskInstance.getTaskDefinitionKey();
            if (activityId.equals(currentKey)) {
                continue;
            }
            String name = historicTaskInstance.getName();
            map.put(activityId, name);
        }
        List list = new ArrayList();
        map.forEach((k, v) -> {
            ActivityVo tmp = new ActivityVo();
            tmp.setId(k);
            tmp.setName(String.valueOf(v));
            list.add(tmp);
        });
        return list;
    }

    /***************************以上根据 任务节点ID 获取流程各对象查询方法********************************/


    /**
     * 部署流程
     */
    @Override
    public ProcessDefinition deployProcess(MultipartFile flowFile) {
        ProcessDefinition processDefinition = null;
        try {

            String fileName = flowFile.getOriginalFilename();
            InputStream fileInputStream = flowFile.getInputStream();
            Deployment deployment;
            String extension = FilenameUtils.getExtension(fileName);
            if (ZIP.equals(extension) || BAR.equals(extension)) {
                ZipInputStream zip = new ZipInputStream(fileInputStream);
                deployment = repositoryService.createDeployment().addZipInputStream(zip).deploy();
            } else {
                deployment = repositoryService.createDeployment().addInputStream(fileName, fileInputStream).deploy();
            }

            String deploymentId = deployment.getId();
            processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId).singleResult();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return processDefinition;
    }

    /**
     * 根据流程key获取最新版本的流程图
     */
    @Override
    public InputStream getFlowImageByKey(String flowKey) {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(flowKey).latestVersion()
                .singleResult();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());
        // 不使用spring请使用下面的两行代码
        ProcessEngineImpl defaultProcessEngine = (ProcessEngineImpl) ProcessEngines.getDefaultProcessEngine();
        Context.setProcessEngineConfiguration(defaultProcessEngine.getProcessEngineConfiguration());

        // 使用spring注入引擎请使用下面的这行代码
        // Context.setProcessEngineConfiguration(processEngine.getProcessEngineConfiguration());
        DefaultProcessDiagramGenerator defaultProcessDiagramGenerator = new DefaultProcessDiagramGenerator();
        return defaultProcessDiagramGenerator.generatePngDiagram(bpmnModel);
    }

    /**
     * 添加批注
     * 当前环节处理人添加批注
     */
    @Override
    public Comment addComment(String taskId, String processInstanceId, String comment) {
        if (StringUtils.isNotBlank(comment)) {
            TaskEntity taskById = findTaskById(taskId);
            String assignee = taskById.getAssignee();
            Authentication.setAuthenticatedUserId(assignee);
            return taskService.addComment(taskId, processInstanceId, comment);
        }
        return null;
    }

    /**
     * 添加批注
     * 实际处理人添加批注
     */
    @Override
    public Comment addComment(String userName, String taskId, String processInstanceId, String comment) {
        if (StringUtils.isNotBlank(comment)) {
            Authentication.setAuthenticatedUserId(userName);
            return taskService.addComment(taskId, processInstanceId, comment);
        }
        return null;
    }

    @Override
    public HistoricTaskInstance getTaskHistoryTaskId(String taskId) {
        return historyService.createHistoricTaskInstanceQuery()
                .taskId(taskId)
                .singleResult();

    }

    @Override
    public Task findTaskByBusinessKey(String businessKey) {
        String processInstanceId = getProcessInstanceIdByBusinessKey(businessKey);
        return findTaskByProcessId(processInstanceId);
    }

    @Override
    public Task findTaskByProcessId(String processInstanceId) {
        return taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
    }

    @Override
    public List<Task> getTasks(String processInstanceId) {
        return taskService.createTaskQuery().processInstanceId(processInstanceId).list();
    }

    @Override
    public List<Task> getTasksByBizKey(String businessKey) {
        String processInstanceId = getProcessInstanceIdByBusinessKey(businessKey);
        return taskService.createTaskQuery().processInstanceId(processInstanceId).list();
    }

    @Override
    public void getGroupUserString(String taskId, TaskVo taskVo) {
        String sql = "select " +
                " GROUP_CONCAT(distinct CONCAT( I.USER_ID_ )) userNames " +
                "from " +
                "  ACT_RU_TASK RES " +
                "left join  " +
                "  ACT_RU_IDENTITYLINK I on " +
                " I.TASK_ID_ = RES.ID_ " +
                "WHERE  " +
                "  I.TASK_ID_ = '" + taskId + "'";
        List<String> userNamesList = iCommonSvc.executeSelectSql2Str(sql);
        if (userNamesList != null && userNamesList.size() > 0) {
            List<String> collect = new ArrayList<>();
            for (String s : userNamesList) {
                if (s != null) {
                    collect.add(s);
                }
            }
            taskVo.setAssignee(collect.get(0));
        }
    }

    @Override
    public String getHisAssigneeByProcessInstanceIdAndActivityId(String processInstanceId, String prevActivityId) {
        String sql = "SELECT t.ASSIGNEE_ " +
                " from ACT_HI_TASKINST t " +
                " where " +
                " t.PROC_INST_ID_='" + processInstanceId + "' and " +
                " t.TASK_DEF_KEY_='" + prevActivityId + "'";
        List<String> list = iCommonSvc.executeSelectSql2Str(sql);
        if (list != null && list.size() > 0) {
            List<String> collect = new ArrayList<>();
            for (String s : list) {
                if (s != null) {
                    collect.add(s);
                }
            }
            return collect.get(0);
        }
        return "admin";
    }

    /**
     * 流程跟踪图信息
     *
     * @param processInstanceId 流程实例ID
     * @return 封装了各种节点信息
     */
    @Override
    public List<Map<String, Object>> traceProcess(String processInstanceId) {
        // his task
        List<HistoricTaskInstance> listHisTask = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .list();
        if (CollectionUtil.isEmpty(listHisTask)) {
            return Collections.emptyList();
        }
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        //执行实例
        String processDefinitionId = historicProcessInstance.getProcessDefinitionId();

        //获得当前任务的所有节点
        ProcessDefinitionEntity def = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(processDefinitionId);
        //rs是指RepositoryService的实例
        List<ActivityImpl> activitiList = def.getActivities();

        List<Map<String, Object>> activityInfos = new ArrayList<>();
        for (ActivityImpl activity : activitiList) {

            String id = activity.getId();

            Map<String, Object> activityInfo = new HashMap<>();

            listHisTask.sort((o1, o2) -> o1.getStartTime().before(o2.getStartTime()) ? 1 : -1);
            // taskId
            for (HistoricTaskInstance hisTask : listHisTask) {
                if (hisTask.getTaskDefinitionKey().equals(id)) {
                    String deleteReason = hisTask.getDeleteReason();
                    if (StringUtils.isBlank(deleteReason)) {
                        deleteReason = "current";
                    } else if ("STOP".equalsIgnoreCase(deleteReason)) {
                        deleteReason = "stop";
                    }
                    String taskId = hisTask.getId();
                    activityInfo.put("taskId", taskId);
                    activityInfo.put("status", deleteReason);
                    activityInfo.put("startTime", hisTask.getStartTime());

                    String assignee = hisTask.getAssignee();
                    if ("current".equals(deleteReason) && StringUtils.isBlank(assignee)) {
                        TaskVo taskVo = new TaskVo();
                        getGroupUserString(taskId, taskVo);
                        activityInfo.put("assignee", taskVo.getAssignee());
                    } else {
                        activityInfo.put("assignee", assignee);
                    }
                    break;
                }
            }

            setPosition(activity, activityInfo);

            // 节点信息
            packageSingleActivitiInfo(activity, activityInfo);

            activityInfos.add(activityInfo);
        }
        activityInfos.sort((o1, o2) -> {
            Integer x1 = (Integer) (o1.get("x"));
            Integer x2 = (Integer) (o2.get("x"));
            return x1 - x2;
        });

        return activityInfos;
    }

    @Override
    public List<Map<String, Object>> traceInfoByBusinessKey(String businessKey) {
        String processInstanceId = getProcessInstanceIdByBusinessKey(businessKey);
        return traceProcess(processInstanceId);
    }

    /**
     * 封装输出信息，包括：当前节点的X、Y坐标、变量信息、任务类型、任务描述
     */
    private Map<String, Object> packageSingleActivitiInfo(ActivityImpl activity, Map<String, Object> activityInfo) {

        // 坐标信息
        setWidthAndHeight(activity, activityInfo);
        // 节点类型
        Map<String, Object> properties = activity.getProperties();
        activityInfo.put("type", properties.get("type"));

        ActivityBehavior activityBehavior = activity.getActivityBehavior();
        log.debug("activityBehavior={}", activityBehavior);

        return activityInfo;
    }

    /**
     * 设置宽度、高度属性
     */
    private void setWidthAndHeight(ActivityImpl activity, Map<String, Object> activityInfo) {
        activityInfo.put("width", activity.getWidth());
        activityInfo.put("height", activity.getHeight());
    }

    /**
     * 设置坐标位置
     */
    private void setPosition(ActivityImpl activity, Map<String, Object> activityInfo) {
        activityInfo.put("x", activity.getX());
        activityInfo.put("y", activity.getY());
    }

    /**
     * 获取当前运行时的人员关系表
     */
    @Override
//    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public List<String> getCandidateUsersByTaskId(String taskId) {
        List<String> candidateUsers = new ArrayList<>();
        try {
            List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
            for (IdentityLink identityLink : identityLinks) {
                candidateUsers.add(identityLink.getUserId());
            }
        } catch (Exception e) {
            log.error(taskId + " exx:", e);
        }
        return candidateUsers;
    }

    /**
     * 修改任务的办理人
     */
    @Override
    public void updateAssigneesByTask(Task task, String[] newAssignees) {
        String taskId = task.getId();
        String originAssignee = task.getAssignee();
        if (StringUtils.isBlank(originAssignee)) {
            log.debug(task.getId() + " e1 " + task.getName());
            List<String> candidateUsers = getCandidateUsersByTaskId(taskId);
            if (candidateUsers.size() > 0) {
                candidateUsers.forEach(candidateUser -> {
                    taskService.deleteCandidateUser(taskId, candidateUser);
                });
            }
        }

        if (newAssignees.length == 1) {
            transferProcess(taskId, newAssignees[0]);
        } else {
            // 添加组任务
            transferProcess(taskId, null);
            addCandidateUser(taskId, newAssignees);
        }
    }
}
