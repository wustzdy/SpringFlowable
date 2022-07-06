package com.wustzdy.springboot.flowable.demo.service;


import com.wustzdy.springboot.flowable.demo.entity.SysUserEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

//@FeignClient(name = "opencloud-organization", fallback = OrganizationProviderFallback.class)
//@FeignClient(name = "opencloud-organization", url = "http://localhost:8010", fallback = OrganizationProviderFallback.class)
//@FeignClient(name = "account", url = "http://account", fallback = OrganizationProviderFallback.class)
public interface OrganizationProvider {

    @PostMapping("/account/service/_findByName")
    String findByServiceName(@RequestBody Map<String, Object> data);

    @PostMapping("/account/users/_findByLoginName")
    String accountFindByLoginName(@RequestBody Map<String, Object> data);

    @GetMapping("/account/enterpriseUsers/accountId:{accountId}")
    String getEnterpriseUsersByAccountId(@PathVariable String accountId);

    @GetMapping("/account/users/accountId:{accountId}")
    String getByAccountId(@PathVariable String accountId);

    @GetMapping("/account/enterpriseUsers")
    String getLeaderNew(@RequestParam(value = "page") Integer page,
                        @RequestParam(value = "pageSize") Integer pageSize,
                        @RequestParam(value = "similarEmployeeName", required = false) String similarEmployeeName);

}
