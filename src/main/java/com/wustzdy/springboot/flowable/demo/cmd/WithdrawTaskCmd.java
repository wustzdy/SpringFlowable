package com.wustzdy.springboot.flowable.demo.cmd;


import com.wustzdy.springboot.flowable.demo.builder.ActivitiHistoryGraphBuilder;
import com.wustzdy.springboot.flowable.demo.demo.Edge;
import com.wustzdy.springboot.flowable.demo.demo.Graph;
import com.wustzdy.springboot.flowable.demo.demo.Node;
import org.flowable.common.engine.impl.Page;
import org.flowable.common.engine.impl.context.Context;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.HistoricActivityInstanceQueryImpl;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.persistence.entity.HistoricActivityInstanceEntity;
import org.flowable.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.flowable.task.service.impl.persistence.entity.HistoricTaskInstanceEntity;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.flowable.engine.impl.persistence.entity
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 撤销任务.
 */
public class WithdrawTaskCmd implements Command<List<String>> {

    private static Logger logger = LoggerFactory.getLogger(WithdrawTaskCmd.class);
    private String historyTaskId;
    private String historicActivityInstanceId;

    public WithdrawTaskCmd(String historyTaskId, String historicActivityInstanceId) {
        this.historyTaskId = historyTaskId;
        this.historicActivityInstanceId = historicActivityInstanceId;
    }

    /**
     * 撤销流程.
     *
     * @return 0-撤销成功 1-流程结束 2-下一结点已经通过,不能撤销
     */
    @Override
    public List<String> execute(CommandContext commandContext) {
        HistoricTaskInstanceEntity historicTaskInstanceEntity = Context.getCommandContext().getHistoricTaskInstanceEntityManager()
                .findHistoricTaskInstanceById(historyTaskId);
        HistoricActivityInstanceEntity historicActivityInstanceEntity = getHistoricActivityInstanceEntity(historyTaskId);
        Graph graph = new ActivitiHistoryGraphBuilder(historicTaskInstanceEntity.getProcessInstanceId()).build();
        Node node = graph.findById(historicActivityInstanceEntity.getId());

        if (!checkCouldWithdraw(node)) {
            logger.info("cannot withdraw {}", historyTaskId);
            return null;
            // return 2;
        }

        // 删除所有活动中的task
        this.deleteActiveTasks(historicTaskInstanceEntity.getProcessInstanceId());

        // 获得期望撤销的节点后面的所有节点历史
        List<String> historyNodeIds = new ArrayList<String>();
        collectNodes(node, historyNodeIds);
        // this.deleteHistoryActivities(historyNodeIds);
        // 恢复期望撤销的任务和历史
        this.processHistoryTask(historicTaskInstanceEntity, historicActivityInstanceEntity);
        logger.info("activiti is withdraw {}", historicTaskInstanceEntity.getName());

        return historyNodeIds;
    }

    public HistoricActivityInstanceEntity getHistoricActivityInstanceEntity(String historyTaskId) {
        logger.info("historyTaskId : {}", historyTaskId);
        logger.info("historicActivityInstanceId : {}", historicActivityInstanceId);

        HistoricActivityInstanceQueryImpl historicActivityInstanceQueryImpl = new HistoricActivityInstanceQueryImpl();
        historicActivityInstanceQueryImpl.activityInstanceId(historicActivityInstanceId);

        HistoricActivityInstanceEntity historicActivityInstanceEntity = (HistoricActivityInstanceEntity) Context.getCommandContext()
                .getHistoricActivityInstanceEntityManager()
                .findHistoricActivityInstancesByQueryCriteria(historicActivityInstanceQueryImpl, new Page(0, 1)).get(0);

        return historicActivityInstanceEntity;
    }

    public boolean checkCouldWithdraw(Node node) {
        // TODO: 如果是catchEvent，也应该可以撤销，到时候再说
        for (Edge edge : node.getEdges()) {
            Node dest = edge.getDest();
            String type = dest.getType();

            if ("userTask".equals(type)) {
                if (!dest.isActive()) {
                    logger.info("cannot withdraw, " + type + "(" + dest.getName() + ") is complete.");
                    return false;
                }
            } else if (type.endsWith("Gateway")) {
                if (!checkCouldWithdraw(dest)) {
                    return false;
                }
            } else {
                logger.info("cannot withdraw, " + type + "(" + dest.getName() + ") is complete.");
                return false;
            }
        }

        return true;
    }

    public void deleteActiveTasks(String processInstanceId) {
        Context.getCommandContext().getTaskEntityManager().deleteTasksByProcessInstanceId(processInstanceId, null, true);
    }

    public void collectNodes(Node node, List<String> historyNodeIds) {
        logger.info("node : {}, {}, {}", node.getId(), node.getType(), node.getName());
        for (Edge edge : node.getEdges()) {
            logger.info("edge : {}", edge.getName());
            Node dest = edge.getDest();
            historyNodeIds.add(dest.getId());
            collectNodes(dest, historyNodeIds);
        }
    }

    public void processHistoryTask(HistoricTaskInstanceEntity historicTaskInstanceEntity,
                                   HistoricActivityInstanceEntity historicActivityInstanceEntity) {
        historicTaskInstanceEntity.setEndTime(null);
        historicTaskInstanceEntity.setDurationInMillis(null);
        historicActivityInstanceEntity.setEndTime(null);
        historicActivityInstanceEntity.setDurationInMillis(null);

        TaskEntity task = TaskEntity.create(new Date());
        task.setProcessDefinitionId(historicTaskInstanceEntity.getProcessDefinitionId());
        task.setId(historicTaskInstanceEntity.getId());
        task.setAssigneeWithoutCascade(historicTaskInstanceEntity.getAssignee());
        task.setParentTaskIdWithoutCascade(historicTaskInstanceEntity.getParentTaskId());
        task.setNameWithoutCascade(historicTaskInstanceEntity.getName());
        task.setTaskDefinitionKey(historicTaskInstanceEntity.getTaskDefinitionKey());
        task.setExecutionId(historicTaskInstanceEntity.getExecutionId());
        task.setPriority(historicTaskInstanceEntity.getPriority());
        task.setProcessInstanceId(historicTaskInstanceEntity.getProcessInstanceId());
        task.setDescriptionWithoutCascade(historicTaskInstanceEntity.getDescription());

        Context.getCommandContext().getTaskEntityManager().insert(task);

        ExecutionEntity executionEntity = Context.getCommandContext().getExecutionEntityManager()
                .findExecutionById(historicTaskInstanceEntity.getExecutionId());
        executionEntity.setActivity(getActivity(historicActivityInstanceEntity));
    }

    public ActivityImpl getActivity(HistoricActivityInstanceEntity historicActivityInstanceEntity) {
        ProcessDefinitionEntity processDefinitionEntity = new FindProcessDefinitionEntityCmd(historicActivityInstanceEntity.getProcessDefinitionId())
                .execute(Context.getCommandContext());

        return processDefinitionEntity.findActivity(historicActivityInstanceEntity.getActivityId());
    }
}
