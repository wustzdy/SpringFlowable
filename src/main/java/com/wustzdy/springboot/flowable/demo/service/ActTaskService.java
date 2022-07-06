package com.wustzdy.springboot.flowable.demo.service;


import com.wustzdy.springboot.flowable.demo.util.R;
import com.wustzdy.springboot.flowable.demo.vo.ResultVo;
import com.wustzdy.springboot.flowable.demo.vo.TaskVo;
import com.wustzdy.springboot.flowable.demo.vo.TraceProcessVo;
import org.flowable.task.api.Task;

import java.util.List;
import java.util.Map;

public interface ActTaskService {

    Task getTask(String taskId);

    List<Task> getTasks(String processInstanceId);

    Boolean isProcessInstanceActive(String processInstanceId);

    boolean isMpTask(String taskId);

    boolean isMpTask(Task task);

    ResultVo doPassMpTask(String taskId, String comment, String operation, String userName);

    ResultVo startMpProcess(String processDefinitionKey, String assignee, String businessKey, Map<String, Object> vars);

    R urgeByProcInsId(String processInstanceId, String username);

    R currentTasksByProcInsId(String processInstanceId);

    List<TaskVo> currentTasks(List<Task> tasks, boolean isNeedEndVo);

    List<String> currentTasksAssigneOrCandidates(String processInstanceId);

    List<String> getWhitelist();

    String filterByWhitelist(String nextStaff);

    List<String> filterByWhitelist(List<String> users);

}
