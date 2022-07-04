package com.wustzdy.springboot.flowable.demo.demo;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
@ToString(callSuper = true)
public class BusinessTemplateModel {
    private String templateName;

    private String templateType;

    private String businessType;
}
