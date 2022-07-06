package com.wustzdy.springboot.flowable.demo.cmd;


import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.context.Context;
import org.flowable.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FindProcessDefinitionEntityCmd implements Command<ProcessDefinitionEntity> {
    private static Logger logger = LoggerFactory.getLogger(FindProcessDefinitionEntityCmd.class);
    private String processDefinitionId;

    public FindProcessDefinitionEntityCmd(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    @Override
    public ProcessDefinitionEntity execute(CommandContext commandContext) {
        ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) Context.getProcessEngineConfiguration().getDeploymentManager()
                .findDeployedProcessDefinitionById(processDefinitionId);

        if (processDefinitionEntity == null) {
            logger.info(processDefinitionId);
            throw new IllegalArgumentException("cannot find processDefinition : " + processDefinitionId);
        }

        return processDefinitionEntity;
    }
}
