package com.wustzdy.springboot.flowable.demo.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.wustzdy.springboot.flowable.demo.constant.FlowConstants;
import com.wustzdy.springboot.flowable.demo.entity.PimOrderEntity;
import com.wustzdy.springboot.flowable.demo.service.ActTaskService;
import com.wustzdy.springboot.flowable.demo.service.BusinessFlowsProxyInstanceService;
import com.wustzdy.springboot.flowable.demo.util.*;
import com.wustzdy.springboot.flowable.demo.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.util.Strings;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.common.engine.impl.interceptor.CommandExecutor;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.impl.RepositoryServiceImpl;
import org.flowable.engine.impl.RuntimeServiceImpl;
import org.flowable.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.flowable.engine.impl.bpmn.behavior.SequentialMultiInstanceBehavior;
import org.flowable.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.flowable.engine.impl.delegate.ActivityBehavior;
import org.flowable.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Comment;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Expression;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Transactional(rollbackFor = Exception.class)
@Service("actTaskService")
public class ActTaskServiceImpl implements ActTaskService {

    public static final String ADMIN = "admin";

    @Value("${notify.whitelist:}")
    private String whitelist;

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private PimOrderDao pimOrderDao;

    @Autowired
    private OrderUtils orderUtils;

   /* @Autowired
    private QyWeixinService qyWeixinService;*/

    @Autowired
    private BusinessFlowsProxyInstanceService proxyInstanceService;

    @Override
    public String filterByWhitelist(String nextStaff) {
        if (StrUtil.isBlank(nextStaff)) {
            return nextStaff;
        }
        List<String> whitelist = getWhitelist();
        if (CollectionUtil.isEmpty(whitelist)) {
            return nextStaff;
        }
        String[] us = nextStaff.split(",");
        List<String> users = Arrays.asList(us);

        String result = users.stream().filter(e -> whitelist.contains(e)).collect(Collectors.joining(","));
        if (!nextStaff.equals(result)) {
            log.debug(StrUtil.format("origin notify users: {}, after whitelist filter: {}", nextStaff, result));
        }
        return result;
    }

    @Override
    public List<String> filterByWhitelist(List<String> users) {
        if (CollectionUtil.isEmpty(users)) {
            return users;
        }

        List<String> whitelist = getWhitelist();
        if (CollectionUtil.isEmpty(whitelist)) {
            return users;
        }

        List<String> origin = users.stream().distinct().collect(Collectors.toList());

        List<String> result = origin.stream().filter(e -> whitelist.contains(e)).collect(Collectors.toList());
        if (origin.size() != result.size()) {
            log.debug(StrUtil.format("origin notify users: {}, after whitelist filter: {}",
                    String.join(",", users), String.join(",", result)));
        }
        return result;
    }

    @Override
    public List<String> getWhitelist() {
        if (StrUtil.isBlank(whitelist)) {
            return Collections.emptyList();
        }
        String[] wl = whitelist.split(",");
        return Arrays.asList(wl);
    }

    @Override
    public boolean isMpTask(String taskId) {
        if ("59fd1602-2d98-11ec-9015-0a580af42c40".equals(taskId)) {
            return true;
        }
        Task task = taskService.createTaskQuery().taskId(taskId).includeProcessVariables().singleResult();
        if (task == null) {
            return false;
        }
        Map<String, Object> variables = task.getProcessVariables();
        return ActUtils.isMp(variables);
    }

    @Override
    public boolean isMpTask(Task task) {
        Map<String, Object> variables = task.getProcessVariables();
        return ActUtils.isMp(variables);
    }

    private void validTask(String taskId, TaskEntity taskEntity) {
        if (taskEntity == null) {
            HistoricTaskInstance task = historyService.createHistoricTaskInstanceQuery()
                    .taskId(taskId)
                    .singleResult();
            if (task == null) {
                throw new RuntimeException("当前任务流程任务【" + taskId + "】已被处理");
            } else {
                throw new RuntimeException("该流程已被【" + task.getAssignee() + "】-"
                        + DateFormatUtils.format(task.getEndTime(), "yyyy-MM-dd HH:mm:ss") + "处理!");
            }
        }
    }

