package com.wustzdy.springboot.flowable.demo.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class BusinessFlowsProxyVo implements Serializable {
    private Integer proxySyncId;
    private String originUser;
    private String proxyUser;
    private String createUser;
    private String comment;
}
