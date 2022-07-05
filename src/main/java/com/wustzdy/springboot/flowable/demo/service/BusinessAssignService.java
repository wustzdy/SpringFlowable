package com.wustzdy.springboot.flowable.demo.service;

import com.wustzdy.springboot.flowable.demo.demo.BusinessAssignModel;
import com.wustzdy.springboot.flowable.demo.entity.BusinessAssignEntity;
import com.wustzdy.springboot.flowable.demo.mapper.BusinessAssignMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@SuppressWarnings("all")
@Service
public class BusinessAssignService {
    @Autowired
    private BusinessAssignMapper mapper;

    public BusinessAssignModel add(BusinessAssignModel model) {
        BusinessAssignEntity entity = new BusinessAssignEntity();
        BeanUtils.copyProperties(model, entity);
        mapper.insert(entity);


        BusinessAssignModel resModel = new BusinessAssignModel();
        BeanUtils.copyProperties(entity, resModel);
        return resModel;

    }


}
