package com.wustzdy.springboot.flowable.demo.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.wustzdy.springboot.flowable.demo.constant.FlowConstants;
import com.wustzdy.springboot.flowable.demo.dao.PimOrderDao;
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

@SuppressWarnings("all")
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
//        jumpTask(getTaskEntity(currentTaskId), targetTaskDefinitionKey, variables);
    }

    /**
     * 跳转（包括回退和向前）至指定活动节点
     * @param currentTaskEntity 当前任务节点
     * @param targetTaskDefinitionKey 目标任务节点（在模型定义里面的节点名称）
     * @throws Exception
     */
   /* public void jumpTask(TaskEntity currentTaskEntity, String targetTaskDefinitionKey, Map<String, Object> variables) {
        ActivityImpl activity = ProcessDefUtils.getActivity(processEngine, currentTaskEntity.getProcessDefinitionId(),
                targetTaskDefinitionKey);
        jumpTask(currentTaskEntity, activity, variables);
    }*/

    /**
     * 跳转（包括回退和向前）至指定活动节点
     *
     * @param currentTaskEntity 当前任务节点
     * @param targetActivity    目标任务节点（在模型定义里面的节点名称）
     * @throws Exception
     */
    /*private void jumpTask(TaskEntity currentTaskEntity, ActivityImpl targetActivity, Map<String, Object> variables) {
        CommandExecutor commandExecutor = ((RuntimeServiceImpl) runtimeService).getCommandExecutor();
        commandExecutor.execute(new JumpTaskCmd(currentTaskEntity, targetActivity, variables));
    }*/
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
        if (!Optional.ofNullable(entity).isPresent()) {
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

}
