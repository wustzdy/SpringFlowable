package com.wustzdy.springboot.flowable.demo.constant;

import java.util.Arrays;
import java.util.List;


public class FlowConstants {

    /**
     * 流程标志
     */
    public static final String MP_FLAG = "mpFlag";
    public static final String PROXY_FLAG = "proxyFlag";
    public static final String SILENT_FLAG = "silentFlag";
    public static final String TMPL_PRIOR_FLAG = "tmplPriorFlag";
    public static final String DEBUG_LINK = "debugLink";

    /**
     * 工单模版操作
     */
    public static final String FLOW_OPER_PASS = "pass";
    public static final String FLOW_OPER_TRANSFER = "transfer";
    /**
     * 训练集群特殊处理
     */
    public static final String ORDER_TYPE_CLUSTER_TRAIN = "order_type_cluster_train";

    /**
     * 平台管理员
     */
/*    public static final String PLAT_APPROVE_USER = "sanghongyu,yuanchengrui,xielianjun";
    public static final String ORDER_APPROVE_USER = "liuying_vendor";*/

   /* public static final String PLAT_APPROVE_USER = "zhudayang,yangguang3";
    public static final String ORDER_APPROVE_USER = "zhudayang";*/
    public static final String NO_BODY = "NoBody";

    /**
     * 流程返回结果状态常量
     */
    public static final String FLOW_STAT_END = "done";
    public static final String FLOW_STAT_REJECT = "reject";
    public static final String FLOW_STAT_BACK = "back";
    public static final String FLOW_STAT_REVOCATION = "revocation";
    public static final String FLOW_STAT_CLOSE = "close";
    public static final String FLOW_STAT_DELETE = "delete";
    public static final String FLOW_STAT_PROCESS = "process";
    public static final String FLOW_HANG_UP = "hangUp";


    /**
     * 流程运行常量
     */
    public final static String END = "END";
    /**
     * 流程审批人参数
     */
    public static final String APPLICANT = "applicant";//申请人:1
    public static final String LEADER = "leader";//直接汇报人:2
    public static final String THIRD_DEPARTMENT = "thirdDepartment";//部门列表人员
    public static final String PLAT_MANAGER = "platManager";//平台管理员:5
    public static final String RESOURCE_OWNER = "owner";//owner:8
    public static final String CLUSTER_MANAGER = "clusterManager";
    public static final String HPC_MANAGER = "hpcManager";//HPC分区管理员:6
    public static final String ORDER_HPC_MANAGER = "orderHpcManager";//分区管理员（工单):3
    public static final String ORDER_HPC_CLUSTER_MANAGER = "orderHpcClusterManager";//集群维护人（工单):4
    public static final String DNS_OWNER = "dnsOwner";//DNS域名owner:7
    /**
     * 关联上边的【流程审批人参数】，请定义上边的时候别忘了，在此处列表中加入数据
     */
    public static final List ASSIGN_LIST = Arrays.asList(APPLICANT,
            LEADER, THIRD_DEPARTMENT, PLAT_MANAGER, CLUSTER_MANAGER,
            RESOURCE_OWNER, HPC_MANAGER, ORDER_HPC_MANAGER, DNS_OWNER, ORDER_HPC_CLUSTER_MANAGER);

    /**
     * 流程状态. process处理中，revocation 撤销，reject 拒绝，done 完结
     */
    public enum FlowStatus {
        /**
         * 流程状态. process处理中，revocation 撤销，reject 拒绝，done 完结
         * close
         * done
         * init
         * process
         * reject
         */
        TRANSFER("transfer", "工单处理中"),
        REVOCATION("revocation", "工单撤销"),
        INIT("init", "工单待处理"),
        PROCESS("process", "工单进行中"),
        CLOSE("close", "工单关闭"),
        REJECT("reject", "工单拒绝"),
        DONE("done", "工单结束");

        private String key;
        private String value;

        FlowStatus(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public static String getStatus(String jobStat) {
            FlowStatus[] values = FlowStatus.values();
            for (FlowStatus flowStatus :
                    values) {
                if (jobStat.equals(flowStatus.key)) {
                    return flowStatus.value;
                }
            }
            return FlowStatus.PROCESS.value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }

}
