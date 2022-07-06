package com.wustzdy.springboot.flowable.demo.service;


import com.wustzdy.springboot.flowable.demo.vo.ActivityVo;
import com.wustzdy.springboot.flowable.demo.vo.TaskVo;
import org.flowable.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Comment;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface IActivitiUtilSvc {

    ProcessInstance startProcess(String processDefinitionKey, String businessKey, Map<String, Object> variables);

    InputStream findImageInputStream(String deploymentId, String imageName);

    byte[] getDiagramImageByDeploymentId(String deploymentId, String imageType);

    byte[] getDiagramImageByProcessInstanceId(String processInstanceId);

    List<Comment> findCommentByTaskId(String taskId);

    List<Comment> findCommentByProcessInstanceId(String processInstanceId);

    List<Comment> findCommentByBusinessKey(String businessKey);

    String getProcessInstanceIdByBusinessKey(String businessKey);

    String claimProcess(String taskId, String userId);

    void addCandidateUser(String taskId, String[] userIds);

    void deleteCandidateUser(String taskId, String[] userIds);

    void commitProcess(String taskId, Map<String, Object> variables, String activityId);

    void stopProcess(String procInstanceId, String deleteReason, String activityId);

    void deleteProcess(String procInstanceId);

    void transferProcess(String taskId, String userId);

    void suspendProcess(String procInstanceId);

    void resumeProcess(String procInstanceId);

    List<ActivityVo> allActivityIds(String processDefinitionKey);

    List<ActivityVo> allActivityIdsById(String processDefinitionId);

    List getNextJobs(String procInstanceId, String activityId, String listType);

    String getNextType(String procInstanceId, String activityId);

    String getEndActivityId(String procInstanceId);

    ProcessInstance findProcessInstanceById(String processInstanceId);

    Task findTask(String procInstanceId);

    Task findTaskByKey(String processInstanceId, String key);

    List<Task> findTaskList(String procInstanceId);

    Task findTaskById(String taskId);

    List<Task> findTaskListByTaskId(String taskId);

    List<Task> findTaskListByKey(String processInstanceId, String key);

    ProcessDefinitionEntity findProcessDefinitionEntityByTaskId(String taskId);

    ProcessInstance findProcessInstanceByTaskId(String taskId);

    String getActivityIdByTaskId(String taskId);

    String getActivityIdByProcessInstanceId(String processInstanceId);

    List<ActivityVo> getHistoryTaskId(String procInstanceId);

    ProcessDefinition deployProcess(MultipartFile flowFile);

    Comment addComment(String taskId, String processInstanceId, String comment);

    HistoricTaskInstance getTaskHistoryTaskId(String taskId);

    Task findTaskByBusinessKey(String businessKey);

    Task findTaskByProcessId(String processInstanceId);

    List<Task> getTasks(String processInstanceId);

    List<Task> getTasksByBizKey(String businessKey);

    void getGroupUserString(String taskId, TaskVo taskVo);

    String getHisAssigneeByProcessInstanceIdAndActivityId(String processInstanceId, String prevActivityId);

    List<Map<String, Object>> traceProcess(String processInstanceId) throws Exception;

    List<Map<String, Object>> traceInfoByBusinessKey(String businessKey);

    Comment addComment(String assignee, String taskId, String processInstanceId, String s);

    List<String> getCandidateUsersByTaskId(String taskId);

    void updateAssigneesByTask(Task task, String[] newAssigneeList);
}
