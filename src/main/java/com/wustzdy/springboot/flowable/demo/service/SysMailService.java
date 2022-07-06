package com.wustzdy.springboot.flowable.demo.service;


import com.wustzdy.springboot.flowable.demo.entity.SysMailEntity;

public interface SysMailService {

    void send(SysMailEntity sysMail);

    SysMailEntity getOneByMailName(String mailName);

}

