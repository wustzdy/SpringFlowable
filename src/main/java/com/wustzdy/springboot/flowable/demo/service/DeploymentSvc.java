package com.wustzdy.springboot.flowable.demo.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author xielianjun
 */
public interface DeploymentSvc {

    void processDeploy(MultipartFile flowFile);
}
