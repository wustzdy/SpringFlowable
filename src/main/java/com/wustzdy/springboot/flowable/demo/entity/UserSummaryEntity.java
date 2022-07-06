package com.wustzdy.springboot.flowable.demo.entity;

import lombok.Data;


@Data
public class UserSummaryEntity {

    /**
     * 处理人
     */
    private String operationUser;
    /**
     * 工单数量
     */
    private Integer orderCount;
    /**
     * 总时长
     */
    private Long totalTime;

    private String totalTimeStr;
    /**
     * 平均时长
     */
    private Long avgTime;

    private String avgTimeStr;

}
