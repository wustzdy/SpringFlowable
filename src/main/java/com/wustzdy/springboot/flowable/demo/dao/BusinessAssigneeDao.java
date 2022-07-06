package com.wustzdy.springboot.flowable.demo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sensetime.cloud.common.utils.IMap;
import com.sensetime.cloud.workflow.entity.BusinessAssigneeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author author
 * @email author@sensetime.com
 * @date 2019-06-17 10:17:28
 */
@Mapper
public interface BusinessAssigneeDao extends BaseMapper<BusinessAssigneeEntity> {

    @Select("select * from cluster_partition_user where apply_id=#{serviceId}")
    List<IMap> clusterPartitionUser(@Param("serviceId") String serviceId);

    @Select("select username from cluster_partition_admins  where part_id=#{partId}")
    List<String> partitionAdminByPartId(@Param("partId") String partId);

    /**
     * 根据集群名称查询集群信息表中的维护人
     *
     * @param cluster
     * @return
     */
    @Select("select manager from cluster_info where cluster=#{cluster} and manager is not null")
    List<String> clusterManagerBycluster(@Param("cluster") String cluster);

    @Select("select * from cluster_group_apply where apply_id=#{serviceId}")
    List<IMap> clusterGroupApplySql(@Param("serviceId") String serviceId);

    @Select("select admin from cluster_group_info where id=${groupId}")
    List<String> executeSelectSql2Str(@Param("groupId") int groupId);

    /**
     * 根据dns查询域名的owner
     */
    @Select("select * from dns_apply where apply_id=#{serviceId}")
    List<IMap> getDnsId(@Param("serviceId") String serviceId);

    /**
     * 根据顶级子域查询域名owner
     */
    @Select("select user from dns_info where zone=#{zone} and host=#{sub_domain} and status != 0")
    List<String> getDnsOwner(@Param("sub_domain") String subDomain, @Param("zone") String zone);
}
