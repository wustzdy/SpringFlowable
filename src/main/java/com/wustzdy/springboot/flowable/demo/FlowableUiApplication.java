package com.wustzdy.springboot.flowable.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;

@SpringBootApplication
@ComponentScan(basePackages = {"com.wustzdy.springboot.flowable.demo",
        "org.flowable.ui.modeler",
        "org.flowable.ui.common"})
@MapperScan("com.wustzdy.springboot.flowable.demo.mapper")
public class FlowableUiApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlowableUiApplication.class, args);
    }

}
