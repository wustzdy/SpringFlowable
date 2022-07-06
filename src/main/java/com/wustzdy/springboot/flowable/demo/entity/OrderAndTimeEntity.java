package com.wustzdy.springboot.flowable.demo.entity;

import lombok.Data;


@Data
public class OrderAndTimeEntity {

    /**
     * 工单编号
     */
    private String orderNum;
    /**
     * 工单标题
     */
    private String orderTitle;
    /**
     * 处理持续时间
     */
    private Long durationTime;
    /**
     * 处理持续时间
     */
    private String durationTimeStr;

}
