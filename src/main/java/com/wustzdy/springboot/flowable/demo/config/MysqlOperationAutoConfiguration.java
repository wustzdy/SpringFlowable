package com.wustzdy.springboot.flowable.demo.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties({OperationLiquibaseDatasourceConfig.class})
public class MysqlOperationAutoConfiguration {

    @Autowired
    private OperationLiquibaseDatasourceConfig liquibaseDataSourceProperties;

    private DataSource liquibaseDataSource() {
      /*  HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        System.out.println("sss: " + liquibaseDataSourceProperties.getUrl());
        config.setJdbcUrl(liquibaseDataSourceProperties.getUrl());
        config.setUsername(liquibaseDataSourceProperties.getUsername());
        config.setPassword(liquibaseDataSourceProperties.getPassword());
        config.setConnectionTestQuery("SELECT 1");
//        config.setMaximumPoolSize(liquibaseDataSourceProperties.getMaxPoolSize());
//        config.setConnectionTimeout(liquibaseDataSourceProperties.getConnectionTimeout());
//        config.setMaxLifetime(liquibaseDataSourceProperties.getMaxLifetime());
        return new HikariDataSource(config);*/

        DruidDataSource datasource = new DruidDataSource();
        datasource.setUrl(liquibaseDataSourceProperties.getUrl());
        datasource.setDriverClassName("com.mysql.jdbc.Driver");
        datasource.setUsername("root");
        datasource.setPassword("123456");
//        logger.info("密码：" + ConfigTools.decrypt(publicKey, password));
       /* datasource.setInitialSize(initialSize);
        datasource.setMinIdle(minIdle);
        datasource.setMaxActive(maxActive);
        datasource.setMaxWait(maxWait);
        datasource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        datasource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        datasource.setValidationQuery(validationQuery);
        datasource.setTestWhileIdle(testWhileIdle);
        datasource.setTestOnBorrow(testOnBorrow);
        datasource.setTestOnReturn(testOnReturn);
        datasource.setUseGlobalDataSourceStat(useGlobalDataSourceStat);
        datasource.setConnectProperties(connectProperties);*/
        return datasource;
    }

    @Bean
    public SpringLiquibase operationLiquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(liquibaseDataSource());
        liquibase.setChangeLog("classpath:operation-changelog.xml");
        liquibase.setContexts("development,test,production");
        liquibase.setShouldRun(true);
        return liquibase;
    }


}
