package com.wustzdy.springboot.flowable.demo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("business_flows_proxy_inst")
public class BusinessFlowsProxyInstanceEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    /**
     * taskId
     */
    private String taskId;

    /**
     * 委托人
     */
    private String originUser;

    /**
     * 代理人
     */
    private String proxyUser;

    private Date createTime;
}
