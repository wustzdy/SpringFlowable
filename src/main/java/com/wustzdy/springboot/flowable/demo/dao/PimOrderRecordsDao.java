package com.wustzdy.springboot.flowable.demo.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wustzdy.springboot.flowable.demo.entity.OrderAndTimeEntity;
import com.wustzdy.springboot.flowable.demo.entity.PimOrderRecordsEntity;
import com.wustzdy.springboot.flowable.demo.entity.UserAndTimeEntity;
import com.wustzdy.springboot.flowable.demo.entity.UserSummaryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface PimOrderRecordsDao extends BaseMapper<PimOrderRecordsEntity> {


    /**
     * 查询工单与处理时长
     */
//    @Select("select order_num,order_title,sum(duration_time) as duration_time from pim_order_records where operating_time >=#{start} and operating_time <=#{end} group by order_num,order_title")
    List<OrderAndTimeEntity> getOrderAndTime(Map<String, Object> params);

    /**
     * 查询人员与处理时长
     */
//    @Select("select operation_user,sum(duration_time) as duration_time from pim_order_records where operating_time >=#{start} and order_num=#{orderNum} and operating_time <=#{end} group by operation_user")
    List<UserAndTimeEntity> getUserAndTime(Map<String, Object> params);

    /**
     * 查询人员处理时间详情
     */
//    @Select("select order_num,order_title,sum(duration_time) as duration_time from pim_order_records where operation_user=#{userName} and operating_time >=#{start} and operating_time <=#{end} group by order_num,order_title")
    List<OrderAndTimeEntity> getUserDetail(Map<String, Object> params);

    /**
     * 查询人员处理时间详情
     */
//    @Select("select order_num,order_title,sum(duration_time) as duration_time from pim_order_records where operation_user=#{userName} and operating_time >=#{start} and operating_time <=#{end} group by order_num,order_title")
    List<OrderAndTimeEntity> getUserOrder(Map<String, Object> params);

    /**
     * 获取人员处理时间统计汇总
     */
//    @Select("select operation_user,count(*) as order_count,sum(duration_time) as total_time,avg(duration_time) as avg_time from pim_order_records " +
//            "where operating_time >=#{start} and operating_time <=#{end} group by operation_user;")
    List<UserSummaryEntity> getUserSummary(Map<String, Object> params);

    /**
     * 获取某工单中技术保障的处理时间
     *
     * @param orderId
     * @return
     */
    @Select("select IFNULL(sum(duration_time),0) from pim_order_records where order_num=#{orderId} and department ='工程院_系统平台_技术保障' " +
            "and operation_user != #{excludeUser}")
    long getSreTime(String orderId, String excludeUser);

    /**
     * 获取某工单中不是技术保障人员的处理时间
     *
     * @param orderId
     * @return
     */
    @Select("select IFNULL(sum(duration_time),0) from pim_order_records where order_num=#{orderId} and (department !='工程院_系统平台_技术保障' " +
            "or operation_user = #{excludeUser})")
    long getNotSreTime(String orderId, String excludeUser);

    /**
     * 获取sre具体的处理时间列表
     *
     * @param orderId
     * @return
     */
    @Select("select operation_user,sum(duration_time) as durationTime from pim_order_records where order_num=#{orderId} and department ='工程院_系统平台_技术保障' group by operation_user")
    List<PimOrderRecordsEntity> getSreList(String orderId);
}
