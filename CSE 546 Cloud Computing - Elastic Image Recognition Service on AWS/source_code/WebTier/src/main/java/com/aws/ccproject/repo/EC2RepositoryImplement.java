package com.aws.ccproject.repo;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.amazonaws.services.ec2.model.AmazonEC2Exception;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TagSpecification;
import com.aws.ccproject.config.AwsConf;
import com.aws.ccproject.constants.Constants;
import com.aws.ccproject.constants.GenerateIntId;

@Repository
public class EC2RepositoryImplement implements EC2Repository {

	private static final Logger log = LoggerFactory.getLogger(EC2RepositoryImplement.class);

	private static final String NAME = "Name";
	private static final String APP_INSTANCE = "AppTier-Instance";
	private static final String INSTANCE = "instance";
	private static final String OTHER_EXCEPTION = "Exception2: {}";

	@Autowired
	private AwsConf awsConfiguration;

	@Override
	public Integer createInst(String imgId, Integer maxNOI, Integer nameCnt) {
		List<String> secGrpIds = new ArrayList<String>();
		Collection<TagSpecification> tagSpecifications = new ArrayList<TagSpecification>();
		TagSpecification tagSpecification = new TagSpecification();
		Collection<Tag> tags = new ArrayList<Tag>();
		Tag tag = new Tag();
		secGrpIds.add(Constants.AWS_SECURITY_GROUP_ID1);
		secGrpIds.add(Constants.AWS_SECURITY_GROUP_ID2);
		int nextVal = GenerateIntId.generate();
		tag.setValue(APP_INSTANCE + "-" + nextVal);
		tag.setKey(NAME);
		tags.add(tag);
		tagSpecification.setResourceType(INSTANCE);
		tagSpecification.setTags(tags);
		tagSpecifications.add(tagSpecification);
		String userDataScript = "#!/bin/bash\n"+ "sudo systemctl enable AppTier.service\n"+ "sudo systemctl start AppTier.service\n";
		RunInstancesRequest runReq = new RunInstancesRequest(imgId, 1, 1);
		runReq.setInstanceType(InstanceType.T2Micro);
		runReq.setSecurityGroupIds(secGrpIds);
		runReq.setTagSpecifications(tagSpecifications);
		runReq.setUserData(Base64.getEncoder().encodeToString(userDataScript.getBytes()));
		try {
			awsConfiguration.awsEC2().runInstances(runReq);
		} catch (AmazonEC2Exception amzEc2Exp) {
			log.info("EC2 instance creation failed: " + amzEc2Exp.getErrorMessage());
		} catch (Exception e) {
			log.info(OTHER_EXCEPTION, e.getMessage());
		}
		return nameCnt;
	}

	@Override
	public DescribeInstanceStatusResult descInstStatus(DescribeInstanceStatusRequest descReq) {
		return awsConfiguration.awsEC2().describeInstanceStatus(descReq);
	}

	@Override
	public DescribeInstancesResult descInsts(DescribeInstancesRequest descInstsReq) {
		return awsConfiguration.awsEC2().describeInstances(descInstsReq);
	}

}
