package com.aws.ccproject.config;

import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.aws.ccproject.constants.Constants;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

@Configuration
public class AwsConf {

	public BasicAWSCredentials basicAWSCredentials() {
		return new BasicAWSCredentials(Constants.AWS_ACCESS_KEY, Constants.AWS_SECRET_KEY);
	}

	public AmazonS3 awsS3() {
		AmazonS3 awsS3 = AmazonS3ClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials()))
				.withRegion(Constants.AWS_REGION)
				.build();
		return awsS3;
	}

	public AmazonSQS awsSQS() {
		AmazonSQS awsSQS = AmazonSQSClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials()))
				.withRegion(Constants.AWS_REGION)
				.build();
		return awsSQS;
	}

	public AmazonEC2 awsEC2() {
		AmazonEC2 awsEC2 = AmazonEC2ClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials()))
				.withRegion(Constants.AWS_REGION)
				.build();
		return awsEC2;
	}

}
