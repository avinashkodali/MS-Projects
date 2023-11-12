package com.aws.ccproject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.aws.ccproject.config.AppConf;
import com.aws.ccproject.service.LoadBalancingService;

@SpringBootApplication
public class WebTierApp {
	private static Logger logger = LoggerFactory.getLogger(WebTierApp.class);
	public static void main(String[] args) {
		SpringApplication.run(WebTierApp.class, args);
		logger.info("WebTier running..");
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConf.class);
		LoadBalancingService loadBalancingService = context.getBean(LoadBalancingService.class);
		loadBalancingService.scaleOut();
		context.close();
	}	

}
