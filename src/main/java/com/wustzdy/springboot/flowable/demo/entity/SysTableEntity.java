package com.wustzdy.springboot.flowable.demo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("sys_table")
public class SysTableEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;
    /**
     * 数据类型
     */
    private String dataType;
    /**
     * 数据key
     */
    private String dataKey;
    /**
     * 数据值
     */
    private String dataValue;
    /**
     * 备注
     */
    private String remark;

    /**
     * 管理员专用
     */
    private Boolean adminFlag;

    /**
     * 是否热推
     */
    private Boolean isHot;

    /**
     * 排序号
     */
    private Integer sortable;

    private String extend;

}
