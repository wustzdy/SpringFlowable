package com.wustzdy.springboot.flowable.demo.vo;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class FlowContent {

    private String name;

    private List<ContentProperties> list = new LinkedList<>();

    public static FlowContent build() {
        return new FlowContent();
    }

    public FlowContent setName(String name) {
        this.name = name;
        return this;
    }

    public FlowContent add(String name, Object value) {
        ContentProperties contentProperties = new ContentProperties(name, value);
        list.add(contentProperties);
        return this;
    }
}
