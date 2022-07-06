package com.wustzdy.springboot.flowable.demo.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;


import com.wustzdy.springboot.flowable.demo.cmd.HistoryProcessInstanceDiagramCmd;
import com.wustzdy.springboot.flowable.demo.constant.FlowConstants;
import com.wustzdy.springboot.flowable.demo.dao.PimOrderDao;
import com.wustzdy.springboot.flowable.demo.dao.PimOrderDelayDao;
import com.wustzdy.springboot.flowable.demo.dao.PimOrderHistoryDao;
import com.wustzdy.springboot.flowable.demo.dao.PimOrderRecordsDao;
import com.wustzdy.springboot.flowable.demo.entity.PimOrderEntity;
import com.wustzdy.springboot.flowable.demo.entity.PimOrderHistoryEntity;
import com.wustzdy.springboot.flowable.demo.entity.PimOrderRecordsEntity;
import com.wustzdy.springboot.flowable.demo.entity.SysUserEntity;
import com.wustzdy.springboot.flowable.demo.service.*;
import com.wustzdy.springboot.flowable.demo.util.ByteUtils;
import com.wustzdy.springboot.flowable.demo.util.OrderUtils;
import com.wustzdy.springboot.flowable.demo.util.R;
import com.wustzdy.springboot.flowable.demo.vo.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.engine.ProcessEngines;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import static com.wustzdy.springboot.flowable.demo.constant.BasicCloudConstants.INIT;
import static com.wustzdy.springboot.flowable.demo.constant.FlowConstants.*;


@SuppressWarnings("all")
@Slf4j
@Transactional(rollbackFor = Exception.class)
@Service("workFlowService")
public class WorkFlowServiceImpl implements WorkFlowService {

    @Autowired
    private BasicFlowService basicFlowService;

    @Autowired
    private PimOrderDao pimOrderDao;

    @Autowired
    private PimOrderDelayDao delayDao;

    @Autowired
    private PimOrderHistoryDao pimOrderHistoryDao;

    @Autowired
    private OrderUtils orderUtils;

    @Autowired
    private IHandlerTaskSvc iHandlerTaskSvc;

    @Autowired
    private IActivitiUtilSvc iActivitiUtilSvc;

    @Autowired
    private BusinessFlowsProxyService proxyService;

    @Autowired
    private PimOrderRecordsDao recordsDao;

    @Autowired
    private ActTaskService actTaskService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private RuntimeService runtimeService;

