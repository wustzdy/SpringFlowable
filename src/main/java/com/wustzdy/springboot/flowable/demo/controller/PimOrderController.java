package com.wustzdy.springboot.flowable.demo.controller;

import com.wustzdy.springboot.flowable.demo.entity.PimOrderEntity;
import com.wustzdy.springboot.flowable.demo.service.PimOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class PimOrderController {
    @Autowired
    private PimOrderService pimOrderService;

    @PostMapping("/save/{orderNum}")
    public void save(@RequestBody PimOrderEntity pimOrder, @PathVariable String orderNum) {

        pimOrderService.startWorkflowAndSave(pimOrder, "zhudayang", orderNum);

    }
}
