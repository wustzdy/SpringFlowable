package com.wustzdy.springboot.flowable.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wustzdy.springboot.flowable.demo.demo.BusinessAssignModel;
import com.wustzdy.springboot.flowable.demo.entity.BusinessAssigneeEntity;
import com.wustzdy.springboot.flowable.demo.mapper.BusinessAssignMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@SuppressWarnings("all")
@Service
public class BusinessAssigneeService {
    @Autowired
    private BusinessAssignMapper mapper;

    public BusinessAssignModel add(BusinessAssignModel model) {
        BusinessAssigneeEntity entity = new BusinessAssigneeEntity();
        BeanUtils.copyProperties(model, entity);
        mapper.insert(entity);


        BusinessAssignModel resModel = new BusinessAssignModel();
        BeanUtils.copyProperties(entity, resModel);
        return resModel;

    }

    public BusinessAssigneeEntity getActivitiAssignees(String processDefinitionId, String operType, String activityId) {
        return mapper.selectOne(new QueryWrapper<BusinessAssigneeEntity>()
                .eq("process_definition_id", processDefinitionId)
                .eq("oper_type", operType)
                .eq("activity_id", activityId));


    }

    /**
     * 根据流程定义id（含版本号）、环节id、流程模版id、操作类型查询签收人；
     * <p>
     * 流程定义id作为流程唯一性的标识
     * 环节id作为环节唯一标识
     * 流程模版id作为【业务类型+动作】启动流程的唯一标识
     */
    public BusinessAssigneeEntity getActivitiAssignees(String processDefinitionId, String operType, String activityId,
                                                       Integer businessTemplateId) {
        return mapper.selectOne(new QueryWrapper<BusinessAssigneeEntity>()
                .eq("process_definition_id", processDefinitionId)
                .eq("business_template_id", businessTemplateId)
                .eq("oper_type", operType)
                .eq("activity_id", activityId));
    }


}