    @Override
    public void passProcess(PassFlowVo passflowVo, String userName, String operation) {
        log.info("-workFlowService-passProcess-passflowVo:{}", passflowVo);
        log.info("-workFlowService-passProcess-userName:{}", userName);
        log.info("-workFlowService-passProcess-operation:{}", operation);

        boolean needUpdateNextUserForMultiTask = false;
        String processInstanceId = null;
        if (passflowVo.getTaskId() != null) {
            ProcessInstance processInstance = iActivitiUtilSvc.findProcessInstanceByTaskId(passflowVo.getTaskId());
            processInstanceId = processInstance.getProcessInstanceId();

            List<Task> tasks = actTaskService.getTasks(processInstanceId);
            if (tasks.size() > 1) {
                needUpdateNextUserForMultiTask = true;
            }
        }

        ResultVo resultData = basicFlowService.passProcessPropagation(passflowVo, userName, operation);
        log.info("-workFlowService-passProcess-resultData:{}", resultData);

        String comment = passflowVo.getComment();
        doBusinessAndMail(resultData, comment, userName);
        // 已办工单
        doDealOrderProcess(userName, resultData.getProcessInstanceId(), comment);

        // 自动审批
        String nextUser = resultData.getAssignee();
        PimOrderEntity pimOrderEntity = pimOrderDao.getByProcessInstanceId(resultData.getProcessInstanceId());
        log.info("-workFlowService-passProcess-pimOrderEntitys:{}", pimOrderEntity);

        if (nextUser == null) {
            if (actTaskService.isProcessInstanceActive(resultData.getProcessInstanceId())) {
                List<Task> tasks = actTaskService.getTasks(resultData.getProcessInstanceId());
                if (tasks.size() > 0) {
                    log.debug("tasks num: " + tasks.size() + " check if need auto pass");
                    Task task = tasks.get(0);

                    Boolean autoPassNext = false;
                    if (task.getAssignee() != null) {
                        if (task.getAssignee().equals(pimOrderEntity.getOrderUser())) {
                            autoPassNext = true;
                        }
                    } else {
                        try {
                            log.debug(tasks.stream().map(Task::getId).collect(Collectors.joining(",")));
                            log.debug(task.getId() + " e4 " + tasks.size() + task.getName());
//                            Thread.sleep(1000);

//                            assert actTaskService.getTask(task.getId()) != null;
//
//                            TaskEntityManager taskEntityManager = commandContext.getTaskEntityManager();
//                            TaskEntity taskEntity = taskEntityManager.findTaskById(task.getId());
//                            assert taskEntity != null;

//                            TaskEntityManager taskEntityManager = Context.getCommandContext().getTaskEntityManager();
//                            TaskEntity taskEntity = taskEntityManager.findTaskById(task.getId());

//                            CommandExecutor commandExecutor = ((RuntimeServiceImpl) runtimeService).getCommandExecutor();
//                            commandExecutor.execute(new JumpTaskCmd(currentTaskEntity, targetActivity, variables));

                            List<String> candidates = iActivitiUtilSvc.getCandidateUsersByTaskId(task.getId());
                            if (candidates.contains(pimOrderEntity.getOrderUser())) {
                                autoPassNext = true;
                            }
                        } catch (Exception e) {

                            log.debug(task.getId() + "ex1:", e);
                        }

                    }
                    if (autoPassNext) {
                        log.debug("start auto pass task: " + task.getName() + task.getId());
                        PassFlowVo nextPassFlowVo = new PassFlowVo();
                        nextPassFlowVo.setTaskId(task.getId());
                        nextPassFlowVo.setComment("系统自动审批");
                        passProcess(nextPassFlowVo, pimOrderEntity.getOrderUser(), "审批通过");
                    }

                }


                if (needUpdateNextUserForMultiTask && processInstanceId != null) {

                    List<Task> tasks2 = actTaskService.getTasks(processInstanceId);
                    if (tasks2.size() > 0) {
                        Task task = tasks2.get(0);
                        if (task.getAssignee() != null) {
                            pimOrderEntity.setNextStaff(task.getAssignee());
                        } else {
                            try {
                                log.debug(task.getId() + " e5 " + task.getName());
                                List<String> candidates = iActivitiUtilSvc.getCandidateUsersByTaskId(task.getId());
                                if (candidates.size() > 0) {
                                    pimOrderEntity.setNextStaff(candidates.stream().collect(Collectors.joining(",")));
                                }
                            } catch (Exception e) {
                                log.debug(task.getId() + "ex2:", e);
                            }
                        }
                        pimOrderDao.updateById(pimOrderEntity);
                    }
                }
            }
        } else {
            String[] users = nextUser.split(",");
            List<String> userList = Arrays.asList(users);
            if (userList.contains(pimOrderEntity.getOrderUser())) {
                PassFlowVo nextPassFlowVo = new PassFlowVo();
                nextPassFlowVo.setTaskId(resultData.getNextTaskId());
                nextPassFlowVo.setComment("系统自动审批");
                passProcess(nextPassFlowVo, pimOrderEntity.getOrderUser(), "审批通过");
            }
        }

    }