    public String claimProcess(String taskId, String userId) {
        if (StringUtils.isBlank(userId)) {
            throw new RuntimeException("请指定签收人");
        }
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
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

    public Comment addComment(String taskId, String processInstanceId, String comment) {
        if (StringUtils.isNotBlank(comment)) {
            Task taskById = taskService.createTaskQuery().taskId(taskId).singleResult();
            String assignee = taskById.getAssignee();
            Authentication.setAuthenticatedUserId(assignee);
            return taskService.addComment(taskId, processInstanceId, comment);
        }
        return null;
    }

    public void commitProcess(String taskId, Map<String, Object> variables, String activityId) {

        // 跳转节点为空，默认提交操作
        if (BaseUtils.isEmpty(activityId)) {
            taskService.complete(taskId, variables);
        } else {
            // 流程转向操作
            jumpTask(taskId, activityId, variables);
        }
    }

    private TaskEntity getTaskEntity(String taskId) {
        return (TaskEntity) taskService.createTaskQuery().taskId(taskId).singleResult();
    }

    @Override
    public Boolean isProcessInstanceActive(String processInstanceId) {
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        if (historicProcessInstance == null) {
            return false;
        }
        return null == historicProcessInstance.getEndTime();
    }

    /**
     * 跳转（包括回退和向前）至指定活动节点
     */
    public void jumpTask(String currentTaskId, String targetTaskDefinitionKey, Map<String, Object> variables) {
        jumpTask(getTaskEntity(currentTaskId), targetTaskDefinitionKey, variables);
    }

    /**
     * 跳转（包括回退和向前）至指定活动节点
     * @param currentTaskEntity 当前任务节点
     * @param targetTaskDefinitionKey 目标任务节点（在模型定义里面的节点名称）
     * @throws Exception
     */
    public void jumpTask(TaskEntity currentTaskEntity, String targetTaskDefinitionKey, Map<String, Object> variables) {
        ActivityImpl activity = ProcessDefUtils.getActivity(processEngine, currentTaskEntity.getProcessDefinitionId(),
                targetTaskDefinitionKey);
        jumpTask(currentTaskEntity, activity, variables);
    }

    /**
     * 跳转（包括回退和向前）至指定活动节点
     * @param currentTaskEntity 当前任务节点
     * @param targetActivity 目标任务节点（在模型定义里面的节点名称）
     * @throws Exception
     */
    private void jumpTask(TaskEntity currentTaskEntity, ActivityImpl targetActivity, Map<String, Object> variables) {
        CommandExecutor commandExecutor = ((RuntimeServiceImpl) runtimeService).getCommandExecutor();
        commandExecutor.execute(new JumpTaskCmd(currentTaskEntity, targetActivity, variables));
    }

    @Override
    public ResultVo doPassMpTask(String taskId, String comment, String operation, String userName) {
        ResultVo resultVo = new ResultVo();
        resultVo.setJobStat(FlowConstants.FLOW_STAT_PROCESS);
        TaskEntity taskEntity = (TaskEntity) taskService.createTaskQuery().taskId(taskId).singleResult();
        validTask(taskId, taskEntity);

        // 通过前签收
        claimProcess(taskId, userName);

        // 进行逻辑通过处理
        String processInstanceId = taskEntity.getProcessInstanceId();

        // 添加批注
        Comment addComment = addComment(taskId, processInstanceId, operation + "  " + comment);
        if (Objects.nonNull(addComment)) {
            resultVo.setCommentId(addComment.getId());
        }

        // 完成当前任务
        commitProcess(taskId, null, null);

        // 判断流程是否结束
        if (!isProcessInstanceActive(processInstanceId)) {
            // 流程结束，返回结束信息
            resultVo.setJobStat(FlowConstants.FLOW_STAT_END);
            resultVo.setAssignee(FlowConstants.NO_BODY);
        }

        resultVo.setProcessInstanceId(processInstanceId);
        return resultVo;
    }

    @Override
    public ResultVo startMpProcess(String processDefinitionKey, String assignee, String businessKey, Map<String, Object> vars) {

        Authentication.setAuthenticatedUserId(assignee);

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, vars);

        String processInstanceId = processInstance.getId();
        String processDefinitionId = processInstance.getProcessDefinitionId();
        Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).active().singleResult();
        String taskId = task.getId();
        String taskDefinitionKey = task.getTaskDefinitionKey();

