package com.wustzdy.springboot.flowable.demo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


@Data
@TableName("pim_order_history")
public class PimOrderHistoryEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @TableId
    private Integer id;
    /**
     * 工单编号
     */
    private String orderNum;
    /**
     * 处理人
     */
    private String userName;
    /**
     *
     */
    private Date dealTime;
    /**
     *
     */
    private String remark;

}
