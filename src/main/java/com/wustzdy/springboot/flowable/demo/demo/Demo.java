package com.wustzdy.springboot.flowable.demo.demo;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@ToString(callSuper = true)
public class Demo implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;

    private String questionType;

    private String description;

    private String contact;

    private String contactTelephone;

    private String contactEmail;

    private String corporateName;
}
