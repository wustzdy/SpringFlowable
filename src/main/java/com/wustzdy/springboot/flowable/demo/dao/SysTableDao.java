package com.wustzdy.springboot.flowable.demo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wustzdy.springboot.flowable.demo.entity.SysTableEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;


@Mapper
public interface SysTableDao extends BaseMapper<SysTableEntity> {

    @Select("select * from sys_table where data_type=#{serviceType} and data_value=#{dataValue}")
    SysTableEntity selectKeyByTypeAndValue(String serviceType, String dataValue);

    @Select("select count(1) from sys_table t,pim_order o  where t.data_value = o.order_type_id and o.order_num = #{orderNum} and t.data_type ='orderType' and t.data_key like '%IDC%'")
    int getIdc(String orderNum);

    @Select("select distinct(data_type) dataType from sys_table")
    List<String> types();

    @Select("select t2.* from sys_table t1,sys_table t2 where t1.data_type = 'orderType' and t1.data_value= t2.data_type and t2.data_type= #{dataType} order by t2.data_type ")
    List<SysTableEntity> listOrderContentByDataType(IPage page, String dataType);

    @Select("select t2.* from sys_table t1,sys_table t2 where t1.data_type = 'orderType' and t1.data_value= t2.data_type order by t2.data_type")
    List<SysTableEntity> listOrderContent(IPage page);

    @Select("select * from sys_table where data_type = #{dataType}  order by sortable asc")
    List<SysTableEntity> getByDataType(String dataType);

    @Select("select * from sys_table as a where data_type = #{dataType} and " +
            "(select count(*) from sys_table as b where a.data_value = b.data_type and b.remark=#{remark}) >0 order by sortable asc;")
    List<SysTableEntity> getByDataTypeAndRemark(String dataType, String remark);

    @Select("select * from sys_table where data_type = #{dataType} and remark = #{remark} order by sortable asc")
    List<SysTableEntity> getByremark(String dataType, String remark);

    @Select("select * from sys_table where data_key = #{dataKey}")
    List<SysTableEntity> getByDataKey(String dataKey);
}
