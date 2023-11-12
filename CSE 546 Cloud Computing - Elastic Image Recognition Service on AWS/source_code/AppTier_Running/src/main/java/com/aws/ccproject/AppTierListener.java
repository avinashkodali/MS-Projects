package com.aws.ccproject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.aws.ccproject.config.AppConf;
import com.aws.ccproject.service.ListenDispatchService;

@SpringBootApplication
public class AppTierListener {
	private static final Logger log = LoggerFactory.getLogger(AppTierListener.class);
	public static void main(String[] args) {
		SpringApplication.run(AppTierListener.class, args);
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConf.class);
		log.info("AppTierListener running...");
		ListenDispatchService listenDispatchService = context.getBean(ListenDispatchService.class);
		listenDispatchService.generalMethod();
		context.close();
	}
}
