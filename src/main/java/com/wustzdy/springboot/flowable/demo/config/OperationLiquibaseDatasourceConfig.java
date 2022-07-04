package com.wustzdy.springboot.flowable.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.datasource")
@Data
public class OperationLiquibaseDatasourceConfig {

    private String driver;
    private String url;
    private String username;
    private String password;
    private int maxPoolSize;
    private int connectionTimeout;
    private int maxLifetime;


}
