package com.wustzdy.springboot.flowable.demo.controller;

import com.wustzdy.springboot.flowable.demo.demo.BusinessTemplateModel;
import com.wustzdy.springboot.flowable.demo.entity.BusinessTemplateEntity;
import com.wustzdy.springboot.flowable.demo.service.BusinessTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/business")
public class BusinessTemplateController {
    @Autowired
    private BusinessTemplateService service;

    @PostMapping("/addTemplate")
    public BusinessTemplateModel addTemplate(@RequestBody BusinessTemplateModel model) {
        return service.add(model);

    }

    @GetMapping("/addTemplate")
    public List<BusinessTemplateEntity> list() {
        return service.list();
    }
}
