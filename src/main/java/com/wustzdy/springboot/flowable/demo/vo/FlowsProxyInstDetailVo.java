package com.wustzdy.springboot.flowable.demo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlowsProxyInstDetailVo implements Serializable {
    private Integer proxyInstId;
    private String originUser;
    private String proxyUser;
    private String taskId;
    private String serviceId;
    private String processInstanceId;
    private String templateName;
    private String templateType;
    private String businessType;
    private String businessOperation;
    private String flowType;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    private Boolean hasRecords;
}