        // 指定到人
        taskService.claim(taskId, assignee);

        // result
        ResultVo resultVo = new ResultVo();
        resultVo.setProcessDefinitionKey(processDefinitionKey);
        resultVo.setTaskId(taskId);
        resultVo.setAssignee(assignee);
        resultVo.setBusinessKey(businessKey);
        resultVo.setProcessInstanceId(processInstanceId);
        resultVo.setJobStat(FlowConstants.FLOW_STAT_PROCESS);
        resultVo.setProcessDefinitionId(processDefinitionId);
        resultVo.setProcessDefinitionKey(taskDefinitionKey);

        return resultVo;
    }


    @Override
    public List<String> currentTasksAssigneOrCandidates(String processInstanceId) {
        List<String> users = new ArrayList<>();
        List<Task> tasks = getTasks(processInstanceId);
        for (Task task : tasks) {
            String assignee = task.getAssignee();
            if (assignee == null) {
                List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(task.getId());
                for (IdentityLink identityLink : identityLinks) {
                    users.add(identityLink.getUserId());
                }
            } else {
                users.add(assignee);
            }
        }

        return users.stream().distinct().collect(Collectors.toList());
    }


    @Override
    public Task getTask(String taskId) {
        return taskService.createTaskQuery().taskId(taskId).singleResult();
    }
    @Override
    public List<Task> getTasks(String processInstanceId) {
        return taskService.createTaskQuery().processInstanceId(processInstanceId).list();
    }


    @Override
    public List<TaskVo> currentTasks(List<Task> tasks, boolean isNeedEndVo) {
        List<TaskVo> taskVos = new ArrayList<>();

        if (CollectionUtil.isEmpty(tasks)) {
            if (isNeedEndVo) {
                TaskVo taskVo = new TaskVo();
                taskVo.setAssignee("流程已结束");
                taskVo.setActivityId("end");
                taskVos.add(taskVo);
            }
        } else {
            for (Task task : tasks) {
                TaskVo taskVo = new TaskVo();
                String assignee = task.getAssignee();
                if (assignee == null) {
                    List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(task.getId());
                    taskVo.setAssignee(identityLinks.stream().map(IdentityLink::getUserId).collect(Collectors.joining(",")));
                } else {
                    taskVo.setAssignee(assignee);
                }
                taskVo.setActivityId(task.getTaskDefinitionKey());
                taskVo.setCreateTime(task.getCreateTime()); // fixme 从历史task拿结束时间
                taskVo.setProcessDefinitionId(task.getProcessDefinitionId());
                taskVo.setTaskId(task.getId());
                taskVo.setName(task.getName());
                taskVos.add(taskVo);
            }
        }
        return taskVos;
    }




    @Override
    public R urgeByProcInsId(String processInstanceId, String username) {
        PimOrderEntity entity = pimOrderDao.getByProcessInstanceId(processInstanceId);
        if (!Optional.ofNullable(entity).isPresent()){
            return R.error("无此工单");
        }

        List<String> users = currentTasksAssigneOrCandidates(processInstanceId);
        if (CollectionUtil.isEmpty(users)) {
            log.warn("当前任务没有审批人");
            return R.error("当前任务没有审批人");
        }

        users = filterByWhitelist(users);
        if (CollectionUtil.isNotEmpty(users)) {
            String nextStaff = users.stream().collect(Collectors.joining(","));
            orderUtils.formatUrgeMailContent(nextStaff, username, "您有1条工单被申请人催促办理，请您及时处理", entity);
//            qyWeixinService.notificationV2(entity, nextStaff);
            return R.ok().put("urgeUsers", nextStaff);
        } else {
            log.warn("白名单过滤后没有审批人需要通知");
            return R.ok("白名单过滤后没有审批人需要通知");
        }
    }

    @Override
    public R currentTasksByProcInsId(String processInstanceId) {
        List<Task> tasks = getTasks(processInstanceId);
        if (CollectionUtil.isEmpty(tasks)) {
            return R.error("flow close: " + processInstanceId);
        }

        List<TaskVo> taskVos = new ArrayList<>();
        for (Task task : tasks) {
            TaskVo taskVo = new TaskVo();
            taskVo.setTaskId(task.getId());
            taskVo.setActivityId(task.getTaskDefinitionKey());
            taskVo.setName(task.getName());
            taskVo.setCreateTime(task.getCreateTime());

            String assignee = task.getAssignee();
            if (assignee == null) {
                List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(task.getId());
                taskVo.setAssignee(identityLinks.stream().map(IdentityLink::getUserId).collect(Collectors.joining(",")));
            } else {
                taskVo.setAssignee(assignee);
            }
            taskVos.add(taskVo);
        }
        return R.ok(taskVos);

    }

    private List<CommentVo> getCommentVos(List<Comment> commentList) {
        commentList.sort((o1, o2) -> o1.getTime().after(o2.getTime()) ? 1 : -1);
        return commentList.stream().map(e -> {
            String userId = StringUtils.isBlank(e.getUserId()) ? ADMIN : e.getUserId();
            String fullMessage = e.getFullMessage();
            Date time = e.getTime();
            CommentVo commentVo = new CommentVo();
            commentVo.setCommentId(e.getId());
            commentVo.setCommentContent(fullMessage);
            commentVo.setCommentTime(time);
            commentVo.setCommentUser(userId);
            commentVo.setTaskId(e.getTaskId());
            HistoricTaskInstance taskInstance = historyService.createHistoricTaskInstanceQuery()
                    .taskId(e.getTaskId())
                    .singleResult();
            commentVo.setTaskName(taskInstance.getName());
            commentVo.setTaskDefinitionKey(taskInstance.getTaskDefinitionKey());
            return commentVo;
        }).collect(Collectors.toList());
    }

    @Override
    public R historicFlowList(String procInsId){

        List<Comment> comments = taskService.getProcessInstanceComments(procInsId);
        System.out.println("comments size " + comments.size());
        List<CommentVo> commentVoList = getCommentVos(comments);

        // 代理审批
        for (CommentVo commentVo: commentVoList) {
            String newCommentUser = proxyInstanceService.getProxyApprover(commentVo.getCommentUser(), commentVo.getTaskId(), commentVo.getCommentId());
            if (Strings.isNotBlank(newCommentUser)) {
                commentVo.setCommentUser(newCommentUser);
            }
        }

        List<Task> tasks = getTasks(procInsId);
        List<TaskVo> taskVos = currentTasks(tasks, false);


        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(procInsId)
                .singleResult();
        if (historicProcessInstance == null) {
            return R.error("not found: " + procInsId);
        }
        //执行实例
        String processDefinitionId = historicProcessInstance.getProcessDefinitionId();

        //获得当前任务的所有节点
        ProcessDefinitionEntity def = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(processDefinitionId);
        //rs是指RepositoryService的实例
        List<ActivityImpl> activitiList = def.getActivities();
        Map<String, ActivityImpl> activityMap = activitiList.stream().collect(Collectors.toMap(ActivityImpl::getId, ActivityImpl->ActivityImpl));

        List<String> currentActIds = taskVos.stream().map(TaskVo::getActivityId).distinct().collect(Collectors.toList());

//		Map<String, Comment> commentMap = comments.stream().collect(Collectors.toMap(Comment::getId, Comment->Comment));

        // multi instance parallel merge to current parallel tasks, including those transferred task
        List<TaskCommentVo> mergeCompletedTask = new ArrayList<>();
        for (String actId : currentActIds) {
            if ("end".equals(actId)) {
                continue;
            }
            ActivityImpl activity = activityMap.get(actId);
            assert activity != null : "Act not found";
            Map<String, Object> properties = activity.getProperties();
            String multiInstance = (String) properties.get("multiInstance");
            if ("parallel".equals(multiInstance)) {
                Iterator<CommentVo> it = commentVoList.iterator();
                while (it.hasNext()) {
                    CommentVo commentVo = it.next();
                    if (actId.equals(commentVo.getTaskDefinitionKey())) {
                        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery()
                                .taskId(commentVo.getTaskId())
                                .singleResult();
                        assert historicTaskInstance != null : "historic task not found:" + commentVo.getTaskId();
                        TaskCommentVo taskVo = new TaskCommentVo();
                        taskVo.setTaskId(commentVo.getTaskId());
                        taskVo.setActivityId(commentVo.getTaskDefinitionKey());
                        taskVo.setAssignee(commentVo.getCommentUser());
                        taskVo.setName(commentVo.getTaskName());
                        taskVo.setCreateTime(historicTaskInstance.getCreateTime());
                        taskVo.setEndTime(commentVo.getCommentTime());
                        String deleteReason = historicTaskInstance.getDeleteReason();
                        if (StringUtils.isBlank(deleteReason)) {
                            deleteReason = "current";
                        } else if (deleteReason.equals("STOP")) {
                            deleteReason = "stop";
                        }
                        taskVo.setDeleteReason(deleteReason);
                        taskVo.setTaskDefinitionKey(actId);

                        //
                        taskVo.setCommentId(commentVo.getCommentId());
                        taskVo.setCommentUser(commentVo.getCommentUser());
                        taskVo.setCommentContent(commentVo.getCommentContent());
                        taskVo.setCommentTime(commentVo.getCommentTime());
                        taskVo.setTaskName(commentVo.getTaskName());

                        mergeCompletedTask.add(taskVo);
                        it.remove();
                    }
                }
            }
        }

        mergeCompletedTask.sort((o1, o2) -> o1.getEndTime().after(o2.getEndTime()) ? 1 : -1);

        List<TaskCommentVo> taskCompleted = new ArrayList<>();
        for (CommentVo commentVo : commentVoList) {
            HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery()
                    .taskId(commentVo.getTaskId())
                    .singleResult();
            TaskCommentVo taskVo = new TaskCommentVo();
            taskVo.setTaskId(commentVo.getTaskId());
            taskVo.setActivityId(commentVo.getTaskDefinitionKey());
            taskVo.setAssignee(commentVo.getCommentUser());
            taskVo.setName(commentVo.getTaskName());
            taskVo.setCreateTime(historicTaskInstance.getCreateTime());
            taskVo.setEndTime(commentVo.getCommentTime());
            if (historicTaskInstance != null) {
                String deleteReason = historicTaskInstance.getDeleteReason();
                if (StringUtils.isBlank(deleteReason)) {
                    deleteReason = "current";
                } else if (deleteReason.equals("STOP")) {
                    deleteReason = "stop";
                }
                taskVo.setDeleteReason(deleteReason);
            }
            taskVo.setTaskDefinitionKey(commentVo.getTaskDefinitionKey());

            //
            taskVo.setCommentId(commentVo.getCommentId());
            taskVo.setCommentUser(commentVo.getCommentUser());
            taskVo.setCommentContent(commentVo.getCommentContent());
            taskVo.setCommentTime(commentVo.getCommentTime());
            taskVo.setTaskName(commentVo.getTaskName());

            taskCompleted.add(taskVo);
        }

        // TODO common parallel gateway , merge parallel branch tasks already completed

        FlowHistoryVo flowHistoryVo = new FlowHistoryVo();
        flowHistoryVo.setCompleted(taskCompleted);
        flowHistoryVo.setCompletedMP(mergeCompletedTask);
        flowHistoryVo.setCurrent(taskVos);

        return R.ok(flowHistoryVo);
    }

    @Override
    public TraceProcessVo traceProcessNew(String processInstanceId) throws Exception {

        List<HistoricTaskInstance> listHisTask = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .list();

        Map<String, List<Map<String, Object>>> hisTasks = new HashMap<>();
        for (HistoricTaskInstance historicTaskInstance : listHisTask) {

            Map<String, Object> activityInfo = new HashMap<>();
            String deleteReason = historicTaskInstance.getDeleteReason();
            if (StringUtils.isBlank(deleteReason)) {
                deleteReason = "current";
            } else if (deleteReason.equals("STOP")) {
                deleteReason = "stop";
            }
            String taskId = historicTaskInstance.getId();
            activityInfo.put("taskId", taskId);
            activityInfo.put("status", deleteReason);
            activityInfo.put("startTime", historicTaskInstance.getStartTime());
            activityInfo.put("hisTask", true);

            String assignee = historicTaskInstance.getAssignee();
            if (deleteReason.equals("current") && StringUtils.isBlank(assignee)) {
                List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
                activityInfo.put("assignee", identityLinks.stream().map(IdentityLink::getUserId).collect(Collectors.joining(",")));
            } else {
                activityInfo.put("assignee", assignee);
            }
            String taskDefinitionKey = historicTaskInstance.getTaskDefinitionKey();
            if (hisTasks.get(taskDefinitionKey) == null) {
                List<Map<String, Object>> tasks = new ArrayList<>();
                tasks.add(activityInfo);
                hisTasks.put(taskDefinitionKey, tasks);
            } else {
                hisTasks.get(taskDefinitionKey).add(activityInfo);
            }
        }

        String activityId = null;
        Execution execution = runtimeService.createExecutionQuery().executionId(processInstanceId).singleResult();//执行实例
        if (execution != null) {
            activityId = execution.getActivityId();
            if (activityId == null) {
                log.info("the execution is not a leaf execution");
            }
        }

//		ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId)
//				.singleResult();
//		ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)


//				.getDeployedProcessDefinition(processInstance.getProcessDefinitionId());

        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        //执行实例
        String processDefinitionId = historicProcessInstance.getProcessDefinitionId();

        //获得当前任务的所有节点
        ProcessDefinitionEntity def = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(processDefinitionId);


        List<ActivityImpl> activitiList = def.getActivities();//获得当前任务的所有节点

        List<Map<String, Object>> activityInfos = new ArrayList<Map<String, Object>>();
        Map<String, List<Map<String, Object>>> taskDefKeyMap = new HashMap<>();


        ActivityImpl activityStartEvent = null;
        for (ActivityImpl activity : activitiList) {

            Map<String, Object> properties = activity.getProperties();
            String type = (String)properties.get("type");

            // 不传网关到前端，以免前端出错
            if ("exclusiveGateway".equals(type)) {
                continue;
            }

            boolean currentActiviti = false;
            String id = activity.getId();

            // 当前节点
            if (id.equals(activityId)) {
                currentActiviti = true;
            }

            List<Map<String, Object>> activityImageInfoList = packageSingleActivitiInfoNew(activity, currentActiviti, hisTasks);
            taskDefKeyMap.put(id, activityImageInfoList);

            for (Map<String, Object> activityImageInfo : activityImageInfoList) {
                activityInfos.add(activityImageInfo);
            }

            if ("startEvent".equals(type)) {
                activityStartEvent = activity;
            }
        }

        activityInfos.sort((o1, o2) -> {
            Integer x1 = (Integer) (o1.get("x"));
            Integer x2 = (Integer) (o2.get("x"));
            return x1 - x2;
        });

        Assert.notNull(activityStartEvent, "没有开始事件");

        Map<String, List<String>> flowMap = new HashMap<>();
        collectFlow(activityStartEvent, flowMap);

        TraceProcessVo traceProcessVo = new TraceProcessVo();
        traceProcessVo.setStartId(activityStartEvent.getId());
        traceProcessVo.setFlowMap(flowMap);
        traceProcessVo.setTaskDefKeyMap(taskDefKeyMap);
        traceProcessVo.setActivityInfos(activityInfos);
        return traceProcessVo;



//		activityStartEvent.getOutgoingTransitions()

//		return activityInfos;
    }

    private List<Map<String, Object>> packageSingleActivitiInfoNew(ActivityImpl activity,
                                                                   boolean currentActiviti, Map<String, List<Map<String, Object>>> hisTasks) throws Exception {

        String activityId = activity.getId();
        List<Map<String, Object>> sameActTasks = hisTasks.get(activityId);
        List<Map<String, Object>> resultTasks;
        if (sameActTasks != null) {
            resultTasks = sameActTasks;
        } else {
            resultTasks = new ArrayList<>();
            resultTasks.add(new HashMap<>());
        }

        ActivityBehavior activityBehavior = activity.getActivityBehavior();
        log.debug("activityBehavior={}", activityBehavior);

        for (Map<String, Object> activityInfo : resultTasks) {
            activityInfo.put("currentActiviti", currentActiviti);
            setPosition(activity, activityInfo);
            setWidthAndHeight(activity, activityInfo);
            Map<String, Object> properties = activity.getProperties();
            activityInfo.put("actName", properties.get("name"));
            activityInfo.put("type", properties.get("type"));

            activityInfo.put("actDoc", properties.get("documentation"));
            String description = activity.getProcessDefinition().getDescription();
            activityInfo.put("actDescription", description);


            if (activityBehavior instanceof UserTaskActivityBehavior) {

                // 当前任务的分配角色
                UserTaskActivityBehavior userTaskActivityBehavior = (UserTaskActivityBehavior) activityBehavior;
                TaskDefinition taskDefinition = userTaskActivityBehavior.getTaskDefinition();

                Set<Expression> candidateGroupIdExpressions = taskDefinition.getCandidateGroupIdExpressions();
                if (!candidateGroupIdExpressions.isEmpty()) {
                    // 任务的处理角色
                    setTaskGroup(activityInfo, candidateGroupIdExpressions);
                }
            }

            if (activityBehavior instanceof ParallelMultiInstanceBehavior) {
                activityInfo.put("behavior", "ParallelMultiInstance");
            } else if (activityBehavior instanceof SequentialMultiInstanceBehavior) {
                activityInfo.put("behavior", "SequentialMultiInstance");
            }
        }

        return resultTasks;
    }

    private void setWidthAndHeight(ActivityImpl activity, Map<String, Object> activityInfo) {
        activityInfo.put("width", activity.getWidth());
        activityInfo.put("height", activity.getHeight());
    }

    private void setPosition(ActivityImpl activity, Map<String, Object> activityInfo) {
        activityInfo.put("x", activity.getX());
        activityInfo.put("y", activity.getY());
    }

    private void collectFlow(ActivityImpl activity, Map<String, List<String>> flowMap) {
        String id = activity.getId();
        List<PvmTransition> outTransitions = activity.getOutgoingTransitions();
        collectFlow(id, outTransitions, flowMap);
    }

    private void collectFlow(String activityId, List<PvmTransition> outTransitions, Map<String, List<String>> flowMap) {
        if (CollectionUtil.isEmpty(outTransitions)) {
            return;
        }
        for (PvmTransition tr : outTransitions) {
            PvmActivity ac = tr.getDestination();
//			String type = ac.getProperty("type").toString();
            List<PvmTransition> outTransitionsTemp = ac.getOutgoingTransitions();

            if (flowMap.get(activityId) == null) {
                List<String> acids = new ArrayList<>();
                acids.add(ac.getId());
                flowMap.put(activityId, acids);
            } else {
                flowMap.get(activityId).add(ac.getId());
            }
            collectFlow(ac.getId(), outTransitionsTemp, flowMap);

        }
    }

    /**
     * 设置任务组
     * @param vars
     * @param candidateGroupIdExpressions
     */
    private void setTaskGroup(Map<String, Object> vars, Set<Expression> candidateGroupIdExpressions) {
        String roles = "";
        for (Expression expression : candidateGroupIdExpressions) {
            String expressionText = expression.getExpressionText();
            String roleName = expressionText;
            roles += roleName;
        }
        vars.put("actTaskRoles", roles);
    }

}
