package com.wustzdy.springboot.flowable.demo.service;

import com.wustzdy.springboot.flowable.demo.demo.BusinessTemplateModel;
import com.wustzdy.springboot.flowable.demo.demo.Demo;
import com.wustzdy.springboot.flowable.demo.entity.BusinessTemplateEntity;
import com.wustzdy.springboot.flowable.demo.entity.DemoEntity;
import com.wustzdy.springboot.flowable.demo.mapper.BusinessTemplateMapper;
import com.wustzdy.springboot.flowable.demo.mapper.DemoMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BusinessTemplateService {
    @Autowired
    private BusinessTemplateMapper mapper;

    @Transactional
    public BusinessTemplateModel add(BusinessTemplateModel model) {
        BusinessTemplateEntity businessTemplateEntity = new BusinessTemplateEntity();
        businessTemplateEntity.setTemplateName(model.getTemplateName());
        businessTemplateEntity.setTemplateType(model.getTemplateType());
        businessTemplateEntity.setBusinessType(model.getBusinessType());
        businessTemplateEntity.setId(1);
        try {
            System.out.println("businessTemplateEntity: " + businessTemplateEntity.toString());
            mapper.insert(businessTemplateEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }

        BusinessTemplateModel resDemo = new BusinessTemplateModel();
        BeanUtils.copyProperties(businessTemplateEntity, resDemo);
        return resDemo;
    }
}
