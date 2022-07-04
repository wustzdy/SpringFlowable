package com.wustzdy.springboot.flowable.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@TableName(DemoEntity.TABLE_NAME)
public class DemoEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String TABLE_NAME = "demo";

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField(value = "question_type")
    private String questionType;

    @TableField(value = "description")
    private String description;

    @TableField(value = "contact")
    private String contact;

    @TableField(value = "contact_telephone")
    private String contactTelephone;

    @TableField(value = "contact_email")
    private String contactEmail;

    @TableField(value = "corporate_name")
    private String corporateName;
}