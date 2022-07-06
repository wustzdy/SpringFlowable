package com.wustzdy.springboot.flowable.demo.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wustzdy.springboot.flowable.demo.entity.BusinessFlowsProxyInstanceEntity;
import com.wustzdy.springboot.flowable.demo.vo.FlowsProxyInstDetailVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface BusinessFlowsProxyInstanceDao extends BaseMapper<BusinessFlowsProxyInstanceEntity> {
    List<FlowsProxyInstDetailVo> selectProxyInstByParam(Map<String, Object> params);
}