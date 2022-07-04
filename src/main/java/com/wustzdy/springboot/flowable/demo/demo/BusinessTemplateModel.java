package com.wustzdy.springboot.flowable.demo.demo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;


@Data
@Accessors(chain = true)
@ToString(callSuper = true)
public class BusinessTemplateModel implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;

    private String templateName;

    private String templateType;

    private String businessType;

    private String businessOperation;

    private Date createTime;

    private Integer templateDefault;

    private String createBy;

    private String updateBy;

    private Date updateTime;
}
