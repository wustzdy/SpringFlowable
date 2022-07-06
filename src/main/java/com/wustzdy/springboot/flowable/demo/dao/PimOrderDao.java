package com.wustzdy.springboot.flowable.demo.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wustzdy.springboot.flowable.demo.entity.PimOrderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;


@Mapper
public interface PimOrderDao extends BaseMapper<PimOrderEntity> {

    @Select("select * from pim_order where order_num = #{orderNum}")
    PimOrderEntity getByOrderNum(String orderNum);

    @Update("update pim_order set order_status = #{orderStatus} where order_num = #{orderNum}")
    void updateByStatus(String orderNum, String orderStatus);

    @Select("select * from pim_order where process_instance_id = #{processInstanceId}")
    PimOrderEntity getByProcessInstanceId(String processInstanceId);

    @Select("select * from pim_order where order_status='process' or order_status='init'")
    List<PimOrderEntity> unfinishList();

    @Select("select count(1) from pim_order where order_user=#{assignee} and order_status='process'")
    long queryUserApplyCount(String assignee);

    List<PimOrderEntity> selectUserPageByParam(Map<String, Object> params);

    @Select("select * from pim_order po  where next_staff='audit_admin' and question_type='approve' and (order_status ='process' or order_status='transfer' or order_status='init')")
    List<PimOrderEntity> fintNextStaffIsAuditAdminList(String nextStaff);

}
