package com.wustzdy.springboot.flowable.demo.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TraceProcessVo {
    String startId;
    Map<String, List<String>> flowMap;
    Map<String, List<Map<String, Object>>> taskDefKeyMap;
    List<Map<String, Object>> activityInfos;

}