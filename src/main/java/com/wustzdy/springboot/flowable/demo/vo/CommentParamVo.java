package com.wustzdy.springboot.flowable.demo.vo;

import lombok.Data;


@Data
public class CommentParamVo {

    private String processInstanceId;

    private String userName;

    private String comment;

}
