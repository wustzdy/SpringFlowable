package com.wustzdy.springboot.flowable.demo.service;

import cn.hutool.core.util.StrUtil;
import com.wustzdy.springboot.flowable.demo.entity.BusinessFlowsEntity;
import com.wustzdy.springboot.flowable.demo.entity.PimOrderEntity;
import com.wustzdy.springboot.flowable.demo.util.OrderUtils;
import com.wustzdy.springboot.flowable.demo.vo.ResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

import static com.wustzdy.springboot.flowable.demo.constant.BasicCloudConstants.INIT;

@Slf4j
@Service
public class PimOrderService {
    @Autowired
    private BasicFlowService basicFlowService;
    @Autowired
    private OrderUtils orderUtils;
    @Autowired
    private ActTaskService actTaskService;


    public void startWorkflowAndSave(PimOrderEntity pimOrder, String userName, String orderNum) {
        log.info("--startWorkflowAndSave--pimOrder-:{}", pimOrder);
        log.info("--startWorkflowAndSave--userName-:{}", userName);
        log.info("--startWorkflowAndSave--orderNum-:{}", orderNum);

        String orderTypeId = pimOrder.getOrderTypeId();
        String orderTypeContentId = pimOrder.getOrderTypeContentId();
        // valid mutli data
        PimOrderEntity pimOrderEntity = pimOrderDao.getByOrderNum(orderNum);
        if (pimOrderEntity != null) {
            throw new RuntimeException("工单已存在，请勿重复提交");
        }
        // base info
        pimOrder.setOrderUser(userName);
        pimOrder.setOrderTime(new Date());
        pimOrder.setOrderStatus(INIT);
        pimOrder.setOrderNum(orderNum);
        pimOrder.setOperationType(orderTypeContentId);
        pimOrderDao.insert(pimOrder);
        // workflow
        BusinessFlowsEntity businessFlowsEntity = new BusinessFlowsEntity();
        businessFlowsEntity.setServiceId(orderNum);
        businessFlowsEntity.setContent(pimOrder.getOrderTitle());
        businessFlowsEntity.setApplyUser(userName);
        businessFlowsEntity.setApplyReason(pimOrder.getOrderDescribe());
        businessFlowsEntity.setServiceType(orderTypeId);
        businessFlowsEntity.setOperationType(orderTypeContentId);
        // 扩展字段 1：作为训练集群特殊处理字段，放入分区idc
        businessFlowsEntity.setExtend(pimOrder.getExtend());
        // 启动流程 使用当前对象获取到流程定义的key（对象的名称就是流程定义的businessKey）
        ResultVo resultVo = basicFlowService.startAndPassProcess(orderNum, userName, businessFlowsEntity, pimOrder.getVars());
        log.info("--startWorkflowAndSave-resultVo-:{}", resultVo);

        if (resultVo.getAssignee() != null) {
            // 发送邮件
            pimOrder.setNextStaff(resultVo.getAssignee());
            String nextStaff = pimOrder.getNextStaff();
            nextStaff = actTaskService.filterByWhitelist(nextStaff);

            log.info("--startWorkflowAndSave-nextStaff-:{}", nextStaff);

            if (StrUtil.isNotBlank(nextStaff)) {
                orderUtils.formatMailContent(nextStaff, pimOrder.getOrderUser(),
                        pimOrder.getOrderDescribe(), pimOrder);
                // 推送企业微信消息
                //reomve notifyQyWeixin
//                orderUtils.notifyQyWeixin(pimOrder, nextStaff, pimOrder.getOrderDescribe());
            }
        } else {
            log.debug(" assignee is null");
        }


    }
}
