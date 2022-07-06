package com.wustzdy.springboot.flowable.demo.vo;

import lombok.Data;

import java.util.List;

@Data
public class FlowHistoryVo {
    List<TaskCommentVo> completed;
    List<TaskCommentVo> completedMP;
    List<TaskVo> current;
}
