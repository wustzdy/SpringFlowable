package com.wustzdy.springboot.flowable.demo.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class FlowsProxyInfo implements Serializable {
    private String originUser;
    private String proxyUser;
    private String operation;
}
