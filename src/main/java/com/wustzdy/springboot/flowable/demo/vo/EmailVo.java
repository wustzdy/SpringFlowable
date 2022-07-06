package com.wustzdy.springboot.flowable.demo.vo;


import lombok.Data;


@Data
public class EmailVo {
    /**
     * 服务类型
     */
    private String serviceType;

    private String serviceTypeId;
    /**
     * 流程内容
     */
    private String content;
    /**
     * 流程发起人
     */
    private String applyUser;
    /**
     * 流程状态. process处理中，revocation 撤销，reject 拒绝，done 完结
     */
    private String stat;

    private String applyReason;
    /**
     * 当前审批人
     */
    private String nextStaff;

    private String message;

    private String gmtCreated;

    private String deviceUse;

    /**
     * 资源类型标题
     */
    private String serviceTitle;
    /**
     * 邮件主题
     */
    private String mailSubject;
    /**
     * 邮件内容
     */
    private String mailContent;

    private String toList;
    private String ccList;

    /**
     * 尊敬的用户
     */
    private String userMail;

}