    @Override
    public void batchPass(Integer id, String userName) {
        PassFlowVo passflowVo = new PassFlowVo();
        passflowVo.setComment("同意该资源申请.");
        PimOrderEntity flowsEntity = pimOrderDao.selectById(id);
        if (flowsEntity == null ||
                (!flowsEntity.getOrderStatus().equals(FLOW_STAT_PROCESS)
                        && !flowsEntity.getOrderStatus().equals(FLOW_OPER_TRANSFER)
                        && !flowsEntity.getOrderStatus().equals(INIT))) {
            return;
        }
        String extend = flowsEntity.getExtend();
        String applyUser = flowsEntity.getOrderUser();
        String serviceType = flowsEntity.getOrderTypeId();
        Integer businessTemplateId = flowsEntity.getBusinessTemplateId();

        // 流程实例id
        String processInstanceId = flowsEntity.getProcessInstanceId();
        String processDefinitionId = flowsEntity.getProcessDefinitionId();
        // 获取流程定义id
        // 环节id
        List<NextTask> nextTasks = basicFlowService.nextTask(processInstanceId);
        String nextActivityId = nextTasks.get(0).getId();
        TaskVo task = iHandlerTaskSvc.findTaskByProcessInstanceId(processInstanceId);
        // 审批人
        String nextTaskAndStaff = basicFlowService.getApproveUser(userName, businessTemplateId, serviceType, applyUser, extend,
                processDefinitionId, nextActivityId);
        passflowVo.setNextTaskAndStaffId(nextTaskAndStaff);
        passflowVo.setTaskId(task.getTaskId());
        // 通过当前启动流程
        ResultVo resultVo = basicFlowService.passProcess(passflowVo, userName, "工单通过");
        doBusinessAndMail(resultVo, "工单通过", userName);
    }

    @Override
    public void rejectStopProcess(RejectFlowVo rejectFlowVo, String userName) {
        ResultVo resultVo = iHandlerTaskSvc.rejectProcess(userName, rejectFlowVo.getProcessInstanceId(), rejectFlowVo.getComment());
        log.info("-rejectStopProcess-resultVo:{}", resultVo.toString());
        proxyService.checkAndDoProxy(userName, resultVo, "reject");  // 代理记录
        doBusinessAndMail(resultVo, userName, userName);
        // 已办工单
        doDealOrderProcess(userName, resultVo.getProcessInstanceId(), rejectFlowVo.getComment());
    }

    @Override
    public void transferProcess(TransferFlowVo transferFlowVo, String userName) {
        log.info("--transferProcess--transferFlowVo:{}", transferFlowVo.toString());
        log.info("--transferProcess--userName:{}", userName);

        FlowsProxyVo flowsProxyVo = proxyService.getProxyUserInfo(transferFlowVo.getAssignee());
        log.info("--transferProcess--flowsProxyVo:{}", flowsProxyVo.toString());

        String assignee = flowsProxyVo.getNextUser();
        ResultVo resultVo = iHandlerTaskSvc.doTransferTask(transferFlowVo.getTaskId(), assignee, transferFlowVo.getComment(), userName);
        log.info("--transferProcess--resultVo:{}", resultVo.toString());

        proxyService.checkAndDoProxy(userName, resultVo, "transfer");
        proxyService.saveProxyInstance(flowsProxyVo, transferFlowVo.getTaskId());
        doBusinessAndMail(resultVo, transferFlowVo.getComment(), userName);
        // 已办工单
        doDealOrderProcess(userName, resultVo.getProcessInstanceId(), transferFlowVo.getComment());
    }

    @Override
    public void batchDeleteProcessInstanceIdsProcess(String processInstanceId, String userName) {
        pimOrderDao.delete(new UpdateWrapper<PimOrderEntity>()
                .eq("process_instance_id", processInstanceId));
        // 流程实例id
        iHandlerTaskSvc.deleteProcess(processInstanceId);

        // 已办工单
        doDealOrderProcess(userName, processInstanceId, null);
    }

