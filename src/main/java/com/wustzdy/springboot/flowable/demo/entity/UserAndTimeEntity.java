package com.wustzdy.springboot.flowable.demo.entity;

import lombok.Data;


@Data
public class UserAndTimeEntity {

    /**
     * 处理人
     */
    private String operationUser;
    /**
     * 处理持续时间
     */
    private Long durationTime;

}
