package com.wustzdy.springboot.flowable.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;


@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@TableName(BusinessTemplateEntity.TABLE_NAME)
public class BusinessTemplateEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String TABLE_NAME = "business_template";

    /**
     * 流程模版id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Integer id;
    /**
     * 流程模版名称
     */
    @TableField(value = "template_name")
    private String templateName;
    /**
     * 流程模版类型
     */
    @TableField(value = "template_type")
    private String templateType;

    @TableField(value = "business_type")
    private String businessType;

    @TableField(value = "business_operation")
    private String businessOperation;

    @TableField(value = "create_time")
    private Date createTime;

    @TableField(value = "template_default")
    private Integer templateDefault;

    @TableField(value = "create_by")
    private String createBy;

    @TableField(value = "update_by")
    private String updateBy;

    @TableField(value = "update_time")
    private Date updateTime;

}