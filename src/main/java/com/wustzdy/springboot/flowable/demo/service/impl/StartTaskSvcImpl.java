/**
 *
 */
package com.wustzdy.springboot.flowable.demo.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import com.wustzdy.springboot.flowable.demo.service.IActivitiUtilSvc;
import com.wustzdy.springboot.flowable.demo.service.ICommonSvc;
import com.wustzdy.springboot.flowable.demo.service.IHandlerTaskSvc;
import com.wustzdy.springboot.flowable.demo.service.IStartTaskSvc;
import com.wustzdy.springboot.flowable.demo.util.IMap;
import com.wustzdy.springboot.flowable.demo.vo.ResultVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wustzdy.springboot.flowable.demo.constant.FlowConstants.FLOW_STAT_PROCESS;


/**
 * @author xielianjun
 */
@Slf4j
@Service("iStartTaskSvc")
@Transactional(rollbackFor = Exception.class)
public class StartTaskSvcImpl implements IStartTaskSvc {

    @Autowired
    private IActivitiUtilSvc iActivitiUtilSvc;

    @Autowired
    private IHandlerTaskSvc handlerTaskSvc;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private ICommonSvc iCommonSvc;

    /**
     * 启动流程
     *
     */
    @Override
    public ResultVo doStartProcess(String flowKey, String assignee, String businessKey, String title) {
        Map<String, Object> variables = new HashMap<>();
        // 设置流程标题
        if (StringUtils.isNotBlank(title)) {
            variables.put("title", title);
        }
        // 默认第一节点接收人为流程发起人
        Authentication.setAuthenticatedUserId(assignee);

        ProcessInstance processInstance = iActivitiUtilSvc.startProcess(flowKey, businessKey, variables);

        String processInstanceId = processInstance.getId();
        String processDefinitionId = processInstance.getProcessDefinitionId();
        Task task = iActivitiUtilSvc.findTask(processInstanceId);
        String taskId = task.getId();
        String taskDefinitionKey = task.getTaskDefinitionKey();
        // 指定到人
        iActivitiUtilSvc.claimProcess(taskId, assignee);
        // next activityIds
        List<String> nextActivityIds = handlerTaskSvc.getNextTaskIds(processInstanceId);//will to do
        // result
        ResultVo resultVo = new ResultVo();
        resultVo.setProcessDefinitionKey(flowKey);
        resultVo.setTaskId(taskId);
        resultVo.setAssignee(assignee);
        resultVo.setBusinessKey(businessKey);
        resultVo.setProcessInstanceId(processInstanceId);
        resultVo.setJobStat(FLOW_STAT_PROCESS);
        resultVo.setNextActivityIds(nextActivityIds);
        resultVo.setProcessDefinitionId(processDefinitionId);
        resultVo.setProcessDefinitionKey(taskDefinitionKey);
        return resultVo;
    }

    @Override
    public ResultVo doStartProcess(String flowKey, String assignee, String businessKey, Map<String, Object> variables) {
        if (CollectionUtil.isEmpty(variables)) {
            variables = Collections.emptyMap();
        }
        // 默认第一节点接收人为流程发起人
        Authentication.setAuthenticatedUserId(assignee);

        ProcessInstance processInstance = iActivitiUtilSvc.startProcess(flowKey, businessKey, variables);

        String processInstanceId = processInstance.getId();
        String processDefinitionId = processInstance.getProcessDefinitionId();
        Task task = iActivitiUtilSvc.findTask(processInstanceId);
        String taskId = task.getId();
        String taskDefinitionKey = task.getTaskDefinitionKey();
        // 指定到人
        iActivitiUtilSvc.claimProcess(taskId, assignee);
        // next activityIds
        List<String> nextActivityIds = handlerTaskSvc.getNextTaskIds(processInstanceId);
        // result
        ResultVo resultVo = new ResultVo();
        resultVo.setTaskId(taskId);
        resultVo.setAssignee(assignee);
        resultVo.setBusinessKey(businessKey);
        resultVo.setJobStat(FLOW_STAT_PROCESS);
        resultVo.setNextActivityIds(nextActivityIds);
        resultVo.setProcessInstanceId(processInstanceId);
        resultVo.setProcessDefinitionId(processDefinitionId);
        resultVo.setProcessDefinitionKey(taskDefinitionKey);
        return resultVo;
    }

    @Override
    public void flushCurrentTaskVariables() {

        String sql = "select p.PROC_INST_ID_ ,p.END_TIME_,f.service_type,f.content,f.operation_type,apply_user,f.service_id  " +
                " from business_flows f ,act_hi_procinst p where f.service_id = p.BUSINESS_KEY_ ";
        List<IMap> iMaps = iCommonSvc.executeSelectSql(sql);
        iMaps.stream().forEach(e -> {
            Object service_type = e.get("service_type");
            Object content = e.get("content");
            Object operation_type = e.get("operation_type");
            Object apply_user = e.get("apply_user");
            Object END_TIME_ = e.get("END_TIME_");
            Object PROC_INST_ID_ = e.get("PROC_INST_ID_");
            if (END_TIME_ == null) {
                runtimeService.setVariables((String) PROC_INST_ID_, e);
            }
        });
    }

}
