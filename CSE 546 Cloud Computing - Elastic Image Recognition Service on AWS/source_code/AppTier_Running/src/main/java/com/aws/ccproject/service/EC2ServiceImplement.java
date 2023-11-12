package com.aws.ccproject.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aws.ccproject.repo.EC2Repository;

@Service
public class EC2ServiceImplement implements EC2Service {

	@Autowired
	private EC2Repository ec2Repo;

	@Override
	public void endInstance() {
		ec2Repo.endInstance();
	}

}
