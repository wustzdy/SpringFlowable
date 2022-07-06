package com.wustzdy.springboot.flowable.demo.vo;

import com.wustzdy.springboot.flowable.demo.entity.BusinessFlowsProxyInstanceEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class FlowsProxyVo implements Serializable {
    private String nextUser;
    private List<BusinessFlowsProxyInstanceEntity> proxyInstanceEntities;
}
