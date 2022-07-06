package com.wustzdy.springboot.flowable.demo.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.wustzdy.springboot.flowable.demo.entity.BusinessFlowsProxyRecordEntity;

import java.util.List;

public interface BusinessFlowsProxyRecordService extends IService<BusinessFlowsProxyRecordEntity> {
    List<BusinessFlowsProxyRecordEntity> getListByInstanceId(Integer instanceId);
}
