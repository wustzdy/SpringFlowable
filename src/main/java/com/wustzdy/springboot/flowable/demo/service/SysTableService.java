package com.wustzdy.springboot.flowable.demo.service;



import com.baomidou.mybatisplus.extension.service.IService;
import com.wustzdy.springboot.flowable.demo.entity.SysTableEntity;
import com.wustzdy.springboot.flowable.demo.util.PageUtils;

import java.util.List;
import java.util.Map;


public interface SysTableService extends IService<SysTableEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void valid(SysTableEntity sysTable);

    SysTableEntity selectByTypeAndValue(String type, String value);

    List<String> types(Map<String, Object> params);

    PageUtils listOrderContent(Map<String, Object> params);

    List<SysTableEntity> selectByDataType(String dataType);

    List<SysTableEntity> selectByDataTypeAndRemark(String dataType, String remark);

    List<SysTableEntity> selectByRemark(String dataType, String remark);

    void selectByDataKey(String dataKey);
}

