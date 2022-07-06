package com.wustzdy.springboot.flowable.demo.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.wustzdy.springboot.flowable.demo.service.IActivitiUtilSvc;
import com.wustzdy.springboot.flowable.demo.service.IHandlerTaskSvc;
import com.wustzdy.springboot.flowable.demo.util.BaseUtils;
import com.wustzdy.springboot.flowable.demo.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Comment;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.omg.CORBA.SystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static cn.hutool.core.util.StrUtil.C_COMMA;
import static com.wustzdy.springboot.flowable.demo.constant.FlowConstants.*;
import static com.wustzdy.springboot.flowable.demo.service.impl.ActTaskServiceImpl.ADMIN;
import static org.apache.logging.log4j.core.util.Patterns.COMMA_SEPARATOR;


@Slf4j
@Service("iHandlerTaskSvc")
@Transactional(rollbackFor = Exception.class)
public class HandlerTaskSvcImpl implements IHandlerTaskSvc {


    @Autowired
    private IActivitiUtilSvc iActivitiUtilSvc;

    /**
     * 签收
     */
    @Override
    public String doClaimTask(String taskId, String assignee) {

        return iActivitiUtilSvc.claimProcess(taskId, assignee);
    }

    /**
     * 审批通过
     */
    @Override
    public ResultVo doPassProcessInstanceIdTask(String processInstanceId, String comment, String nextTaskAndStaffId, String operation, String userName) {
        Task task = iActivitiUtilSvc.findTaskByProcessId(processInstanceId);
        Assert.notNull(task, "该任务已被其他人处理");
        return this.doPassTask(task.getId(), comment, nextTaskAndStaffId, operation, userName);
    }

    /**
     * 审批通过
     *
     * @return
     */
    @Override
    public ResultVo doPassTask(String taskId, String comment, String nextTaskAndStaffIds,
                               String operation, String userName) {
        ResultVo resultVo = new ResultVo();
        resultVo.setJobStat(FLOW_STAT_PROCESS);
        TaskEntity taskEntity = (TaskEntity) iActivitiUtilSvc.findTaskById(taskId);
        validTask(taskId, taskEntity);

        // 通过前签收
        iActivitiUtilSvc.claimProcess(taskId, userName);

        // 进行逻辑通过处理
        String actId = taskEntity.getTaskDefinitionKey();
        String processInstanceId = taskEntity.getProcessInstanceId();
        List<String> nextActivityIds = getNextTaskIds(processInstanceId);
        resultVo.setNextActivityIds(nextActivityIds);
        // 添加批注
        Comment addComment = iActivitiUtilSvc.addComment(taskId, processInstanceId, operation + "  " + comment);
        if (Objects.nonNull(addComment)) {
            resultVo.setCommentId(addComment.getId());
        } else {
            resultVo.setCommentId("");
        }
        //前台获取用户信息后
        Map<String, String> taskAndStaffMap = new HashMap<>();
        if (!BaseUtils.isEmpty(nextTaskAndStaffIds)) {
            // 格式 userTask1~userA,userB:userTask2~userC,userD
            String[] nextJobAndStaffArray = nextTaskAndStaffIds.split(":");

            for (String s : nextJobAndStaffArray) {
                String[] stract = s.split("~");
                if (stract.length == 2) {
                    // 任务task+user
                    taskAndStaffMap.put(stract[0], stract[1]);
                }
            }
        }
        // 返回结果处理人员信息
        if (taskAndStaffMap.size() > 0) {
            String collect = String.join(",", taskAndStaffMap.values());
            resultVo.setAssignee(collect);
        } else {
            resultVo.setAssignee(nextTaskAndStaffIds);
        }
        // 获取节点条件，调用activiti流转
        Map<String, Object> variables = new HashMap<>();
        Object[] selectJobs = taskAndStaffMap.keySet().toArray();
        List<String> allJobIds = iActivitiUtilSvc.getNextJobs(processInstanceId, actId, "string");
        for (Object selectJob : selectJobs) {
            variables.put(selectJob.toString(), true);
            allJobIds.remove(selectJob.toString());
        }
        for (String unSelectJob : allJobIds) {
            variables.put(unSelectJob, false);
        }
        // 完成当前任务
        iActivitiUtilSvc.commitProcess(taskId, variables, null);

        // 判断流程是否结束
        if (BaseUtils.isEmpty(iActivitiUtilSvc.getEndActivityId(processInstanceId))) {
            List<Task> nextTaskList = new ArrayList<>();
            // 查询当前运行的任务节点
            List<Task> taskList = iActivitiUtilSvc.findTaskList(processInstanceId);
            // 取流程图上下一节点ID集合
            List nextJobIds = iActivitiUtilSvc.getNextJobs(processInstanceId, actId, "string");
            for (Task task : taskList) {
                String activityId = task.getTaskDefinitionKey();
                // 只取交集
                if (nextJobIds.contains(activityId)) {
                    nextTaskList.add(task);
                }
            }

            if (nextTaskList.size() > 1) {
                // 并发任务
                // 循环为并发节点插入工单信息
                for (Task task : nextTaskList) {
                    claimProcessOrAddCandidateUser(nextTaskAndStaffIds, taskAndStaffMap, task);
                }
            } else if (nextTaskList.size() == 1) {
                // 单一节点
                Task task = nextTaskList.get(0);
                //获取下一步操作的人员信息
                resultVo.setNextTaskId(task.getId());
                claimProcessOrAddCandidateUser(nextTaskAndStaffIds, taskAndStaffMap, task);
            } else {
                throw new RuntimeException("Please choose the next step to deal with the problem.");
            }
        } else {
            // 流程结束，返回结束信息
            resultVo.setJobStat(FLOW_STAT_END);
            resultVo.setAssignee(NO_BODY);
        }
        resultVo.setProcessInstanceId(processInstanceId);
        return resultVo;
    }

