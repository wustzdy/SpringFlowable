package com.wustzdy.springboot.flowable.demo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("business_flows_proxy_record")
public class BusinessFlowsProxyRecordEntity implements Serializable {
    @TableId
    private Integer id;

    /**
     * 代理实例ID
     */
    private Integer proxyInstId;

    private String commentId;

    private String operation;

    private Date createTime;
}
