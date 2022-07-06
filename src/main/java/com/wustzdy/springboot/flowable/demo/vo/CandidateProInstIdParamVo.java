package com.wustzdy.springboot.flowable.demo.vo;

import lombok.Data;
import lombok.NonNull;


@Data
public class CandidateProInstIdParamVo {
    @NonNull
    private String assignee;
    @NonNull
    private String taskId;
    private String userName;

    private String comment;

}
