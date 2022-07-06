package com.wustzdy.springboot.flowable.demo.constant;


public final class BasicCloudConstants {
    /**
     * 待审批
     */
    public static final String PROCESS = "process";
    /**
     * 工单初始化
     */
    public static final String INIT = "init";


    public static final String ASK = "ask";

    public static final String APPROVE = "approve";

    public static final String X_CLIENT_TOKEN_USER = "x-client-token-user";
    public static final String X_CLIENT_TOKEN_USER_ID = "userId";

    public static final String IDC_COUNT = "idc_count";

    public static final String SRE_LEADER = "qiaobinbin";
    /**
     * 服务间调用的认证token
     */
    public static final String SERVER_NAME = "【工单服务】";

    /**
     * 工单字典表dataType
     */
    public static final String WORK_ORDER_TYPE_OPERS = "orderType";

    /**
     * 文件类型
     */
    public final static String ZIP = "zip";
    public final static String BAR = "bar";

    /**
     * 服务类型serviceType
     */
    public static final String OPENCLOUD_MYSQL = "OpenCloud-MySQL";
    public static final String OPENCLOUD_MONGODB = "OpenCloud-MongoDB";
    public static final String OPENCLOUD_REDIS = "OpenCloud-Redis";
    public static final String OPENCLOUD_VM = "OpenCloud-VM";
    //    public static final String OPENCLOUD_KAFKA = "OpenCloud-Kafka";
    public static final String OPENCLOUD_ELASTICSEARCH = "OpenCloud-ElasticSearch";
    public static final String OPENCLOUD_EMAIL = "OpenCloud-Email";
    public static final String CAPACITY = "capacity";
    public static final String CAPACITY_WHITE = "whitelist";
    public static final String HPC_PARTITION = "HPC-Partition";
    public static final String HPC_PARTITION_MERGE = "HPC-Partition-Merge";
    public static final String HPC_PARTITION_LEADER = "HPC-Partition-Leader";
    public static final String HPC_GROUP = "HPC-Group";
    public static final String HPC_FTP = "HPC-Ftp";
    public static final String HPC_FTP_LARGE = "HPC-Ftp-Large";
    public static final String HPC_QUOTA = "HPC-Quota";
    public static final String KUBECLOUD_PROFECT = "KubeCloud-Project";
    public static final String KUBECLOUD_NAMESPACE = "KubeCloud-Namespace";
    public static final String BIGDATA_CLUSTER = "Bigdata-Cluster";
    public static final String BIGDATA_CLUSTER_RENEW = "Bigdata-Cluster-Renew";
    public static final String BIGDATA_HDFSPOLICY = "Bigdata-Hdfspolicy";
    public static final String BIGDATA_HDFSPOLICY_RENEW = "Bigdata-Hdfspolicy-Renew";
    public static final String BIGDATA_DIR = "Bigdata-Dir";
    public static final String HARBOR_PROJECT = "Harbor-Project";
    public static final String CEPH_OSS = "Ceph-OSS";
    public static final String CEPH_FS = "Ceph-FS";
    public static final String DNS_SUB_DOMAIN = "DNS-Sub-Domain";
    public static final String DNS_TOP_DOMAIN = "DNS-Top-Domain";

    public static final String X_SG_SESSION_SESSIONID = "x-sg-session-sessionid";
    public static final String X_SG_SESSION_TENANTID = "x-sg-session-tenantid";
    public static final String X_SG_SESSION_CALLERID = "x-sg-session-callerid";
    public static final String X_SG_SESSION_CREDENTIALID = "x-sg-session-credentialid";
    public static final String X_SG_SESSION_ID_TOKEN = "x-sg-session-id-token";
}
