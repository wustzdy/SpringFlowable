package com.wustzdy.springboot.flowable.demo.service;



import com.wustzdy.springboot.flowable.demo.util.PageUtils;
import com.wustzdy.springboot.flowable.demo.vo.*;

import java.util.Map;

public interface BusinessFlowsProxyService {

    FlowsProxyDetailVo getProxyUsers(String nextUser);

    String getOneProxyUser(String originUser);

    void syncFlowProxy(BusinessFlowsProxyVo flowsProxyVo);

    FlowsProxyVo getProxyUserInfo(String nextUser);

    void checkAndDoProxy(String userName, String taskId, String commentId, String operation);

    void checkAndDoProxy(String userName, ResultVo resultVo, String operation);

    void saveProxyInstance(FlowsProxyVo flowsProxyVo, ResultVo resultVo);

    void saveProxyInstance(FlowsProxyVo flowsProxyVo, String taskId);

    PageUtils<FlowsProxyInstDetailVo> selectProxyInstByParam(Map<String, Object> params);
}
