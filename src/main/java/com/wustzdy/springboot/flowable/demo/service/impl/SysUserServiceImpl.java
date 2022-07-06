package com.wustzdy.springboot.flowable.demo.service.impl;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wustzdy.springboot.flowable.demo.entity.SysUserEntity;
import com.wustzdy.springboot.flowable.demo.service.OrganizationProvider;
import com.wustzdy.springboot.flowable.demo.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SuppressWarnings("all")
@Service("sysUserService")
@Slf4j
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private OrganizationProvider organizationProvider;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public String getAccountIdByLoginName(String loginName) {
        log.info("--getAccountIdByLoginName-loginName:{}", loginName);
        Map<String, Object> data = new HashMap<>();
        data.put("loginName", loginName);

        String jsonStr = organizationProvider.accountFindByLoginName(data);
        log.info("--getAccountIdByLoginName-jsonStr:{}", jsonStr);
        JSONObject accountInfo = JSONObject.parseObject(jsonStr);

        return accountInfo.getString("accountId");
    }

    @Override
    public SysUserEntity getSysUserByAccountId(String accountId) {
        SysUserEntity userEntity = new SysUserEntity();
        String result = organizationProvider.getEnterpriseUsersByAccountId(accountId);
        log.info(result);
        JSONArray orgs = JSONArray.parseArray(result);
        if (orgs.size() >= 1) {
            JSONObject org = orgs.getJSONObject(0);

            userEntity.setName(org.getString("employeeName"));
            userEntity.setDepartName(org.getString("enterpriseName"));
            userEntity.setWholeDepartName(org.getString("enterpriseName"));
            userEntity.setDepartmentCode(org.getString("departmentCode"));
        }
        return userEntity;
    }

    @Override
    public SysUserEntity getSysUserByUsername(String username) {
      /*  Result<SysUserEntity> result = organizationProvider.getUserByUniqueId(username);
        RestResultUtils.valid(result);
        return result.getData();*/
        return getSysUserByAccountId(getAccountIdByLoginName(username));
    }

    @Override
    public String getLeader(String username, boolean validSupserLeader) {
       /* Result<String> result = organizationProvider.getLeader(username, validSupserLeader);
        RestResultUtils.valid(result);*/

        String result = organizationProvider.getLeaderNew(1, 10, username);
        String leader = null;
        try {
            JsonNode jsonNode = objectMapper.readTree(result);
            JsonNode recordsNode = jsonNode.get("records");
            if (recordsNode.isArray()) {
                JsonNode record = recordsNode.get(0);
                leader = record.get("extras").get("managerInfo").get("extras").get("name").get("name").asText();
            }
        } catch (IOException e) {
            log.error("s-order enterpriseUsers  get leader info request is error:{}", e);
            throw new RuntimeException(e.getMessage());
        }
        return leader;
    }

    @Override
    public String getEmail(String username) {
       /* Result<String> result = organizationProvider.getEmail(username);
        RestResultUtils.valid(result);
        return result.getData();
        */
        String result = organizationProvider.getByAccountId(getAccountIdByLoginName(username));
        log.info("-getEmail-result-:{}", result);
        JSONObject accountInfo = JSONObject.parseObject(result);
        log.info("-getEmail-accountInfo-:{}", accountInfo);

        return accountInfo.getString("emailAddress");
    }

    @Override
    public List<SysUserEntity> getUserListByRoleId(Long roleId) {
//        Result<List<SysUserEntity>> userListByRoleId = organizationProvider.getUserListByRoleId(roleId);
//        RestResultUtils.valid(userListByRoleId);
//        return userListByRoleId.getData();
        return null;
    }

    public static void main(String[] args) {
        String result = "{\n" +
                "    \"records\": [\n" +
                "        {\n" +
                "            \"userId\": \"OWxv9nsW7Xr8M\",\n" +
                "            \"accountId\": \"DeaKJztomeWoK\",\n" +
                "            \"enterpriseName\": \"SenseTime\",\n" +
                "            \"employeeName\": \"yangguang3\",\n" +
                "            \"employeeCode\": \"4366\",\n" +
                "            \"employeeStatus\": {\n" +
                "                \"value\": \"NORMAL\",\n" +
                "                \"label\": \"Normal\"\n" +
                "            },\n" +
                "            \"departmentCode\": \"10001127\",\n" +
                "            \"extras\": {\n" +
                "                \"accountStatus\": \"NORMAL\",\n" +
                "                \"uid\": 900024060,\n" +
                "                \"gid\": 100,\n" +
                "                \"name\": {\n" +
                "                    \"name\": \"yangguang3\",\n" +
                "                    \"displayName\": \"杨广\"\n" +
                "                },\n" +
                "                \"managerInfo\": {\n" +
                "                    \"userId\": \"Rz9b19spMBzwd\",\n" +
                "                    \"accountId\": \"8NOenOsJB5ndk\",\n" +
                "                    \"enterpriseName\": \"SenseTime\",\n" +
                "                    \"employeeName\": \"maruijin\",\n" +
                "                    \"employeeCode\": \"6772\",\n" +
                "                    \"employeeStatus\": {\n" +
                "                        \"value\": \"NORMAL\",\n" +
                "                        \"label\": \"Normal\"\n" +
                "                    },\n" +
                "                    \"departmentCode\": \"10000780\",\n" +
                "                    \"extras\": {\n" +
                "                        \"accountStatus\": \"NORMAL\",\n" +
                "                        \"uid\": 900017275,\n" +
                "                        \"gid\": 100,\n" +
                "                        \"name\": {\n" +
                "                            \"name\": \"maruijin\",\n" +
                "                            \"displayName\": \"马瑞金\"\n" +
                "                        },\n" +
                "                        \"email\": \"maruijin@sensetime.com\"\n" +
                "                    }\n" +
                "                },\n" +
                "                \"departmentInfo\": {\n" +
                "                    \"departmentId\": \"V3AxpatYD4pbZ\",\n" +
                "                    \"enterpriseName\": \"SenseTime\",\n" +
                "                    \"departmentName\": \"云业务中台组\",\n" +
                "                    \"departmentCode\": \"10001127\",\n" +
                "                    \"departmentStatus\": {\n" +
                "                        \"value\": \"NORMAL\",\n" +
                "                        \"label\": \"Normal\"\n" +
                "                    },\n" +
                "                    \"parentDepartmentCode\": \"10000780\",\n" +
                "                    \"managerEmployeeCode\": \"6772\",\n" +
                "                    \"extras\": {\n" +
                "                        \"level\": {\n" +
                "                            \"level\": \"23\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                },\n" +
                "                \"email\": \"yangguang3@sensetime.com\"\n" +
                "            }\n" +
                "        }\n" +
                "    ],\n" +
                "    \"total\": 1,\n" +
                "    \"pageSize\": 20,\n" +
                "    \"page\": 1\n" +
                "}";
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(result);
            JsonNode recordsNode = jsonNode.get("records");
            if (recordsNode.isArray()) {
                JsonNode record = recordsNode.get(0);
                String leader = record.get("extras").get("managerInfo").get("extras").get("name").get("name").asText();
                System.out.println("--leader--:" + leader);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
