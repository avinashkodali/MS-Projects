package com.aws.ccproject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.aws.ccproject.config.AppConf;
import com.aws.ccproject.service.ListenDispatchService;

@SpringBootApplication
public class AppTierListener {
	
	private static final Logger logger = LoggerFactory.getLogger(AppTierListener.class);

	public static void main(String[] args) {
		SpringApplication.run(AppTierListener.class, args);
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConf.class);
		logger.info("AppTierListener running..");
		System.out.println("AppTierListener running..");
		ListenDispatchService listenDispatchService = context.getBean(ListenDispatchService.class);
		listenDispatchService.generalMethod();
		context.close();
	}
}
