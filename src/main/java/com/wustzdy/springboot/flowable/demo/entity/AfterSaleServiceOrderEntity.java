package com.wustzdy.springboot.flowable.demo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@TableName(AfterSaleServiceOrderEntity.TABLE_NAME)
public class AfterSaleServiceOrderEntity {
    public static final String TABLE_NAME = "salesorder_after_sale_service_order";

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