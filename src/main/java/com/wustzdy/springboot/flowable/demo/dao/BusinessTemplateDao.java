package com.wustzdy.springboot.flowable.demo.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wustzdy.springboot.flowable.demo.entity.BusinessTemplateEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;


@Mapper
public interface BusinessTemplateDao extends BaseMapper<BusinessTemplateEntity> {

    @Select("select * from business_template" +
            " where business_type=#{serviceType} and business_operation=#{operationType} and template_default=1")
    BusinessTemplateEntity selectByBusinessTypeAndBusinessOperation(String serviceType, String operationType);

    @Select("select * from business_template" +
            " where business_type=#{serviceType} and template_default=0 ")
    BusinessTemplateEntity selectDefaultByBusinessType(String serviceType);

    @Select("select distinct(template_type) businessType from business_template")
    List<String> selectTemplateTypeList();

    @Select("select distinct(template_type) businessType from business_template where flow_type='resource' or flow_type='service'")
    List<String> selectResourceAndServiceTemplateTypeList();

    @Select("select distinct(template_type) businessType from business_template where flow_type='order'")
    List<String> selectOrderTemplateTypeList();

}