    @Override
    public void comment(CommentParamVo commentParamVo, String flowStatProcess) {
        ResultVo resultVo = iHandlerTaskSvc.addCommentByProcessInstanceId(commentParamVo);
        if (Objects.isNull(resultVo)) {
            return;
        }
        String processInstanceId = commentParamVo.getProcessInstanceId();
        PimOrderEntity pimOrderEntity = pimOrderDao.getByProcessInstanceId(processInstanceId);
        if (pimOrderEntity == null) {
            return;
        }
        pimOrderEntity.setOrderStatus(flowStatProcess);
        String comment = commentParamVo.getComment();
        String orderUser = pimOrderEntity.getOrderUser();
//        String nextStaff = pimOrderEntity.getNextStaff();
        String currentUser = commentParamVo.getUserName();
//        String senUser;
//        if (currentUser.equals(orderUser)) {
//            senUser = nextStaff;
//        } else {
//            senUser = orderUser;
//        }
        // 已办工单
        doDealOrderProcess(currentUser, processInstanceId, comment);

        pimOrderEntity.setCurrentDealUser(currentUser);
        pimOrderEntity.setUpdateTime(new Date());
        pimOrderDao.updateById(pimOrderEntity);
        // 代理记录
        proxyService.checkAndDoProxy(currentUser, resultVo, "comment");
        orderUser = actTaskService.filterByWhitelist(orderUser);
        if (StrUtil.isNotBlank(orderUser)) {
            // 发送邮件
            orderUtils.formatMailContent(orderUser, currentUser, comment, pimOrderEntity);
        }
    }

    @Override
    public void comment(CommentParamVo commentParamVo) {
        ResultVo resultVo = iHandlerTaskSvc.addCommentByProcessInstanceId(commentParamVo);
        if (Objects.isNull(resultVo)) {
            return;
        }
        String processInstanceId = commentParamVo.getProcessInstanceId();
        PimOrderEntity pimOrderEntity = pimOrderDao.getByProcessInstanceId(processInstanceId);
        if (pimOrderEntity == null) {
            return;
        }
        String comment = commentParamVo.getComment();
//        String orderUser = pimOrderEntity.getOrderUser();
        String nextStaff = pimOrderEntity.getNextStaff();
        String currentUser = commentParamVo.getUserName();
//        String senUser;
//        if (currentUser.equals(orderUser)) {
//            senUser = nextStaff;
//        } else {
//            senUser = orderUser;
//        }
        // 已办工单
        doDealOrderProcess(currentUser, processInstanceId, comment);

        pimOrderEntity.setCurrentDealUser(currentUser);
        pimOrderEntity.setUpdateTime(new Date());
        pimOrderDao.updateById(pimOrderEntity);
        nextStaff = actTaskService.filterByWhitelist(nextStaff);
        if (StrUtil.isNotBlank(nextStaff)) {
            // 发送邮件
            orderUtils.formatMailContent(nextStaff, currentUser, comment, pimOrderEntity);
        }
    }

    private void doDealOrderProcess(String currentUser, String processInstanceId, String content) {
        PimOrderEntity pimOrderEntity = pimOrderDao.getByProcessInstanceId(processInstanceId);
        if (pimOrderEntity == null) {
            return;
        }
        PimOrderHistoryEntity historyEntity = new PimOrderHistoryEntity();
        historyEntity.setUserName(currentUser);
        historyEntity.setOrderNum(pimOrderEntity.getOrderNum());
        historyEntity.setRemark(content);
        pimOrderHistoryDao.insert(historyEntity);
        //保存操作记录
        saveRecord(pimOrderEntity, currentUser);
    }

    @Override
    public String getApproveLeader(ApproveUserVo params, String userName) {
        String id = params.getId();
        PimOrderEntity pimOrderEntity = pimOrderDao.selectById(id);

        return getApproveLeader(params, userName, pimOrderEntity);
    }

    /**
     * 获取审批人信息
     */
    @Override
    public String getApproveLeader(ApproveUserVo params, String username, PimOrderEntity pimOrderEntity) {
        String activityId = params.getActivityId();
        String processDefinitionId = params.getProcessDefinitionId();
        if (pimOrderEntity == null) {
            throw new RuntimeException("该流程无业务信息");
        }
        String extend = pimOrderEntity.getExtend();
        String applyUser = pimOrderEntity.getOrderUser();
        String serviceType = pimOrderEntity.getOrderTypeId();
        Integer businessTemplateId = pimOrderEntity.getBusinessTemplateId();
        // 审批人
        return basicFlowService.getApproveUser(username, businessTemplateId, serviceType, applyUser, extend,
                processDefinitionId, activityId);
    }

