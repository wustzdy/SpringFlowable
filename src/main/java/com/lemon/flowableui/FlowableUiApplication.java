package com.lemon.flowableui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.lemon",
		"org.flowable.ui.modeler",
		"org.flowable.ui.common"})
public class FlowableUiApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlowableUiApplication.class, args);
	}

}
