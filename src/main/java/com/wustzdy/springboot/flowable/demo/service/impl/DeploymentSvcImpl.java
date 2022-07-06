package com.wustzdy.springboot.flowable.demo.service.impl;


import com.wustzdy.springboot.flowable.demo.service.DeploymentSvc;
import com.wustzdy.springboot.flowable.demo.service.IActivitiUtilSvc;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author xielianjun
 */
@Slf4j
@Service("deploymentSvc")
@Transactional(rollbackFor = Exception.class)
public class DeploymentSvcImpl implements DeploymentSvc {

    @Autowired
    private IActivitiUtilSvc iActivitiUtilSvc;

    @Override
    public void processDeploy(MultipartFile flowFile) {
        //调用流程发布，返回流程定义
        ProcessDefinition processDefinition = iActivitiUtilSvc.deployProcess(flowFile);

        String name = processDefinition.getName();
        String key = processDefinition.getKey();
        int version = processDefinition.getVersion();

        log.info("name is {},key is {},version is {}", name, key, version);
    }

}
