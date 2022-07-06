package com.wustzdy.springboot.flowable.demo.service;



import com.baomidou.mybatisplus.extension.service.IService;
import com.wustzdy.springboot.flowable.demo.entity.BusinessFlowsProxyInstanceEntity;
import com.wustzdy.springboot.flowable.demo.util.PageUtils;

import java.util.Map;

public interface BusinessFlowsProxyInstanceService extends IService<BusinessFlowsProxyInstanceEntity> {
    void checkAndDoProxy(String username, String taskId, String commentId, String operation);

    String getProxyApprover(String proxyUser, String taskId, String commentId);

    PageUtils<BusinessFlowsProxyInstanceEntity> getProxyInstances(Map<String, Object> params);

}

