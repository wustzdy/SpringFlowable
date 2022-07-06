package com.wustzdy.springboot.flowable.demo.controller;

import com.wustzdy.springboot.flowable.demo.demo.BusinessAssignModel;
import com.wustzdy.springboot.flowable.demo.service.BusinessAssigneeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/business")
public class BusinessAssignController {
    @Autowired
    private BusinessAssigneeService service;

    @PostMapping("/assign")
    public BusinessAssignModel addTemplate(@RequestBody BusinessAssignModel model) {
        return service.add(model);

    }
}
