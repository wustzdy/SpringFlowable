package com.wustzdy.springboot.flowable.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wustzdy.springboot.flowable.demo.dao.BusinessFlowsProxyInstanceDao;
import com.wustzdy.springboot.flowable.demo.entity.BusinessFlowsProxyInstanceEntity;
import com.wustzdy.springboot.flowable.demo.entity.BusinessFlowsProxyRecordEntity;
import com.wustzdy.springboot.flowable.demo.entity.SysTableEntity;
import com.wustzdy.springboot.flowable.demo.service.BusinessFlowsProxyInstanceService;
import com.wustzdy.springboot.flowable.demo.service.BusinessFlowsProxyRecordService;
import com.wustzdy.springboot.flowable.demo.service.SysTableService;
import com.wustzdy.springboot.flowable.demo.util.PageUtils;
import com.wustzdy.springboot.flowable.demo.vo.FlowsProxyInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Transactional(rollbackFor = Exception.class)
@Service("businessFlowsProxyInstanceService")
public class BusinessFlowsProxyInstanceServicelmpl extends ServiceImpl<BusinessFlowsProxyInstanceDao,
        BusinessFlowsProxyInstanceEntity> implements BusinessFlowsProxyInstanceService {
    @Autowired
    private BusinessFlowsProxyRecordService proxyRecordService;

    @Autowired
    private SysTableService sysTableService;

    @Override
    public void checkAndDoProxy(String username, String taskId, String commentId, String operation) {
        List<BusinessFlowsProxyInstanceEntity> instanceEntityList = this.getBaseMapper().selectList(
                new QueryWrapper<BusinessFlowsProxyInstanceEntity>()
                        .eq("proxy_user", username)
                        .eq("task_id", taskId)
                        .orderByDesc("create_time"));
        if (Objects.nonNull(instanceEntityList) && instanceEntityList.size() > 0) {
            BusinessFlowsProxyInstanceEntity instanceEntity = instanceEntityList.get(0);
            BusinessFlowsProxyRecordEntity recordEntity = new BusinessFlowsProxyRecordEntity();
            recordEntity.setCommentId(commentId);
            recordEntity.setOperation(operation);
            recordEntity.setProxyInstId(instanceEntity.getId());
            proxyRecordService.save(recordEntity);
        }
    }

    @Override
    public String getProxyApprover(String proxyUser, String taskId, String commentId) {
        String dealedCommentUser = "";
        try {
            Optional<FlowsProxyInfo> optional = getProxyInfo(proxyUser, taskId, commentId);
            if (optional.isPresent()) {
                FlowsProxyInfo flowsProxyInfo = optional.get();
                if (Strings.isNotBlank(flowsProxyInfo.getOriginUser()) && Strings.isNotBlank(flowsProxyInfo.getProxyUser())
                        && Strings.isNotBlank(flowsProxyInfo.getOperation())) {
                    SysTableEntity sysTableEntity = sysTableService.selectByTypeAndValue("proxyOperations", flowsProxyInfo.getOperation());
                    if (Objects.nonNull(sysTableEntity)) {
                        dealedCommentUser = flowsProxyInfo.getProxyUser() + " 代 " + flowsProxyInfo.getOriginUser() + " " + sysTableEntity.getDataKey();
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取代理审批批注信息失败：" + e.getMessage());
        }
        return dealedCommentUser;
    }

    private Optional<FlowsProxyInfo> getProxyInfo(String proxyUser, String taskId, String commentId) {
        BusinessFlowsProxyInstanceEntity instanceEntity = this.getBaseMapper().selectOne(
                new QueryWrapper<BusinessFlowsProxyInstanceEntity>()
                        .eq("proxy_user", proxyUser)
                        .eq("task_id", taskId));
        if (Objects.nonNull(instanceEntity)) {
            BusinessFlowsProxyRecordEntity recordEntity = proxyRecordService.getBaseMapper().selectOne(
                    new QueryWrapper<BusinessFlowsProxyRecordEntity>()
                            .eq("proxy_inst_id", instanceEntity.getId())
                            .eq("comment_id", commentId));
            if (Objects.nonNull(recordEntity)) {
                FlowsProxyInfo proxyInfo = new FlowsProxyInfo();
                proxyInfo.setOriginUser(instanceEntity.getOriginUser());
                proxyInfo.setProxyUser(instanceEntity.getProxyUser());
                proxyInfo.setOperation(recordEntity.getOperation());
                return Optional.of(proxyInfo);
            }
        }
        return Optional.ofNullable(null);
    }

    @Override
    public PageUtils<BusinessFlowsProxyInstanceEntity> getProxyInstances(Map<String, Object> params) {
        IPage<BusinessFlowsProxyInstanceEntity> page = new Page<>();
        int currPage = Integer.parseInt(params.getOrDefault("page", 1).toString());
        int pageSize = Integer.parseInt(params.getOrDefault("limit", 10).toString());
        page.setCurrent(currPage);
        page.setSize(pageSize);
        QueryWrapper<BusinessFlowsProxyInstanceEntity> queryWrapper = new QueryWrapper<>();
        String[] searchFields = new String[]{"origin_user", "proxy_user", "task_id"};
        for (String field : searchFields) {
            if (params.containsKey(field) && StringUtils.isNotBlank(params.get(field).toString())) {
                String fieldValue = params.get(field).toString();
                queryWrapper.eq(field, fieldValue);
            }
        }
        queryWrapper.orderByDesc("create_time");
        page = this.getBaseMapper().selectPage(page, queryWrapper);
        return new PageUtils<>(page);
    }
}
