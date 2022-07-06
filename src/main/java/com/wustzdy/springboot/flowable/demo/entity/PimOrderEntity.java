package com.wustzdy.springboot.flowable.demo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@TableName("pim_order")
public class PimOrderEntity implements Serializable {
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
     * 工单标题
     */
    private String orderTitle;
    /**
     * 工单描述
     */
    private String orderDescribe;
    /**
     * 提交账号
     */
    private String orderUser;
    /**
     * 提交时间
     */
    private Date orderTime;
    /**
     * 工单状态
     */
    private String orderStatus;
    /**
     * 工单类型id
     */
    private String orderTypeId;
    /**
     * 工单类型内容细分id
     */
    private String orderTypeContentId;
    /**
     * 优先级
     */
    private Integer orderPriority;

    /**
     * 其他描述
     */
    private String otherDescribe;
    /**
     * 评价内容
     */
    private String orderEvaluate;
    /**
     * 整体评价
     */
    private Integer orderStar;
    /**
     * 结束时间
     */
    private Date endTime;
    /**
     * data
     */
    private String dataJson;
    /**
     * labelWidth
     */
    private Integer labelWidth;

    /**
     * 流程定义key
     */
    private String processDefinitionKey;
    private String processDefinitionId;
    private Date updateTime;

    /**
     * 流程实例id
     */
    private String processInstanceId;
    /**
     * 动作
     */
    private String operationType;
    /**
     * 下一审批人
     */
    private String nextStaff;
    /**
     * 流程模版id
     */
    private Integer businessTemplateId;
    /**
     * 问题类型 ask approve
     */
    private String questionType;
    /**
     * // 扩展字段 1：作为训练集群特殊处理字段，放入分区id
     */
    private String extend;

    /**
     * 当前处理人
     */
    private String currentDealUser;
    /**
     * 工单类型id
     */
    @TableField(exist = false)
    private String orderType;
    /**
     * 工单类型内容细分id
     */
    @TableField(exist = false)
    private String orderTypeContent;

    @TableField(exist = false)
    private List<Tkv> list;

    @TableField(exist = false)
    private String taskId;

    @TableField(exist = false)
    private String durationTime;

    @TableField(exist = false)
    private Date dealTime;

    @TableField(exist = false)
    private String remark;

    @TableField(exist = false)
    private Map<String, Object> vars;

    @TableField(exist = false)
    private String approveTime;

    @TableField(exist = false)
    private String handingTime;

    @TableField(exist = false)
    private String sreTime;

    @Data
    public static class Tkv {

        private String type;
        private String key;
        private Object value;

        public Tkv(String type, String key, Object value) {
            this.type = type;
            this.key = key;
            this.value = value;
        }
    }
}