    @Override
    public void addCandidateProcess(CandidateProInstIdParamVo candidateParamVo) {
        // 流程
        @NonNull String assignee = candidateParamVo.getAssignee();
        @NonNull String taskId = candidateParamVo.getTaskId();
        String comment = candidateParamVo.getComment();
        @NonNull String userName = candidateParamVo.getUserName();
        ResultVo resultVo = iHandlerTaskSvc.addCandidate(taskId, assignee, comment, userName);
        // 代理审批记录
        proxyService.checkAndDoProxy(userName, taskId, resultVo.getCommentId(), "candidate");
        doBusinessAndMail(resultVo, comment, userName);
    }

    @Override
    public void stopProcess(String userName, String processInstanceId, String comment) {
        ResultVo resultVo = iHandlerTaskSvc.stopProcess(userName, processInstanceId, comment);
        doBusinessAndMail(resultVo, comment, userName);
        // 已办工单
        doDealOrderProcess(userName, processInstanceId, comment);
    }

    @Override
    public void closeProcess(String userName, String processInstanceId, String comment) {
        log.info("--closeProcess--userName:{},processInstanceId:{},comment:{}", userName, processInstanceId, comment);
        // 流程
        ResultVo resultVo = iHandlerTaskSvc.closeProcess(userName, processInstanceId, comment);
        log.info("--closeProcess--resultVo:{}", resultVo.toString());
        // 代理记录
        proxyService.checkAndDoProxy(userName, resultVo, "close");
        doBusinessAndMail(resultVo, comment, userName);
        // 已办工单
        doDealOrderProcess(userName, processInstanceId, comment);
    }

//    @Override
//    public void urgeOrder(String userName, String processInstanceId) {
//        PimOrderEntity entity = pimOrderDao.getByProcessInstanceId(processInstanceId);
//        if (entity == null) {
//            return;
//        }
//        orderUtils.formatUrgeMailContent(entity.getNextStaff(), userName, "您有1条工单被申请人催促办理，请您及时处理", entity);
//    }

    @Override
    public void urgeOrder(String userName, String processInstanceId) {
        PimOrderEntity entity = pimOrderDao.getByProcessInstanceId(processInstanceId);
        if (entity == null) {
            return;
        }
        List<String> users = actTaskService.currentTasksAssigneOrCandidates(processInstanceId);
        users = actTaskService.filterByWhitelist(users);
        if (CollectionUtil.isNotEmpty(users)) {
            String nextStaff = users.stream().collect(Collectors.joining(","));
            orderUtils.formatUrgeMailContent(nextStaff, userName, "您有1条工单被申请人催促办理，请您及时处理", entity);
        }
    }

    @Override
    public R reopen(String processInstanceId) {
        PimOrderEntity entity = pimOrderDao.getByProcessInstanceId(processInstanceId);
        if (Optional.ofNullable(entity).isPresent() && entity.getOrderStatus().equals(FLOW_HANG_UP)) {
            entity.setOrderStatus(FLOW_STAT_PROCESS);
            pimOrderDao.updateById(entity);
            //将流程实例设置为重新打开状态
            iActivitiUtilSvc.resumeProcess(processInstanceId);
            return R.ok();
        } else {
            return R.error("无此工单或工单并非为挂起状态，无法重新打开");
        }
    }

