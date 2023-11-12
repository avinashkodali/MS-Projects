package com.aws.ccproject.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aws.ccproject.constants.Constants;

@Service
public class LoadBalancingServiceImplement implements LoadBalancingService {

	private static final Logger log = LoggerFactory.getLogger(LoadBalancingServiceImplement.class);

	@Autowired
	private SQSService sqsService;

	@Autowired
	private EC2Service ec2Service;

	@Override
	public void scaleOut() {
		log.info("Scaling out started");
		Integer nameCnt = 0;
		while (true) {
			Integer cntRunningInsts = ec2Service.getNumInsts();
			Integer numAppInsts = cntRunningInsts - 1; //1 webtier
			Integer numMsgs = sqsService.getApproxNoMsgs(Constants.INPUT_SQS);
			log.info("Msgs in InputSQS: " + numMsgs + ", Running Instances:" + cntRunningInsts + ", Apptier Instances Running:" + numAppInsts);
			if (numMsgs > 0 && numMsgs > numAppInsts && nameCnt<Constants.MAXIMUM_RUNNING_INSTANCES) {
				Integer temp = Constants.MAXIMUM_RUNNING_INSTANCES - numAppInsts;
				if (temp > 0) {
					Integer temp1 = numMsgs - numAppInsts;
					if (temp<=temp1) {
						nameCnt = ec2Service.startInsts(temp, nameCnt);
					} else {
						nameCnt = ec2Service.startInsts(temp1, nameCnt);
					}
					nameCnt++;
				}
			}
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
