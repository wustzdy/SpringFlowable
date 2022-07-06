package com.wustzdy.springboot.flowable.demo.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.BeanUtils;
import com.wustzdy.springboot.flowable.demo.entity.SysMailEntity;
import com.wustzdy.springboot.flowable.demo.entity.SysTableEntity;
import com.wustzdy.springboot.flowable.demo.entity.SysUserEntity;
import com.wustzdy.springboot.flowable.demo.util.FreemarkerHelper;
import com.wustzdy.springboot.flowable.demo.vo.EmailVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.wustzdy.springboot.flowable.demo.constant.BasicCloudConstants.WORK_ORDER_TYPE_OPERS;


@Slf4j
@Component
@EnableAsync
public class MailService {

    /**
     * 工单邮件模版
     */
    public static final String WORK_ORDER_MAIL_TEMPLATE = "workOrder";
    /**
     * 工单催办邮件模板
     */
    public static final String URGE_ORDER_MAIL_TEMPLATE = "urgeOrder";
    /**
     * 资源邮件模版
     */
    private static final String RESOURCE_APPLY = "resourceApply";
    @Autowired
    private SysTableDao sysTableDao;

    @Autowired
    private SysMailService sysMailService;

    @Autowired
    private SysUserService sysUserService;

    /**
     * 工单申请邮件
     */
    @Async
    public void sendOrderMessage(EmailVo emailVo, String orderTypeId, String orderTypeContentId) {
        log.info("-sendOrderMessage-EmailVo--:{}", emailVo);
        log.info("-sendOrderMessage-orderTypeId--:{}", orderTypeId);
        log.info("-sendOrderMessage-orderTypeContentId--:{}", orderTypeContentId);

        try {
            setOrderEmailVo(emailVo, orderTypeId, orderTypeContentId);
            // to cc
            setSentEmailUser(emailVo);
            // content
            log.info("----content--emailVo--:{}", emailVo);
            setResourceSendEmialContent(emailVo, WORK_ORDER_MAIL_TEMPLATE);
            // send
            log.info("----send----");
            this.sendMail(emailVo);

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 工单审批提醒邮件
     */
    @Async
    public void sendUrgeOrderMessage(EmailVo emailVo, String orderTypeId, String orderTypeContentId) {
        try {
            setOrderEmailVo(emailVo, orderTypeId, orderTypeContentId);
            // to cc
            setSentEmailUser(emailVo);
            // content
            setResourceSendEmialContent(emailVo, URGE_ORDER_MAIL_TEMPLATE);
            // send
            this.sendMail(emailVo);

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }


    private void setOrderEmailVo(EmailVo emailVo, String serviceType, String operationType) {

        SysTableEntity typeEntity = sysTableDao.selectKeyByTypeAndValue(WORK_ORDER_TYPE_OPERS, serviceType);
        SysTableEntity operEntity = sysTableDao.selectKeyByTypeAndValue(typeEntity.getDataValue(), operationType);
        StringBuilder typeContent = new StringBuilder();
        typeContent.append(typeEntity.getDataKey()).append(SEPARATOR_BLANk);
        if (operEntity != null) {
            typeContent.append(operEntity.getDataKey());
        } else {
            typeContent.append(operationType);
        }
        emailVo.setServiceTitle(typeContent.toString());
        emailVo.setServiceType(typeEntity.getDataKey());
    }

    /**
     * 发送内容
     */
    private void setResourceSendEmialContent(EmailVo emailVo, String mailTemplateName) {
        String mailContent = null;
        String mailSubject = null;
        mailTemplateName = StrUtil.blankToDefault(mailTemplateName, emailVo.getServiceTypeId());
        String serviceTitle = emailVo.getServiceTitle();

        SysMailEntity mailEntity = sysMailService.getOneByMailName(mailTemplateName);
        log.info("--setResourceSendEmialContent--mailEntity:{}", mailEntity);
        if (mailEntity == null) {
            mailEntity = sysMailService.getOneByMailName(RESOURCE_APPLY);
        }
        if (mailEntity != null) {
            try {
                emailVo.setMailSubject(serviceTitle);
                mailSubject = FreemarkerHelper.processTemplate("mailSubject", mailEntity.getMailSubject(), BeanUtils.beanToMap(emailVo));
                mailContent = FreemarkerHelper.processTemplate("mailContent", mailEntity.getMailContent(), BeanUtils.beanToMap(emailVo));
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            if (StringUtils.isBlank(mailSubject)) {
                emailVo.setMailSubject(mailEntity.getMailSubject());
            }
        }
        emailVo.setMailSubject(mailSubject);
        emailVo.setMailContent(mailContent);
    }


    /**
     * 邮件组件配置
     */
    private SysMailEntity mailBaseSetting(EmailVo emailVo) {
        // 发送通知邮件
        SysMailEntity sysMailEntity = new SysMailEntity();
        try {

            sysMailEntity.setCc(emailVo.getCcList());
            sysMailEntity.setTo(emailVo.getToList());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException("邮件发送异常，请联系管理员");
        }
        return sysMailEntity;
    }

    /**
     * 发送mail
     */
    @Async
    public void sendMail(EmailVo emailVo) {
        log.info("--sendMail-emailVo--:{}", emailVo);

        SysMailEntity sysMailEntity = mailBaseSetting(emailVo);
        String mailContent = emailVo.getMailContent();
        String mailSubject = emailVo.getMailSubject();
        if (StringUtils.isNotBlank(mailContent)) {
            sysMailEntity.setMailSubject(mailSubject);
            sysMailEntity.setMailContent(mailContent);
            sysMailService.send(sysMailEntity);
        }
    }

    private void setSentEmailUser(EmailVo emailVo) {
        log.info("----setSentEmailUser--emailVo--:{}", emailVo);

        //主送人
        Set<String> toList = new HashSet<>();
        Set<String> toUserList = new HashSet<>();
        // 抄送人
        Set<String> ccList = new HashSet<>();
        // 结束流程，无审批人
        String applyUser = emailVo.getApplyUser();
        // 账号邮箱
        String applyUserEmail = sysUserService.getEmail(applyUser);
        log.info("----setSentEmailUser--applyUserEmail--:{}", applyUserEmail);

        // 获取申请人信息，是否是主账号，如果是子账号，并且子账号邮箱不等于主账号邮箱信息，需要抄送主账号
        SysUserEntity userEntity = sysUserService.getSysUserByUsername(applyUser);
        log.info("----setSentEmailUser--userEntity--:{}", userEntity);

       /* if (!userEntity.getMaster()) {
            String masterUserEmail = sysUserService.getEmail(userEntity.getMasterUsername());
            if (!applyUserEmail.equals(masterUserEmail)) {
                ccList.add(masterUserEmail);
            }
        }*/
        if (emailVo.getNextStaff().equalsIgnoreCase(NO_BODY)) {
            // apply user
            if (applyUserEmail != null) {
                toList.add(applyUserEmail);
                toUserList.add(applyUser);
            }
        } else {
            // 流程未结束，主送人
            String[] platUsers = emailVo.getNextStaff().split(",");
            // leader
            for (String platUser : platUsers) {
                String platUserEmail = sysUserService.getEmail(platUser);
                log.info("----setSentEmailUser--platUserEmail--:{}", platUserEmail);

                if (platUserEmail == null) {
                    continue;
                }
                toList.add(platUserEmail);
                toUserList.add(platUser);
            }
            // 抄送人
            if (applyUserEmail != null) {
                ccList.add(applyUserEmail);
            }
        }
        log.info("邮件收件人: {}", toList);
        log.info("邮件抄送人: {}", ccList);
        emailVo.setToList(StringUtils.join(toList, SEPARATOR_COMMA));
        emailVo.setCcList(StringUtils.join(ccList, SEPARATOR_COMMA));
        // userMail
        if (toUserList.size() == 1) {
            emailVo.setUserMail(StringUtils.join(toUserList, SEPARATOR_COMMA));
        }
        log.info("---setSentEmailUser--emailVo:{}", emailVo);
    }

    /**
     * 工单关闭邮件
     */
    public void sendOrderAlertCloseMessage(String orderUser, Integer num, String mailName) {
        log.info("--sendOrderAlertCloseMessage-orderUser:{},num:{},mailName:{}", orderUser, num, mailName);
//        SysMailEntity mailEntity = sysMailService.getOneByMailName("orderAlertClose");
        SysMailEntity mailEntity = sysMailService.getOneByMailName(mailName);
        if (mailEntity != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("username", orderUser);
            data.put("num", num);
            mailEntity.setTo(sysUserService.getEmail(orderUser));
            String mailContent = mailEntity.getMailContent();
            String mailSubject = mailEntity.getMailSubject();
            try {
                mailSubject = FreemarkerHelper.processTemplate("mailSubject", mailSubject, data);
                mailContent = FreemarkerHelper.processTemplate("mailContent", mailContent, data);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            mailEntity.setMailContent(mailContent);
            mailEntity.setMailSubject(mailSubject);
            sysMailService.send(mailEntity);
        } else {
            log.warn("{} order alsert close mail template is not exit;", orderUser);
        }
    }
}
