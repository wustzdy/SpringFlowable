package com.wustzdy.springboot.flowable.demo.vo;

import lombok.Data;


@Data
public class ContentProperties {

    /**
     * 显示属性name
     */
    private String name;
    /**
     * key
     */
    private String key;
    /**
     * 显示属性value
     */
    private Object value;

    public ContentProperties(String name, Object value) {
        this.name = name;
        this.value = value;
    }
}
