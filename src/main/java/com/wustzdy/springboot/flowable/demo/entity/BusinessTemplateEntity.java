package com.wustzdy.springboot.flowable.demo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.nh.micro.ext.ExtBeanWrapper;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;


@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@TableName("business_template")
public class BusinessTemplateEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 流程模版id
     */
    @TableId
    private Integer templateId;
    /**
     * 流程模版名称
     */
    private String templateName;
    /**
     * 流程模版类型
     */
    private String templateType;
    /**
     * 流程类型，目前有两种，一种是工单模版，一种是资源申请模版
     * <p>
     * 第一种：flowType=resource 资源申请模版，业务类型需要自己填写，类型为资源申请的类型
     * 第二种：flowType=order 工单模版，到工单模版到表里边按照名称找即可
     */
    private String flowType;
    /**
     * 根据流程类型的不同呢，业务类型也不同，
     */
    private String businessType;

    @TableField(exist = false)
    private String businessTypeName;
    /**
     * 业务员操作类型，如果需要细化业务类型的流程，则需要进一步进行业务类型的操作定义
     * 否则设置默认，这个字段将不显示在前端页面
     */
    private String businessOperation;

    @TableField(exist = false)
    private String businessOperationName;
    /**
     * 时间
     */
    private Date gmtCreate;
    /**
     * template_default
     */
    private Integer templateDefault;

    /**
     * 流程启动参数模板
     */
    private ExtBeanWrapper varTmpl;
}