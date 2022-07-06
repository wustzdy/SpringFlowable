package com.wustzdy.springboot.flowable.demo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;


@Data
@TableName("pim_order_records")
public class PimOrderRecordsEntity {

    @TableId
    private Integer id;
    /**
     * 工单编号
     */
    private String orderNum;
    /**
     * 工单记录id
     */
    private Integer orderId;
    /**
     * 工单标题
     */
    private String orderTitle;
    /**
     * 操作人
     */
    private String operationUser;
    /**
     * 部门信息
     */
    private String department;
    /**
     * 工单类型
     */
    private String questionType;
    /**
     * 处理持续时间
     */
    private Long durationTime;

    @TableField(exist = false)
    private String durationTimeStr;
    /**
     * 操作时间
     */
    private Date operatingTime;

}
