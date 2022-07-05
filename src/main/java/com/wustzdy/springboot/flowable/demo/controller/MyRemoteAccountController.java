package com.wustzdy.springboot.flowable.demo.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.wustzdy.springboot.flowable.demo.vo.ActivityVo;
import com.wustzdy.springboot.flowable.demo.vo.ProcdefineVo;

import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.UserTask;
import org.flowable.editor.language.json.converter.BpmnJsonConverter;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ModelQuery;
import org.flowable.engine.repository.NativeModelQuery;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.ui.common.model.UserRepresentation;
import org.flowable.ui.common.rest.idm.CurrentUserProvider;
import org.flowable.ui.modeler.domain.Model;
import org.flowable.ui.modeler.serviceapi.ModelService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;


@RestController()
@RequestMapping("/my-app")
public class MyRemoteAccountController {
    @Autowired
    private ModelService modelService;
    @Autowired
    private RepositoryService repositoryService;
    protected final Collection<CurrentUserProvider> currentUserProviders;
    private Integer limit;
    private Integer page;

    public MyRemoteAccountController(ObjectProvider<CurrentUserProvider> currentUserProviders) {
        this.currentUserProviders = currentUserProviders.orderedStream().collect(Collectors.toList());
    }


    /**
     * GET /rest/account -> get the current user.
     */
    @GetMapping(value = "/rest/account", produces = "application/json")
    public UserRepresentation getAccount(Authentication authentication) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setId("lemon");
        userRepresentation.setFirstName("lemon");
        userRepresentation.setPrivileges(Lists.newArrayList("flowable-idm", "flowable-modeler", "flowable-task"));
        return userRepresentation;
    }

    @GetMapping("/deployment")
    public String deploy(@RequestParam("id") String id) throws Exception {
        Model model = modelService.getModel(id);
        BpmnModel bpmnModel = modelService.getBpmnModel(model);
        Deployment deployment = repositoryService.createDeployment()
                .name(model.getName())
                .addBpmnModel(model.getKey() + ".bpmn", bpmnModel).deploy();
        modelService.saveModel(model);
        System.out.println("deploymentId: " + deployment.getId());
        return deployment.getId();    //部署ID
    }

    //获取流程模型分页数据
    @GetMapping("/models")
    public List<org.flowable.engine.repository.Model> getModels(Integer limit, Integer page) {
        limit = 10;
        page = 1;
        List<org.flowable.engine.repository.Model> list = repositoryService.createModelQuery().orderByCreateTime().desc().listPage(limit * (page - 1)
                , limit);

        List<org.flowable.engine.repository.Model> list1 = repositoryService.createModelQuery().list();
//        modelService.get

        return list;

    }


    //已发布流程列表
    @GetMapping("/procdef")
    public List<ProcdefineVo> getList(Integer limit, Integer page) {
        limit = 10;
        page = 1;
        List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
                .listPage(limit * (page - 1), limit);
        int count = (int) repositoryService.createProcessDefinitionQuery().count();

        List<ProcdefineVo> list = new ArrayList<>();
        for (ProcessDefinition procdefEntity : processDefinitions) {
            ProcdefineVo procdefineVo = new ProcdefineVo(procdefEntity);
            Deployment deployment = repositoryService.createDeploymentQuery()
                    .deploymentId(procdefEntity.getDeploymentId())
                    .singleResult();
            procdefineVo.setDeploymentTime(deployment.getDeploymentTime());
            list.add(procdefineVo);
        }
        list.sort(Comparator.comparing(ProcdefineVo::getDeploymentTime).reversed());
        return list;
    }

    //流程模版-新增

    //流程模版-查询列表

    //任务管理-新增

    //任务管理-查询列表


    //获取userTask任务节点
    @GetMapping("/activityIds/{processDefinitionId}")
    public List<ActivityVo> activityIds(@PathVariable("processDefinitionId") String processDefinitionId) {

        List<ActivityVo> jobList = new ArrayList<>();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);

        Process process = bpmnModel.getProcesses().get(0);
        //获取所有节点
        Collection<FlowElement> flowElements = process.getFlowElements();

        List<UserTask> UserTaskList = process.findFlowElementsOfType(UserTask.class);
        for (UserTask userTask : UserTaskList) {
            ActivityVo map = new ActivityVo();
            map.setId(userTask.getId());
            map.setName(String.valueOf(userTask.getName()));
            jobList.add(map);
        }
        return jobList;
    }

    //流程定义-列表 s-flow/procdef/procdefList
    @GetMapping("/procdef/procdefList")
    public List<ProcdefineVo> getProcdefList() {

        List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
                .list();
        List<ProcdefineVo> list = new ArrayList<>();
        for (ProcessDefinition procdefEntity : processDefinitions) {

            list.add(new ProcdefineVo(procdefEntity));
        }
        return list;
    }
}
