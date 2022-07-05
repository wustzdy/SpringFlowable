package com.wustzdy.springboot.flowable.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@TableName(BusinessAssignEntity.TABLE_NAME)
public class BusinessAssignEntity {
    public static final String TABLE_NAME = "business_assignee";

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField(value = "activity_id")
    private String activityId;

    @TableField(value = "assignee")
    private String assignee;

    @TableField(value = "order_type")
    private String orderType;

    @TableField(value = "create_time")
    private Date createTime;

    @TableField(value = "process_definition_id")
    private String processDefinitionId;

    @TableField(value = "activity_name")
    private String activityName;

    @TableField(value = "send_mail")
    private int sendMail;

    @TableField(value = "business_template_id")
    private String businessTemplateId;

    @TableField(value = "flow_type")
    private String flowType;
}