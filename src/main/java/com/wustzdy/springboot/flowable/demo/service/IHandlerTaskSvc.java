package com.wustzdy.springboot.flowable.demo.service;


import com.wustzdy.springboot.flowable.demo.vo.*;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface IHandlerTaskSvc {

    String doClaimTask(String taskId, String assignee);

    ResultVo doPassTask(String taskId, String comment, String nextTaskAndStaffIds, String operation, String userName);

    void doCallBackTask(String taskId);

    ResultVo doTransferTask(String taskId, String assignee, String comment, String userName);

    void doSuspend(String processInstanceId);

    void doResume(String processInstanceId);

    InputStream findImageInputStream(String deploymentId, String imageName);

    byte[] findDiagramImageForAcivitiFlowsActiviti(String deploymentId, String imageType);

    InputStream getFlowImageByKey(String flowKey);

    List<CommentVo> findCommentByBusinessKey(String businessKey);

    List<CommentVo> findCommentByTaskId(String taskId);

    TaskVo findTaskByBusinessKey(String businessKey);

    TaskVo findTaskByTaskId(String taskId);

    List<CommentVo> findCommentByProcessInstanceId(String processInstanceId);

    TaskVo findTaskByProcessInstanceId(String processInstanceId);

    List<Map> getNextTasks(String processInstanceId);

    List getNextTaskIds(String processInstanceId);

    String getNextTaskType(String processInstanceId);

    List<ActivityVo> getReturnBackTaskActivityId(String processInstanceId);

    List<ActivityVo> allActivityIdsById(String processDefinitionId);

    ResultVo stopProcess(String assignee, String processInstanceId, String comment);

    ResultVo stopProcessByTaskId(String assignee, String taskId, String comment);

    ResultVo rejectProcess(String assignee, String processInstanceId, String comment);

    List<Map<String, Object>> traceProcess(String proInsId) throws Exception;

    List<Map<String, Object>> traceInfoByBusinessKey(String businessKey);

    ResultVo addCommentByProcessInstanceId(CommentParamVo commentParamVo);

    ResultVo doPassProcessInstanceIdTask(String processInstanceId, String comment, String nextTaskAndStaffId, String operation, String userName);

    ResultVo doTransferProcessInstanceIdTask(String processInstanceId, String assignee, String comment, String userName);

    ResultVo closeProcess(String assignee, String processInstanceId, String comment);

    ResultVo deleteProcess(String processInstanceId);

    ResultVo doBackTask(String processInstanceId, String prevActivityId, String comment, String userName);

    ResultVo addCandidate(String taskId, String assignee, String comment, String userName);

    ResultVo doBackTaskByTaskId(String taskId, String prevActivityId, String comment, String userName);

    String getNextTaskTypeByTaskId(String taskId);

    List<ActivityVo> getReturnBackTaskActivityIdByTaskId(String taskId);

    List<Map<String, Object>> traceProcessByTaskId(String taskId) throws Exception;
}