    /**
     * 根据业务场景更新流程业务状态信息、下一环节审批人信息
     */
    private void doBusinessAndMail(ResultVo resultVo, String comment, String userName) {
        log.info("--doBusinessAndMail--resultVo:{}", resultVo.toString());
        String processInstanceId = resultVo.getProcessInstanceId();
        Assert.notNull(processInstanceId);
        String jobStat = resultVo.getJobStat();
        String assignee = resultVo.getAssignee();

        PimOrderEntity pimOrderEntity = pimOrderDao.getByProcessInstanceId(processInstanceId);
        if (pimOrderEntity == null) {
            return;
        }
        log.info("---------------------doBusinessAndMail jobStat is {} ----------------------", jobStat);
        switch (jobStat) {
            default:
            case FlowConstants.FLOW_STAT_END:
                pimOrderEntity.setOrderStatus(FlowConstants.FLOW_STAT_END);
                pimOrderEntity.setEndTime(new Date());
                break;
            case FlowConstants.FLOW_STAT_REJECT:
                pimOrderEntity.setOrderStatus(FlowConstants.FLOW_STAT_REJECT);
                pimOrderEntity.setEndTime(new Date());
                break;
            case FlowConstants.FLOW_STAT_REVOCATION:
                pimOrderEntity.setOrderStatus(FlowConstants.FLOW_STAT_REVOCATION);
                pimOrderEntity.setEndTime(new Date());
                break;
            case FLOW_STAT_PROCESS:
                pimOrderEntity.setOrderStatus(FLOW_STAT_PROCESS);
                break;
            case FLOW_STAT_CLOSE:
                pimOrderEntity.setOrderStatus(FLOW_STAT_CLOSE);
                pimOrderEntity.setEndTime(new Date());
                break;
        }
        //保存操作记录
        saveRecord(pimOrderEntity, userName);

        if (assignee != null) {
            // 更新流程业务数据 更新流程表审批人信息
            if (!assignee.equals(NO_BODY)) {
                pimOrderEntity.setNextStaff(assignee);
                pimOrderEntity.setCurrentDealUser(assignee);
            }
            pimOrderEntity.setUpdateTime(new Date());
            pimOrderDao.updateById(pimOrderEntity);
            delayDao.updateDelayStatus(pimOrderEntity.getOrderStatus(), pimOrderEntity.getOrderNum());

            assignee = actTaskService.filterByWhitelist(assignee);
            log.info("--doBusinessAndMail-jobStat:{},assignee:{}", jobStat, assignee);
            if (StrUtil.isNotBlank(assignee)) {
                // mail
                // 发送邮件
                orderUtils.formatMailContent(assignee, pimOrderEntity.getOrderUser(), comment, pimOrderEntity);
                // 推送消息到企业微信，仅仅当工单状态为"进行中"
                if (Arrays.asList(new String[]{FLOW_STAT_PROCESS}).contains(jobStat)) {
                    orderUtils.notifyQyWeixin(pimOrderEntity, assignee, comment);
                }
            }
        } else {
            pimOrderEntity.setUpdateTime(new Date());
            pimOrderDao.updateById(pimOrderEntity);
            delayDao.updateDelayStatus(pimOrderEntity.getOrderStatus(), pimOrderEntity.getOrderNum());
        }
    }

    @Override
    public byte[] responseDiagramByteByBusinessKey(String businessKey) throws IOException {
        String processInstanceId = iActivitiUtilSvc.getProcessInstanceIdByBusinessKey(businessKey);
        iActivitiUtilSvc.getProcessInstanceIdByBusinessKey(businessKey);
        return responseDiagramByte(processInstanceId);
    }

    @Override
    public void responseDiagramByBusinessKey(HttpServletResponse response, String businessKey) throws IOException {
        String processInstanceId = iActivitiUtilSvc.getProcessInstanceIdByBusinessKey(businessKey);
        iActivitiUtilSvc.getProcessInstanceIdByBusinessKey(businessKey);
        this.responseDiagram(response, processInstanceId);
    }

    @Override
    public byte[] responseDiagramByteByProcessInstanceId(String processInstanceId) throws IOException {
        return responseDiagramByte(processInstanceId);
    }

