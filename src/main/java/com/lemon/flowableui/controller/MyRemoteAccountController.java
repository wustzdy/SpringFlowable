package com.lemon.flowableui.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.editor.language.json.converter.BpmnJsonConverter;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.ui.common.model.UserRepresentation;
import org.flowable.ui.common.rest.idm.CurrentUserProvider;
import org.flowable.ui.modeler.domain.Model;
import org.flowable.ui.modeler.serviceapi.ModelService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.stream.Collectors;


@RestController()
@RequestMapping("/my-app")
public class MyRemoteAccountController {
    @Autowired
    private ModelService modelService;
    @Autowired
    private RepositoryService repositoryService;
    protected final Collection<CurrentUserProvider> currentUserProviders;

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

}
