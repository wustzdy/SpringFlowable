package com.wustzdy.springboot.flowable.demo.entity;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 系统用户
 */
@NoArgsConstructor
@Data
public class SysUserEntity {
    private Long userId;

    private String name;

    private String username;

    private Boolean master;

    private Boolean manager;

    private String email;

    private String mobile;

    private List<Long> roleIdList;

    private String departName;

    private String wholeDepartName;

    private String departmentCode;

    private String leader;

    private Integer wechatLink;

    private String defaultLink;

    private String masterUsername;

    private String firstDepartName;

    private String secondDepartName;

    private String thirdDepartName;

}
