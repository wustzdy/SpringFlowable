package com.wustzdy.springboot.flowable.demo.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wustzdy.springboot.flowable.demo.entity.PimOrderDelayEntity;
import com.wustzdy.springboot.flowable.demo.entity.PimOrderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;


@Mapper
public interface PimOrderDelayDao extends BaseMapper<PimOrderDelayEntity> {

    /**
     * 修改状态
     *
     * @param orderStatus
     * @param orderNum
     */
    @Update("update pim_order_delay set order_status = #{orderStatus} where order_num = #{orderNum}")
    void updateDelayStatus(String orderStatus, String orderNum);

    /**
     * 根据工单号查找
     *
     * @param orderNum
     * @return
     */
    @Select("select * from pim_order_delay where order_num = #{orderNum}")
    PimOrderDelayEntity getByNum(String orderNum);

    /**
     * 统计并更新超时工单的状态
     *
     * @param time
     */
    @Update("update pim_order_delay set time_status = 3 where expected_time < #{time} and order_status not in ('close','hangUp') and time_status " +
            "= 0")
    void summaryAndUpdate(String time);

    @Select("select * from pim_order_delay where order_type_content_id = #{contentId} and order_status not in ('close','hangUp') and time_status " +
            "!= 2 and update_time > #{minusWeeks}")
    List<PimOrderDelayEntity> getIdcOrder(String contentId, String minusWeeks);

    List<PimOrderEntity> searchOrder(Map<String, Object> params);
}