    /**
     * 单人直接签收，多人分配到组任务
     */
    private void claimProcessOrAddCandidateUser(String nextTaskAndStaffIds,
                                                Map<String, String> taskAndStaffMap,
                                                Task task) {
        //获取下一步操作的人员信息
        String nextStaffIds = taskAndStaffMap.get(task.getTaskDefinitionKey());
        if (StringUtils.isBlank(nextStaffIds)) {
            // 没有任务，则默认执行人
            nextStaffIds = nextTaskAndStaffIds;
        }

        // 判断人是否多人，多人则归为是组任务
        String[] nextStaffIdArr = nextStaffIds.split(COMMA_SEPARATOR);
        if (nextStaffIdArr.length == 1) {
            //下一环节指定接收人
            iActivitiUtilSvc.claimProcess(task.getId(), nextStaffIdArr[0]);
        } else {
            // 组任务
            iActivitiUtilSvc.addCandidateUser(task.getId(), nextStaffIdArr);
        }
    }

    /**
     * 回退流程
     */
    @Override
    public ResultVo doBackTask(String processInstanceId, String prevActivityId, String comment, String userName) {
        String assignee;
        try {
            TaskEntity taskEntity = (TaskEntity) iActivitiUtilSvc.findTaskByProcessId(processInstanceId);
            validTask(taskEntity);
            String taskId = taskEntity.getId();
            // 回退前签收
            iActivitiUtilSvc.claimProcess(taskId, userName);

            processInstanceId = taskEntity.getProcessInstanceId();
            // 添加批注
            iActivitiUtilSvc.addComment(taskId, processInstanceId, "回退流程  " + comment);

            Map<String, Object> variables = new HashMap<>();
            //回退环节ID prevActivityId
            iActivitiUtilSvc.commitProcess(taskId, variables, prevActivityId);

            // 回退后签收
            assignee = iActivitiUtilSvc.getHisAssigneeByProcessInstanceIdAndActivityId(processInstanceId, prevActivityId);
            TaskEntity newTaskEntity = (TaskEntity) iActivitiUtilSvc.findTaskByProcessId(processInstanceId);
            iActivitiUtilSvc.claimProcess(newTaskEntity.getId(), assignee);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        ResultVo resultVo = new ResultVo();
        resultVo.setJobStat(FLOW_STAT_BACK);
        resultVo.setAssignee(assignee);
        resultVo.setProcessInstanceId(processInstanceId);
        // setNextActivityIds

        return resultVo;
    }

    @Override
    public ResultVo doBackTaskByTaskId(String taskId, String prevActivityId, String comment, String userName) {
        String assignee;
        String processInstanceId;
        try {
            ProcessInstance processInstance = iActivitiUtilSvc.findProcessInstanceByTaskId(taskId);
            processInstanceId = processInstance.getProcessInstanceId();
            // 回退前签收
            iActivitiUtilSvc.claimProcess(taskId, userName);
            // 添加批注
            iActivitiUtilSvc.addComment(taskId, processInstanceId, "回退流程  " + comment);

            Map<String, Object> variables = new HashMap<>();
            //回退环节ID prevActivityId
            iActivitiUtilSvc.commitProcess(taskId, variables, prevActivityId);

            // 回退后签收
            assignee = iActivitiUtilSvc.getHisAssigneeByProcessInstanceIdAndActivityId(processInstanceId, prevActivityId);
            TaskEntity newTaskEntity = (TaskEntity) iActivitiUtilSvc.findTaskByProcessId(processInstanceId);
            iActivitiUtilSvc.claimProcess(newTaskEntity.getId(), assignee);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException("回退流程失败，请检查回退流程prevActivityId是否正确！");
        }
        ResultVo resultVo = new ResultVo();
        resultVo.setJobStat(FLOW_STAT_BACK);
        resultVo.setAssignee(assignee);
        resultVo.setProcessInstanceId(processInstanceId);
        // setNextActivityIds

        return resultVo;
    }

    @Override
    public String getNextTaskTypeByTaskId(String taskId) {
        ProcessInstance processInstance = iActivitiUtilSvc.findProcessInstanceByTaskId(taskId);
        String processInstanceId = processInstance.getProcessInstanceId();
        return getNextTaskType(processInstanceId);
    }

    @Override
    public List<ActivityVo> getReturnBackTaskActivityIdByTaskId(String taskId) {
        ProcessInstance processInstance = iActivitiUtilSvc.findProcessInstanceByTaskId(taskId);
        String processInstanceId = processInstance.getProcessInstanceId();
        return getReturnBackTaskActivityId(processInstanceId);
    }

    @Override
    public List<Map<String, Object>> traceProcessByTaskId(String taskId) throws Exception {
        ProcessInstance processInstance = iActivitiUtilSvc.findProcessInstanceByTaskId(taskId);
        String processInstanceId = processInstance.getProcessInstanceId();
        return traceProcess(processInstanceId);
    }

    @Override
    public ResultVo addCandidate(String taskId, String assignee, String comment, String userName) {
        Task task = iActivitiUtilSvc.findTaskById(taskId);
        String processInstanceId = task.getProcessInstanceId();
        Assert.notNull(task, "该任务已被其他人处理");
        Comment addComment = null;
        // 添加批注
        if (StringUtils.isNotBlank(comment)) {
            addComment = iActivitiUtilSvc.addComment(userName, taskId, processInstanceId, comment);
        }
//        // 候选人
//        List<String> assignees = StrUtil.split(assignee, C_COMMA);
//        // 环节处理人
//        String taskAssignee = task.getAssignee();
//        if (taskAssignee != null && !assignees.contains(taskAssignee)) {
//            assignees.add(taskAssignee);
//        }
//        // 去重复
//        List<String> collect = assignees.stream().distinct().collect(Collectors.toList());
        List<String> addAssigneeList = getUniqueAssigneeList(task, assignee);
        // 添加组任务
        iActivitiUtilSvc.transferProcess(taskId, null);
        iActivitiUtilSvc.addCandidateUser(taskId, ArrayUtil.toArray(addAssigneeList, String.class));

        ResultVo resultVo = new ResultVo();
        if (Objects.nonNull(addComment)) {
            resultVo.setCommentId(addComment.getId());
        } else {
            resultVo.setCommentId("");
        }
        resultVo.setJobStat(FLOW_STAT_PROCESS);
        resultVo.setAssignee(StringUtils.join(addAssigneeList, C_COMMA));
        resultVo.setProcessInstanceId(processInstanceId);

        return resultVo;
    }

    private List<String> getUniqueAssigneeList(Task task, String assignee) {
        List<String> newAssigneeList = StrUtil.split(assignee, C_COMMA);
        newAssigneeList = newAssigneeList.stream().distinct().collect(Collectors.toList()); // 去重复
        String taskAssignee = task.getAssignee();
        if (Objects.nonNull(taskAssignee)) {
            if (!newAssigneeList.contains(taskAssignee)) {
                newAssigneeList.add(taskAssignee);
            }
        } else {
            log.debug(task.getId() + " e2 " + task.getName());
            List<String> originAssigneeList = iActivitiUtilSvc.getCandidateUsersByTaskId(task.getId());
            newAssigneeList.removeAll(originAssigneeList);
        }
        return newAssigneeList;
    }

    /**
     * 关闭工单
     */
    @Override
    public ResultVo closeProcess(String assignee, String processInstanceId, String comment) {
        List<Task> tasks = iActivitiUtilSvc.getTasks(processInstanceId);
        if (CollectionUtil.isEmpty(tasks)) {
            ResultVo resultVo = new ResultVo();
            resultVo.setJobStat(FLOW_STAT_CLOSE);
            resultVo.setProcessInstanceId(processInstanceId);
            resultVo.setAssignee(assignee);
            return resultVo;
        }

        if (tasks.size() == 1) {
            Task task = tasks.get(0);
            String taskId = task.getId();
            // 添加批注
            Comment addComment = iActivitiUtilSvc.addComment(assignee, taskId, processInstanceId, "关闭工单  " + comment);

            String activityId = iActivitiUtilSvc.getActivityIdByProcessInstanceId(processInstanceId);
            // 删除运行中的实例
            iActivitiUtilSvc.stopProcess(processInstanceId, FLOW_STAT_CLOSE, activityId);
            ResultVo resultVo = new ResultVo();
            if (Objects.nonNull(addComment)) {
                resultVo.setCommentId(addComment.getId());
            }
            resultVo.setTaskId(taskId);
            resultVo.setJobStat(FLOW_STAT_CLOSE);
            resultVo.setProcessInstanceId(processInstanceId);
            resultVo.setAssignee(assignee);
            return resultVo;
        }

        ResultVo resultVo = new ResultVo();
        for (Task task : tasks) {
            String taskId = task.getId();
            // 添加批注
            Comment addComment = iActivitiUtilSvc.addComment(assignee, taskId, processInstanceId, "关闭工单  " + comment);
            if (resultVo.getCommentId() == null) {
                resultVo.setCommentId(addComment.getId());
            }
        }

        String activityId = tasks.get(0).getTaskDefinitionKey();
        // 删除运行中的实例
        iActivitiUtilSvc.stopProcess(processInstanceId, FLOW_STAT_CLOSE, activityId);

        resultVo.setTaskId(tasks.get(0).getId());
        resultVo.setJobStat(FLOW_STAT_CLOSE);
        resultVo.setProcessInstanceId(processInstanceId);
        resultVo.setAssignee(assignee);
        return resultVo;
    }

    @Override
    public ResultVo deleteProcess(String processInstanceId) {
        iActivitiUtilSvc.deleteProcess(processInstanceId);
        ResultVo resultVo = new ResultVo();
        resultVo.setJobStat(FLOW_STAT_DELETE);
        resultVo.setProcessInstanceId(processInstanceId);
        return resultVo;
    }

    /**
     * 撤单
     */
    @Override
    public ResultVo stopProcess(String assignee, String processInstanceId, String comment) {
        Task task = iActivitiUtilSvc.findTaskByProcessId(processInstanceId);
        String taskId = task.getId();
        // 添加批注
        iActivitiUtilSvc.addComment(assignee, taskId, processInstanceId, "撤销流程  " + comment);

        String activityId = iActivitiUtilSvc.getActivityIdByProcessInstanceId(processInstanceId);
        // 删除运行中的实例
        iActivitiUtilSvc.stopProcess(processInstanceId, FLOW_STAT_REVOCATION, activityId);
        ResultVo resultVo = new ResultVo();
        resultVo.setJobStat(FLOW_STAT_REVOCATION);
        resultVo.setAssignee(NO_BODY);
        resultVo.setProcessInstanceId(processInstanceId);
        return resultVo;
    }

    /**
     * 撤单
     */
    @Override
    public ResultVo stopProcessByTaskId(String assignee, String taskId, String comment) {
        ProcessInstance processInstance = iActivitiUtilSvc.findProcessInstanceByTaskId(taskId);
        String processInstanceId = processInstance.getProcessInstanceId();
        // 添加批注
        iActivitiUtilSvc.addComment(assignee, taskId, processInstanceId, "撤销流程  " + comment);

        String activityId = iActivitiUtilSvc.getActivityIdByProcessInstanceId(processInstanceId);
        // 删除运行中的实例
        iActivitiUtilSvc.stopProcess(processInstanceId, FLOW_STAT_REVOCATION, activityId);
        ResultVo resultVo = new ResultVo();
        resultVo.setJobStat(FLOW_STAT_REVOCATION);
        resultVo.setAssignee(NO_BODY);
        resultVo.setProcessInstanceId(processInstanceId);
        return resultVo;
    }

    /**
     * 撤单/驳回停止任务
     */
    @Override
    public ResultVo rejectProcess(String assignee, String processInstanceId, String comment) {
        Task task = iActivitiUtilSvc.findTaskByProcessId(processInstanceId);
        String taskId = task.getId();
        // 添加批注
        Comment addComment = iActivitiUtilSvc.addComment(assignee, taskId, processInstanceId, "回退流程  " + comment);

        // 停止前签收
        iActivitiUtilSvc.claimProcess(taskId, assignee);

        String activityId = iActivitiUtilSvc.getActivityIdByProcessInstanceId(processInstanceId);

        // 删除运行中的实例
        iActivitiUtilSvc.stopProcess(processInstanceId, FLOW_STAT_REJECT, activityId);
        ResultVo resultVo = new ResultVo();
        if (Objects.nonNull(addComment)) {
            resultVo.setCommentId(addComment.getId());
        }
        resultVo.setTaskId(taskId);
        resultVo.setJobStat(FLOW_STAT_REJECT);
        resultVo.setAssignee(NO_BODY);
        resultVo.setProcessInstanceId(processInstanceId);
        return resultVo;
    }

    @Override
    public List<Map<String, Object>> traceProcess(String proInsId) throws Exception {
        return iActivitiUtilSvc.traceProcess(proInsId);
    }

    @Override
    public List<Map<String, Object>> traceInfoByBusinessKey(String businessKey) {
        return iActivitiUtilSvc.traceInfoByBusinessKey(businessKey);
    }

    /**
     * 转办流程
     */
    @Override
    public ResultVo doTransferProcessInstanceIdTask(String processInstanceId, String assignee, String comment, String userName) {
        Task task = iActivitiUtilSvc.findTaskByProcessId(processInstanceId);
        Assert.notNull(task, "该任务已被其他人处理");
        return this.doTransferTask(task.getId(), assignee, comment, userName);
    }


    /**
     * 转办流程
     */
    @Override
    public ResultVo doTransferTask(String taskId, String assignee, String comment, String userName) {
        TaskEntity taskById = (TaskEntity) iActivitiUtilSvc.findTaskById(taskId);
        validTask(taskId, taskById);
        // 通过前签收
        iActivitiUtilSvc.claimProcess(taskId, userName);

        String processInstanceId = taskById.getProcessInstanceId();

        // 添加批注
        Comment addComment = iActivitiUtilSvc.addComment(userName, taskId, processInstanceId, "转办流程  " + comment);

        //转办接收人
        String[] assignees = assignee.split(",");
        iActivitiUtilSvc.updateAssigneesByTask(taskById, assignees);
//        if (assignees.length == 1) {
//            iActivitiUtilSvc.transferProcess(taskId, assignee);
//        } else {
//            // 添加组任务
//            iActivitiUtilSvc.transferProcess(taskId, null);
//            iActivitiUtilSvc.addCandidateUser(taskId, assignees);
//        }

        ResultVo resultVo = new ResultVo();
        if (Objects.nonNull(addComment)) {
            resultVo.setCommentId(addComment.getId());
        }
        resultVo.setTaskId(taskId);
        resultVo.setJobStat(FLOW_STAT_PROCESS);
        resultVo.setAssignee(assignee);
        resultVo.setProcessInstanceId(processInstanceId);
        return resultVo;
    }

    private void validTask(String taskId, TaskEntity taskEntity) {
        if (taskEntity == null) {
            HistoricTaskInstance task = iActivitiUtilSvc.getTaskHistoryTaskId(taskId);
            if (task == null) {
                throw new RuntimeException("当前任务流程任务【" + taskId + "】已被处理");
            } else {
                throw new RuntimeException("该流程已被【" + task.getAssignee() + "】-"
                        + DateFormatUtils.format(task.getEndTime(), "yyyy-MM-dd HH:mm:ss") + "处理!");
            }
        }
    }

    private void validTask(TaskEntity taskEntity) {
        if (taskEntity == null) {
            throw new RuntimeException("该流程已被处理!");
        }
    }


    @Override
    public void doSuspend(String procInstanceId) {
        iActivitiUtilSvc.suspendProcess(procInstanceId);
    }

    @Override
    public void doResume(String procInstanceId) {
        iActivitiUtilSvc.resumeProcess(procInstanceId);
    }

    @Override
    public InputStream findImageInputStream(String deploymentId, String imageName) {
        return iActivitiUtilSvc.findImageInputStream(deploymentId, imageName);
    }

    @Override
    public byte[] findDiagramImageForAcivitiFlowsActiviti(String deploymentId, String imageType) {
        return iActivitiUtilSvc.getDiagramImageByDeploymentId(deploymentId, imageType);
    }

    @Override
    public ResultVo addCommentByProcessInstanceId(CommentParamVo commentParamVo) {
        String comment = commentParamVo.getComment();
        String processInstanceId = commentParamVo.getProcessInstanceId();
        String userName = commentParamVo.getUserName();
        Task task = iActivitiUtilSvc.findTaskByProcessId(processInstanceId);
        if (task == null) {
            return null;
        }
        // 添加批注
        Comment addComment = iActivitiUtilSvc.addComment(userName, task.getId(), processInstanceId, comment);
        ResultVo resultVo = new ResultVo();
        resultVo.setTaskId(task.getId());
        resultVo.setCommentId(addComment.getId());
        return resultVo;
    }

    @Override
    public List<CommentVo> findCommentByBusinessKey(String businessKey) {
        List<Comment> commentList = iActivitiUtilSvc.findCommentByBusinessKey(businessKey);
        return getCommentVos(commentList);
    }

    @Override
    public List<CommentVo> findCommentByTaskId(String taskId) {
        List<Comment> commentList = iActivitiUtilSvc.findCommentByTaskId(taskId);
        return getCommentVos(commentList);
    }

    @Override
    public List<CommentVo> findCommentByProcessInstanceId(String processInstanceId) {
        List<Comment> commentList = iActivitiUtilSvc.findCommentByProcessInstanceId(processInstanceId);
        return getCommentVos(commentList);
    }

    @Override
    public TaskVo findTaskByProcessInstanceId(String processInstanceId) {
        Task task = iActivitiUtilSvc.findTaskByProcessId(processInstanceId);
        TaskVo taskVo = new TaskVo();
        currentTask(task, taskVo);
        return taskVo;
    }

    @Override
    public List<Map> getNextTasks(String processInstanceId) {
        String activityId = iActivitiUtilSvc.getActivityIdByProcessInstanceId(processInstanceId);
        return iActivitiUtilSvc.getNextJobs(processInstanceId, activityId, "map");
    }

    @Override
    public List getNextTaskIds(String processInstanceId) {
        String activityId = iActivitiUtilSvc.getActivityIdByProcessInstanceId(processInstanceId);
        return iActivitiUtilSvc.getNextJobs(processInstanceId, activityId, "string");
    }

    @Override
    public String getNextTaskType(String processInstanceId) {
        String activityId = iActivitiUtilSvc.getActivityIdByProcessInstanceId(processInstanceId);
        return iActivitiUtilSvc.getNextType(processInstanceId, activityId);
    }

    @Override
    public List<ActivityVo> getReturnBackTaskActivityId(String processInstanceId) {
        //取走过的历史节点
        return iActivitiUtilSvc.getHistoryTaskId(processInstanceId);

    }

    @Override
    public List<ActivityVo> allActivityIdsById(String processDefinitionId) {
        return iActivitiUtilSvc.allActivityIdsById(processDefinitionId);
    }


    @Override
    public TaskVo findTaskByBusinessKey(String businessKey) {
//        Task task = iActivitiUtilSvc.findTaskByBusinessKey(businessKey);
        List<Task> tasks = iActivitiUtilSvc.getTasksByBizKey(businessKey);
        Task task = tasks.get(0);
        TaskVo taskVo = new TaskVo();
        currentTask(task, taskVo);
        return taskVo;
    }

    private void currentTask(Task task, TaskVo taskVo) {
        if (task == null) {
            taskVo.setCreateTime(new Date());
            taskVo.setAssignee("流程已结束");
            taskVo.setActivityId("end");
            return;
        } else {
            String assignee = task.getAssignee();
            if (assignee == null) {
                iActivitiUtilSvc.getGroupUserString(task.getId(), taskVo);
            } else {
                taskVo.setAssignee(assignee);
            }
            taskVo.setActivityId(task.getTaskDefinitionKey());
            taskVo.setCreateTime(task.getCreateTime());
        }
        taskVo.setProcessDefinitionId(task.getProcessDefinitionId());
        taskVo.setTaskId(task.getId());
    }


    @Override
    public TaskVo findTaskByTaskId(String taskId) {
        Task task = iActivitiUtilSvc.findTaskById(taskId);
        TaskVo taskVo = new TaskVo();
        currentTask(task, taskVo);
        return taskVo;
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
            return commentVo;
        }).collect(Collectors.toList());
    }


}
