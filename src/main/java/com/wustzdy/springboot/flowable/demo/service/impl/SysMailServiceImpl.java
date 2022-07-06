package com.wustzdy.springboot.flowable.demo.service.impl;


import com.wustzdy.springboot.flowable.demo.entity.SysMailEntity;
import com.wustzdy.springboot.flowable.demo.service.SysMailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@SuppressWarnings("all")
@Slf4j
@Service("sysMailService")
public class SysMailServiceImpl implements SysMailService {

    @Autowired
//    private OperationProvider operationProvider;

    @Override
    public void send(SysMailEntity sysMail) {
        log.info("开始发送邮件sysMail-getTo:{},getCc:{}", sysMail.getTo(), sysMail.getCc());
//        Boolean sendFlag = operationProvider.sendMail(sysMail).getData();
//        log.info("发送邮件sendFlag:{}，邮件接受人:{}", sendFlag, sysMail.getTo());
    }

    @Override
    public SysMailEntity getOneByMailName(String mailName) {
        log.info("--getOneByMailName-mailName-:{}", mailName);
//        Result<SysMailEntity> mailEntity = operationProvider.getEmailConfigByMailName(mailName);
//        log.info("--getOneByMailName-mailEntity-:{}", mailEntity.toString());
//        RestResultUtils.valid(mailEntity);
//        return mailEntity.getData();
        return null;

    }

}