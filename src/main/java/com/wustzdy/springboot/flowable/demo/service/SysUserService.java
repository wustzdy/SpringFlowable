package com.wustzdy.springboot.flowable.demo.service;



import com.wustzdy.springboot.flowable.demo.entity.SysUserEntity;

import java.util.List;

public interface SysUserService {

    String getAccountIdByLoginName(String loginName);

    SysUserEntity getSysUserByAccountId(String accountId);


    SysUserEntity getSysUserByUsername(String username);

    String getLeader(String username, boolean validSupserLeader);

    String getEmail(String username);

    List<SysUserEntity> getUserListByRoleId(Long roleId);

}
