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

import java.util.List;
import java.util.stream.Collectors;

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

    public List<BusinessTemplateEntity> list() {
        List<BusinessTemplateEntity> list = mapper.selectList(null);

        return list;

    }
}
