package com.wustzdy.springboot.flowable.demo.util;

import com.sun.istack.internal.logging.Logger;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.impl.RepositoryServiceImpl;
import org.flowable.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.engine.impl.el.FixedValue;
import org.flowable.engine.impl.persistence.entity.ProcessDefinitionEntity;

import java.util.LinkedHashSet;
import java.util.Set;


public class ProcessDefUtils {

    public static ActivityImpl getActivity(ProcessEngine processEngine, String processDefId, String activityId) {
        ProcessDefinitionEntity pde = getProcessDefinition(processEngine, processDefId);
        return pde.findActivity(activityId);
    }

    public static ProcessDefinitionEntity getProcessDefinition(ProcessEngine processEngine, String processDefId) {
        return (ProcessDefinitionEntity) ((RepositoryServiceImpl) processEngine.getRepositoryService()).getDeployedProcessDefinition(processDefId);
    }

/*	public static void grantPermission(ActivityImpl activity, String assigneeExpression, String candidateGroupIdExpressions,
			String candidateUserIdExpressions) throws Exception {
		TaskDefinition taskDefinition = ((UserTaskActivityBehavior) activity.getActivityBehavior()).getTaskDefinition();
		taskDefinition.setAssigneeExpression(assigneeExpression == null ? null : new FixedValue(assigneeExpression));
		FieldUtils.writeField(taskDefinition, "candidateUserIdExpressions", ExpressionUtils.stringToExpressionSet(candidateUserIdExpressions), true);
		FieldUtils.writeField(taskDefinition, "candidateGroupIdExpressions", ExpressionUtils.stringToExpressionSet(candidateGroupIdExpressions), true);

		Logger.getLogger(ProcessDefUtils.class).info(
				String.format("granting previledges for [%s, %s, %s] on [%s, %s]", assigneeExpression, candidateGroupIdExpressions,
						candidateUserIdExpressions, activity.getProcessDefinition().getKey(), activity.getProperty("name")));
	}*/

    public static class ExpressionUtils {
        public static Expression stringToExpression(ProcessEngineConfigurationImpl conf, String expr) {
            return conf.getExpressionManager().createExpression(expr);
        }

        public static Expression stringToExpression(String expr) {
            return new FixedValue(expr);
        }

        public static Set<Expression> stringToExpressionSet(String exprs) {
            Set<Expression> set = new LinkedHashSet<Expression>();
            for (String expr : exprs.split(";")) {
                set.add(stringToExpression(expr));
            }

            return set;
        }
    }
}