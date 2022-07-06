package com.wustzdy.springboot.flowable.demo.constant;

public class Constant {
    /**
     * 超级管理员ID
     */
    public static final int SUPER_ADMIN = 1;


    /**
     * 普通用户
     */
    public static final Long ORDINARY_USERS = 2L;

    /**
     * 管理员
     */
    public static final Long ADMINISTRATORS = 3L;

    /**
     * 超级管理员
     */
    public static final Long SUPER_ADMINISTRATOR = 1L;

    /**
     * 当前页码
     */
    public static final String PAGE = "page";
    /**
     * 每页显示记录数
     */
    public static final String LIMIT = "limit";
    /**
     * 排序字段
     */
    public static final String ORDER_FIELD = "sidx";
    /**
     * 排序方式
     */
    public static final String ORDER = "order";
    /**
     * 升序
     */
    public static final String ASC = "asc";

    /**
     * 资源名称
     */
    public static final String BASE_RESOURCE_NAME = "sensetime_resource";

    /**
     * 升级文件key
     */
    public static final String UPGRADE_FILE_KEY = "uploadFile.upgrade.storePath.key";
    /**
     * 升级脚本key
     */
    public static final String UPGRADE_SHELL_KEY = "uploadFile.upgrade.shell.key";

    /**
     * 版本version key
     */
    public static final String VERSION_KEY = "version.key";

    /**
     * kubecloud项目token
     */
    public static final String KUBECLOUD_TOKEN = "kubecloud.token";

    /**
     * restart start stop
     */
    public static final String RESTART = "restart_copy";
    public static final String START = "start";
    public static final String STOP = "stop";

    /**
     * super manager
     */
    public static final String AUDIT_ADMIN = "audit_admin";
    public static final String ADMIN = "admin";
    public static final String A_PWD = "superman@110";
    /**
     * 登陆类型
     */
    public static final String LDAP_LOGIN = "ldap";
    public static final String LOCAL_LOGIN = "local";
    /**
     * file
     */
    public static final String SUFFIX_JAR = ".jar";
    public static final Integer UPGRADE_MAX_SIZE = 10 * 1024 * 1024;

    /**
     * 字符串分隔符
     */
    public static final String SEPARATOR_COMMA = ",";
    public static final String SEPARATOR_BLANk = " ";
    public static final String SEPARATOR_VERTICAL_LINE = "\\|";
    public static final String SEPARATOR_SEMICOLON = ";";
    public static final String SEPARATOR_WELL = "#";
    public static final String SEPARATOR_UNDER_LINE = "_";
    public static final String SEPARATOR_TILDE = "~";
    /**
     * 环境变量
     */
    public static final String ENV_PROD = "prod";
    public static final String ENV_DEV = "dev";

    /**
     * 菜单类型
     *
     * @date 2016年11月15日 下午1:24:29
     */
    public enum MenuType {
        /**
         * 目录
         */
        CATALOG(0),
        /**
         * 菜单
         */
        MENU(1),
        /**
         * 按钮
         */
        BUTTON(2);

        private int value;

        MenuType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * 定时任务状态
     *
     * @date 2016年12月3日 上午12:07:22
     */
    public enum ScheduleStatus {
        /**
         * 正常
         */
        NORMAL(0),
        /**
         * 暂停
         */
        PAUSE(1);

        private int value;

        ScheduleStatus(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * 云服务商
     */
    enum CloudService {
        /**
         * 七牛云
         */
        QINIU(1),
        /**
         * 阿里云
         */
        ALIYUN(2),
        /**
         * 腾讯云
         */
        QCLOUD(3);

        private int value;

        CloudService(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

}
