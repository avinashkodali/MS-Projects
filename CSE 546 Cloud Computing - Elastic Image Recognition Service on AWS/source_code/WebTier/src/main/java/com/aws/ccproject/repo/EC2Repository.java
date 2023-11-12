package com.aws.ccproject.repo;

import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;

public interface EC2Repository {
	DescribeInstanceStatusResult descInstStatus(DescribeInstanceStatusRequest descReq);

	Integer createInst(String imgId, Integer maxNOI, Integer nameCnt);

	DescribeInstancesResult descInsts(DescribeInstancesRequest descInstsReq);

}
