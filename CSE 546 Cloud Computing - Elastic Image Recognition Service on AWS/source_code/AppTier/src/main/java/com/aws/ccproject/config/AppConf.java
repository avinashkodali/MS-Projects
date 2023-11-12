package com.aws.ccproject.config;

import org.springframework.context.annotation.Bean;

import com.aws.ccproject.repo.EC2Repository;
import com.aws.ccproject.repo.EC2RepositoryImplement;
import com.aws.ccproject.repo.S3Repository;
import com.aws.ccproject.repo.S3RepositoryImplement;
import com.aws.ccproject.repo.SQSRepository;
import com.aws.ccproject.repo.SQSRepositoryImplement;
import com.aws.ccproject.service.EC2Service;
import com.aws.ccproject.service.EC2ServiceImplement;
import com.aws.ccproject.service.ListenDispatchService;
import com.aws.ccproject.service.ListenDispatchServiceImplement;
import com.aws.ccproject.service.S3Service;
import com.aws.ccproject.service.S3ServiceImplement;
import com.aws.ccproject.service.SQSService;
import com.aws.ccproject.service.SQSServiceImplement;

public class AppConf {
	
	@Bean
	public AwsConf awsConfiguration() {
		return new AwsConf();
	}

	@Bean
	public S3Repository s3Repo() {
		return new S3RepositoryImplement();
	}

	@Bean
	public S3Service s3Service() {
		return new S3ServiceImplement();
	}
	
	@Bean
	public SQSService sqsService() {
		return new SQSServiceImplement();
	}

	@Bean
	public SQSRepository sqsRepo() {
		return new SQSRepositoryImplement();
	}
	
	@Bean
	public EC2Repository ec2Repo() {
		return new EC2RepositoryImplement();
	}

	@Bean
	public EC2Service ec2Service() {
		return new EC2ServiceImplement();
	}
	
	@Bean
	public ListenDispatchService listenDispatchService() {
		return new ListenDispatchServiceImplement();
	}

}
