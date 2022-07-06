package com.wustzdy.springboot.flowable.demo.vo;

import lombok.Data;


@Data
public class NextTask {

    private String id;

    private String name;

    public NextTask(Object id, Object name) {
        this.id = (String) id;
        this.name = (String) name;
    }
}