    @Override
    public byte[] responseDiagramByte(String processInstanceId) throws IOException {
        Command<InputStream> cmd = new HistoryProcessInstanceDiagramCmd(processInstanceId);
        InputStream is = ProcessEngines.getDefaultProcessEngine().getManagementService().executeCommand(cmd);
        return ByteUtils.input2byte(is);

    }

    @Override
    public void responseDiagramByProcessInstanceId(HttpServletResponse response, String processInstanceId) throws IOException {
        this.responseDiagram(response, processInstanceId);
    }

    @Override
    public void responseDiagram(HttpServletResponse response, String processInstanceId) throws IOException {
        int len;
        byte[] b = new byte[1024];
        response.setContentType("image/png");
        Command<InputStream> cmd = new HistoryProcessInstanceDiagramCmd(processInstanceId);
        InputStream is = ProcessEngines.getDefaultProcessEngine().getManagementService().executeCommand(cmd);
        while ((len = is.read(b, 0, 1024)) != -1) {
            response.getOutputStream().write(b, 0, len);
        }

    }

    /**
     * 操作记录
     *
     * @param orderEntity
     * @return
     */
    private Integer saveRecord(PimOrderEntity orderEntity, String userName) {
        log.info("---------------------save records by {} ----------------------", userName);
        String[] split = orderEntity.getNextStaff().split(",");
        SysUserEntity user = sysUserService.getSysUserByUsername(userName);
        for (String s : split) {
            if (s.equals(userName) || s.equals(orderEntity.getOrderUser())) {
                PimOrderRecordsEntity entity = null;
                try {
                    entity = new PimOrderRecordsEntity();
                    entity.setOrderId(orderEntity.getId());
                    entity.setOrderNum(orderEntity.getOrderNum());
                    entity.setOperationUser(userName);
                    entity.setDepartment(user.getWholeDepartName());
                    entity.setQuestionType(orderEntity.getQuestionType());
                    entity.setOrderTitle(orderEntity.getOrderTitle());
                    entity.setOperatingTime(new Date());
                    //操作持续时间
                    long updateTime = 0;
                    Date entityUpdateTime = orderEntity.getUpdateTime();
                    if (!Optional.ofNullable(entityUpdateTime).isPresent()) {
                        updateTime = orderEntity.getOrderTime().getTime();
                    } else {
                        updateTime = entityUpdateTime.getTime();
                    }
                    long l = 0;
                    //当天早上九点之前与下午六点之后不算超时时间
                    LocalDateTime workEndTime = getWorkEndTime(LocalDateTime.now());
                    LocalDateTime workStartTime = getWorkStartTime(LocalDateTime.now());
                    long workEnd = workEndTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
                    long workStart = workStartTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
                    long nowTime = System.currentTimeMillis();
                    if (nowTime > workEnd) {
                        if (updateTime < workEnd) {
                            l = workEnd - updateTime;
                        } else {
                            l = 0;
                        }
                    } else if (nowTime < workStart) {
                        //获取昨天下午六点的时间
                        LocalDateTime minusDays = LocalDateTime.now().minusDays(1);
                        LocalDateTime yesWorkEndTime = getWorkEndTime(minusDays);
                        long yesWorkEndTimes = yesWorkEndTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
                        if (updateTime < yesWorkEndTimes) {
                            l = yesWorkEndTimes - updateTime;
                        } else {
                            l = 0;
                        }
                    } else if (workStart < nowTime && nowTime < workEnd) {
                        if (updateTime < workStart) {
                            l = nowTime - workStart;
                        } else {
                            l = nowTime - updateTime;
                        }
                    }
                    entity.setDurationTime(l);
                } catch (Exception e) {
                    log.error(e.getMessage());
                    e.printStackTrace();
                }

                return recordsDao.insert(entity);
            }
        }
        return 0;
    }

    private LocalDateTime getWorkEndTime(LocalDateTime time) {
        return time.withHour(18)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
    }

    private LocalDateTime getWorkStartTime(LocalDateTime time) {
        return time.withHour(9)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
    }
}