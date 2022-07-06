package com.wustzdy.springboot.flowable.demo.entity;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wustzdy.springboot.flowable.demo.vo.ContentProperties;
import com.wustzdy.springboot.flowable.demo.vo.FlowContent;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;
import java.util.List;


@Data
@TableName("business_flows")
public class BusinessFlowsEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;
    /**
     * 服务类型
     */
    @NotBlank
    private String serviceType;
    /**
     * 对应业务id
     */
    @NotBlank
    private String serviceId;
    /**
     * 流程内容
     */
    @NotBlank
    private String content;
    /**
     * 流程发起人
     */
    @NotBlank
    private String applyUser;

    /**
     * done reject process revocation
     */
    private String jobStat;
    /**
     * 申请理由
     */
    @NotBlank
    private String applyReason;
    /**
     * 创建时间
     */
    private Date gmtCreated;
    /**
     * 修改时间
     */
    private Date gmtModified;

    /**
     * 流程定义key
     */
    private String processDefinitionKey;
    /**
     * 流程实例id
     */
    private String processInstanceId;
    /**
     * 申请使用时长
     */
    private Integer applyDate;
    /**
     * 动作
     */
    private String operationType;
    /**
     * 下一审批人(该记录作为发送邮件的邮件接受人，即：mailservice管理该字段)
     */
    private String nextStaff;
    /**
     * 流程模版id
     */
    private Integer businessTemplateId;
    /**
     * owner
     */
    private String owner;

    private String extend;

    /**
     * 实例id  业务id的关系= 1对多
     */
    private String instanceId;
    /**
     * 流程定义id
     */
    private String processDefinitionId;

    /**
     * 部门信息
     */
    private String departName;
    /**
     * 回调结果
     */
    private String result;

    public String mailContent() {
        FlowContent flowContent = JSON.parseObject(content, FlowContent.class);
        List<ContentProperties> list = flowContent.getList();
        StringBuilder result = new StringBuilder();
        for (ContentProperties contentProperties : list) {
            String name = contentProperties.getName();
            Object value = contentProperties.getValue();
            result.append("【").append(name).append("】")
                    .append(value)
                    .append("<br>");
        }
        return result.toString();
    }
}
