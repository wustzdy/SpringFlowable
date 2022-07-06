/**
 *
 */
package com.wustzdy.springboot.flowable.demo.service;


import com.wustzdy.springboot.flowable.demo.vo.ResultVo;

import java.util.Map;



public interface IStartTaskSvc {

    /**
     * 启动流程
     */
    ResultVo doStartProcess(String processDefinitionKey, String assignee, String businessKey, String title);


    ResultVo doStartProcess(String processDefinitionKey, String assignee, String businessKey, Map<String, Object> variables);

    void flushCurrentTaskVariables();
}
