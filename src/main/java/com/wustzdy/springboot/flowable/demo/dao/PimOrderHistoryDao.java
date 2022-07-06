package com.wustzdy.springboot.flowable.demo.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wustzdy.springboot.flowable.demo.entity.PimOrderEntity;
import com.wustzdy.springboot.flowable.demo.entity.PimOrderHistoryEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;


@Mapper
public interface PimOrderHistoryDao extends BaseMapper<PimOrderHistoryEntity> {


    List<PimOrderEntity> selectHistoryOrderPage(Map<String, Object> params);
}
