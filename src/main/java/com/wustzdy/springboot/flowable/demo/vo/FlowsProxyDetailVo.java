package com.wustzdy.springboot.flowable.demo.vo;

import com.wustzdy.springboot.flowable.demo.entity.BusinessFlowsProxyInstanceEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Data
public class FlowsProxyDetailVo implements Serializable {
    private Set<String> proxyUserSet;
    private List<BusinessFlowsProxyInstanceEntity> instanceEntities;

    public FlowsProxyDetailVo() {

    }

    public FlowsProxyDetailVo(Set<String> proxyUserSet, List<BusinessFlowsProxyInstanceEntity> instanceEntities) {
        this.proxyUserSet = proxyUserSet;
        this.instanceEntities = instanceEntities;
    }
}
