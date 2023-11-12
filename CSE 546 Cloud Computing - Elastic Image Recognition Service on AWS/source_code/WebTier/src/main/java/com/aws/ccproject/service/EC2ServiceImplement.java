package com.aws.ccproject.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.InstanceStateName;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.amazonaws.services.ec2.model.Reservation;
import com.aws.ccproject.constants.Constants;
import com.aws.ccproject.repo.EC2Repository;

@Service
public class EC2ServiceImplement implements EC2Service {

	@Autowired
	private EC2Repository ec2Repo;
	
	@Override
	public Integer startInsts(Integer cnt, Integer nameCnt) {
		return ec2Repo.createInst(Constants.IMAGE_ID, cnt, nameCnt);
	}

	@Override
	public Integer getNumInsts() {		
		DescribeInstancesRequest descInstsReq = new DescribeInstancesRequest();
		
		Filter runningInstsFilter = new Filter();
		runningInstsFilter.setName("instance-state-name");
		runningInstsFilter.setValues(Arrays.asList(new String[] {"running", "pending"}));
		
		descInstsReq.setFilters( Arrays.asList(new Filter[] {runningInstsFilter}));
		
		descInstsReq.setMaxResults(1000);
		
		DescribeInstancesResult descInstsRes = ec2Repo.descInsts(descInstsReq);
		
		int cntRunningInsts = 0;
		for(Reservation res : descInstsRes.getReservations()) {
			cntRunningInsts += res.getInstances().size();
		}
		return cntRunningInsts;
	}
}
