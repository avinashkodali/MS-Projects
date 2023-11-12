package com.aws.ccproject.repo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.util.EC2MetadataUtils;
import com.aws.ccproject.config.AwsConf;
import com.aws.ccproject.constants.Constants;

@Repository
public class EC2RepositoryImplement implements EC2Repository {
	private static Logger log = LoggerFactory.getLogger(EC2RepositoryImplement.class);
	@Autowired
	private AwsConf awsConfiguration;
	public void endInstance() {
		String currentEC2Id = EC2MetadataUtils.getInstanceId();
		if(currentEC2Id !=null) {
			log.info(Constants.END_INSTANCE + currentEC2Id);
			TerminateInstancesRequest req = new TerminateInstancesRequest().withInstanceIds(currentEC2Id);
			awsConfiguration.awsEC2().terminateInstances(req);
		}
	}

}
