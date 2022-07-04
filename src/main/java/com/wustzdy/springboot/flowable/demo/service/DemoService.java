package com.wustzdy.springboot.flowable.demo.service;

import com.alibaba.druid.support.spring.stat.SpringStatUtils;
import com.wustzdy.springboot.flowable.demo.demo.Demo;
import com.wustzdy.springboot.flowable.demo.entity.DemoEntity;
import com.wustzdy.springboot.flowable.demo.mapper.DemoMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

import static org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE;

@Service
public class DemoService {
    @Autowired
    private DemoMapper demoMapper;

    @Transactional
    public Demo add(Demo demo) {
        DemoEntity demoEntity = new DemoEntity();
        BeanUtils.copyProperties(demo, demoEntity);
        demoMapper.insert(demoEntity);

        Demo resDemo = new Demo();
        BeanUtils.copyProperties(demoEntity, resDemo);
        resDemo.setId(demoEntity.getId());
        return resDemo;
    }

}
