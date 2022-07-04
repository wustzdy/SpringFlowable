package com.wustzdy.springboot.flowable.demo.controller;

import com.wustzdy.springboot.flowable.demo.demo.BusinessTemplateModel;
import com.wustzdy.springboot.flowable.demo.service.BusinessTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/business")
public class BusinessTemplateController {
    @Autowired
    private BusinessTemplateService service;

    @PostMapping("/addTemplate")
    public BusinessTemplateModel addTemplate(@RequestBody BusinessTemplateModel model) {
        return service.add(model);

    }
}
