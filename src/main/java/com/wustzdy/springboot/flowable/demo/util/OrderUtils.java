package com.wustzdy.springboot.flowable.demo.util;

import cn.hutool.core.date.DateUtil;
import com.wustzdy.springboot.flowable.demo.constant.FlowConstants;
import com.wustzdy.springboot.flowable.demo.entity.PimOrderEntity;
import com.wustzdy.springboot.flowable.demo.service.MailService;
import com.wustzdy.springboot.flowable.demo.vo.EmailVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static cn.hutool.core.date.DatePattern.NORM_DATETIME_PATTERN;

@Slf4j
@Component
public class OrderUtils {
    private static final String TEST_ENV = "test";

    @Value("${spring.profiles.active}")
    private String active;

    @Autowired
    private MailService mailService;

   /* @Autowired
    private QyWeixinService qyWeixinService;*/

    public String convertTimestampLongToDate(long dateLong) {
        return DateUtil.format(new Date(dateLong), NORM_DATETIME_PATTERN);
    }

    public String convertTimestampStringToDate(String dateStr) {
        long dateLong;
        dateLong = Long.parseLong(dateStr);

        return convertTimestampLongToDate(dateLong);
    }

    public void formatMailContent(String senUser, String copyUser, String comment, PimOrderEntity pimOrderEntity) {
        log.info("--formatMailContent--senUser:{},copyUser:{},comment:{},pimOrderEntity:{}",senUser,copyUser,comment,pimOrderEntity.toString());
        EmailVo emailVo = formatMail(senUser, copyUser, comment, pimOrderEntity);
//        if (!active.equals(TEST_ENV)) {
        mailService.sendOrderMessage(emailVo, pimOrderEntity.getOrderTypeId(), pimOrderEntity.getOrderTypeContentId());
//        }
    }

    public void formatUrgeMailContent(String senUser, String copyUser, String comment, PimOrderEntity pimOrderEntity) {
        //计算超时时间
        Date updateTime = pimOrderEntity.getUpdateTime();
        if (updateTime == null) {
            updateTime = pimOrderEntity.getOrderTime();
        }
        long durationTime = DateUtil.current(false) - updateTime.getTime();
        String format = DateUtils.formatDateTime(durationTime);

        EmailVo emailVo = formatMail(senUser, copyUser, comment, pimOrderEntity);
        emailVo.setDeviceUse(format);
        mailService.sendUrgeOrderMessage(emailVo, pimOrderEntity.getOrderTypeId(), pimOrderEntity.getOrderTypeContentId());
    }

    private EmailVo formatMail(String senUser, String copyUser, String comment, PimOrderEntity pimOrderEntity) {
        String value = FlowConstants.FlowStatus.getStatus(pimOrderEntity.getOrderStatus());
        EmailVo emailVo = new EmailVo();
        emailVo.setApplyReason(pimOrderEntity.getRemark());
        emailVo.setApplyUser(copyUser);
        emailVo.setContent(pimOrderEntity.getOrderDescribe());
        emailVo.setNextStaff(senUser);
        emailVo.setApplyReason(pimOrderEntity.getOrderTitle());
        emailVo.setGmtCreated(LocalDateTimeUtils.formatLocalDateTimeToString(pimOrderEntity.getOrderTime()));
        emailVo.setStat(value);
        emailVo.setServiceTypeId(pimOrderEntity.getOrderTypeId());
        emailVo.setMessage(comment);

        return emailVo;
    }

    public void sendAlertEmail(String orderUser, int num, String mailName) {
        log.info("--sendAlertEmail-orderUser:{},num:{},mailName:{}", orderUser, num, mailName);
        mailService.sendOrderAlertCloseMessage(orderUser, num, mailName);
    }

    /**
     * 通知企业微信
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void notifyQyWeixin(PimOrderEntity pimOrderEntity, String assignee, String comment) {
//        qyWeixinService.notification(pimOrderEntity, assignee, comment);
    }
}
