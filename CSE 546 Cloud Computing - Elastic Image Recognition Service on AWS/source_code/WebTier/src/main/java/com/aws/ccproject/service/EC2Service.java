package com.aws.ccproject.service;

public interface EC2Service {

	public Integer getNumInsts();

	public Integer startInsts(Integer cnt, Integer nameCnt);

}
